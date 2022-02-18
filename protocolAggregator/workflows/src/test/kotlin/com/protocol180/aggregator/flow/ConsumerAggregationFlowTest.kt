package com.protocol180.aggregator.flow

import com.protocol180.aggregator.states.CoalitionConfigurationState
import com.protocol180.aggregator.states.CoalitionDataType
import com.protocol180.aggregator.states.DataOutputState
import com.protocol180.aggregator.states.RewardsState
import com.protocol180.aggregator.states.RoleType
import net.corda.core.internal.readFully
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Practical exercise instructions Flows part 1.
 * Uncomment the unit tests and use the hints + unit test body to complete the Flows such that the unit tests pass.
 * Note! These tests rely on Quasar to be loaded, set your run configuration to "-ea -javaagent:lib/quasar.jar"
 * Run configuration can be edited in IntelliJ under Run -> Edit Configurations -> VM options
 * On some machines/configurations you may have to provide a full path to the quasar.jar file.
 * On some machines/configurations you may have to use the "JAR manifest" option for shortening the command line.
 */
class ConsumerAggregationFlowTest {
    lateinit var network: MockNetwork
    lateinit var consumer1: StartedMockNode
    lateinit var consumer2: StartedMockNode
    lateinit var host: StartedMockNode
    lateinit var provider1: StartedMockNode
    lateinit var provider2: StartedMockNode
    lateinit var coalitionConfigurationState: CoalitionConfigurationState

    @Before
    fun setup() {
        network = MockNetwork(
                MockNetworkParameters(
                        cordappsForAllNodes = listOf(
                                TestCordapp.findCordapp("com.protocol180.aggregator.contracts"),
                                TestCordapp.findCordapp("com.protocol180.aggregator.flow")
                        )
                )
        )
        consumer1 = prepareNodeForRole(RoleType.DATA_CONSUMER)
        consumer2 = prepareNodeForRole(RoleType.DATA_CONSUMER)
        host = prepareNodeForRole(RoleType.COALITION_HOST)
        provider1 = prepareNodeForRole(RoleType.DATA_PROVIDER)
        provider2 = prepareNodeForRole(RoleType.DATA_PROVIDER)
        network.runNetwork()
        createConfigurationState()
    }


    @After
    fun tearDown() {
        network.stopNodes()
    }

    private fun prepareNodeForRole(role: RoleType): StartedMockNode {
        return network.createNode(
                parameters = MockNodeParameters(
                        additionalCordapps = listOf(
                                TestCordapp.findCordapp("com.protocol180.aggregator.flow")
                                        .withConfig(mapOf(Pair(NetworkParticipantService.PARTICIPANT_ROLE_CONFIG_KEY, role.name)))
                        )
                )
        )
    }

    private fun createConfigurationState() {
        var coalitionPartyToRole = mapOf(RoleType.COALITION_HOST to setOf(host.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_CONSUMER to setOf(consumer1.info.chooseIdentityAndCert().party.name,consumer2.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_PROVIDER to setOf(provider1.info.chooseIdentityAndCert().party.name,
                provider2.info.chooseIdentityAndCert().party.name))

        val dataTypes = listOf(
                CoalitionDataType("testDataType1", "Test Data Type 1",
                        ClassLoader.getSystemClassLoader().getResourceAsStream("testSchema1.avsc").readFully(),"com.protocol180.aggregator.sample.ExampleAggregationEnclave"),
                CoalitionDataType("testDataType2", "Test Data Type 2",
                        ClassLoader.getSystemClassLoader().getResourceAsStream("testSchema2.avsc").readFully(), "com.protocol180.aggregator.sample.ExampleAggregationEnclave")
        )

        val flow1 = CoalitionConfigurationUpdateFlow(coalitionPartyToRole, dataTypes)
        val future1 = host.startFlow(flow1)
        network.runNetwork()
        val signedTransaction = future1.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        coalitionConfigurationState = signedTransaction.tx.getOutput(0) as CoalitionConfigurationState
    }

    @Test
    fun consumerAggregationFlowStateCreationTest() {
        val dataType = "testDataType2"
        val description = "test schema for DataType2 code"
        uploadAttachmentToNode(provider1.services, dataType,"Provider1InputData.zip")
        uploadAttachmentToNode(provider2.services, dataType,"Provider2InputData.zip")

        val flow = ConsumerAggregationFlow(dataType, description)
        val future = consumer1.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val output = signedTransaction.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output.host)
        assertEquals(consumer1.info.legalIdentities.first(), output.consumer)

        //check data output and rewards transaction for host
        host.transaction {
            val dataOutputState: DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                    VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data
            val rewardsStates = provider1.services.vaultService.queryBy<RewardsState>(
                    VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states

            assertEquals(output.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer1.info.legalIdentities.first(), dataOutputState.consumer)
            assertTrue {
                dataOutputState.participants.containsAll(
                        listOf(host.info.legalIdentities[0], consumer1.info.legalIdentities.first()))
            }

            val rewardsState1 = rewardsStates[0].state.data
            val rewardsState2 = rewardsStates[1].state.data

            assertEquals(host.info.legalIdentities[0], rewardsState1.host)
            assertEquals(provider1.info.legalIdentities.first(), rewardsState1.provider)
            assertTrue {
                rewardsState1.participants.containsAll(
                        listOf(host.info.legalIdentities[0], provider1.info.legalIdentities.first()))
            }

            assertEquals(host.info.legalIdentities[0], rewardsState2.host)
            assertEquals(provider2.info.legalIdentities.first(), rewardsState2.provider)
            assertTrue {
                rewardsState2.participants.containsAll(
                        listOf(host.info.legalIdentities[0], provider2.info.legalIdentities.first()))
            }
        }

        //check data output transaction for consumer
        consumer1.transaction {
            val dataOutputState: DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                    VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(output.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer1.info.legalIdentities.first(), dataOutputState.consumer)
            assertTrue {
                dataOutputState.participants.containsAll(
                        listOf(host.info.legalIdentities[0], consumer1.info.legalIdentities.first()))
            }
        }

        //check rewards transaction for provider
        provider1.transaction {
            val rewardsState: RewardsState = provider1.services.vaultService.queryBy<RewardsState>(
                    VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(host.info.legalIdentities[0], rewardsState.host)
            assertEquals(provider1.info.legalIdentities.first(), rewardsState.provider)
            assertTrue {
                rewardsState.participants.containsAll(
                        listOf(host.info.legalIdentities[0], provider1.info.legalIdentities.first()))
            }
        }

        //check rewards transaction for provider
        provider2.transaction {
            val rewardsState: RewardsState = provider2.services.vaultService.queryBy<RewardsState>(
                    VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(host.info.legalIdentities[0], rewardsState.host)
            assertEquals(provider2.info.legalIdentities.first(), rewardsState.provider)
            assertTrue {
                rewardsState.participants.containsAll(
                        listOf(host.info.legalIdentities[0], provider2.info.legalIdentities.first()))
            }
        }

    }

    @Test
    fun multipleConcurrentConsumerAggregationFlowTest() {
        val dataType = "testDataType2"
        val description = "test schema for DataType2 code"
        uploadAttachmentToNode(provider1.services, dataType, "Provider1InputData.zip")
        uploadAttachmentToNode(provider2.services, dataType, "Provider2InputData.zip")

        val flow = ConsumerAggregationFlow(dataType, description)
        val future = consumer1.startFlow(flow)


        val flow1 = ConsumerAggregationFlow(dataType, "test schema for sencond aggregation cycle")
        val future1 = consumer2.startFlow(flow1)

        // launching receive call for consumer1 aggregation request
        host.pumpReceive()

        // launching receive call for consumer2 aggregation request
        host.pumpReceive()

        // after both aggregation flow launched above in parallel, executing remaining flow calls for both aggregation request.
        network.runNetwork()

        val signedTransaction1 = future.get()
        assertEquals(1, signedTransaction1.tx.outputStates.size)
        val output1 = signedTransaction1.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output1.host)
        assertEquals(consumer1.info.legalIdentities.first(), output1.consumer)


        val signedTransaction2 = future1.get()
        assertEquals(1, signedTransaction2.tx.outputStates.size)
        val output2 = signedTransaction2.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output2.host)
        assertEquals(consumer2.info.legalIdentities.first(), output2.consumer)

        //check data output transaction for consumer1
        consumer1.transaction {
            val dataOutputState: DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(output1.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer1.info.legalIdentities.first(), dataOutputState.consumer)
            assertTrue {
                dataOutputState.participants.containsAll(
                    listOf(host.info.legalIdentities[0], consumer1.info.legalIdentities.first()))
            }
        }

        //check data output transaction for consumer2
        consumer2.transaction {
            val dataOutputState: DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(output2.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer2.info.legalIdentities.first(), dataOutputState.consumer)
            assertTrue {
                dataOutputState.participants.containsAll(
                    listOf(host.info.legalIdentities[0], consumer2.info.legalIdentities.first()))
            }
        }
    }


    @Test
    fun consumerOutputQueryTestAfterAggregation() {
        val dataType = "testDataType2"
        val description = "test schema for DataType2 code"
        uploadAttachmentToNode(provider1.services, dataType,"Provider1InputData.zip")
        uploadAttachmentToNode(provider2.services, dataType,"Provider2InputData.zip")

        val flow = ConsumerAggregationFlow("testDataType2", description)
        val future = consumer1.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val output = signedTransaction.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output.host)
        assertEquals(consumer1.info.legalIdentities.first(), output.consumer)

        //Check data output from consumer node
        val consumerDataOutputRetrievalFlow = ConsumerDataOutputRetrievalFlow(output.flowTopic)
        val dataOutputFuture = consumer1.startFlow(consumerDataOutputRetrievalFlow)
        val dataOutputRecords = dataOutputFuture.get()
        assertNotNull(dataOutputRecords)
        println("Data Output : $dataOutputRecords")
        network.runNetwork()


        //Check reward output for each providers node
        val provider1RewardsState: RewardsState = provider1.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data
        var providerRewardOutputRetrievalFlow = ProviderRewardOutputRetrievalFlow(provider1RewardsState.flowTopic)
        val rewardOutputFuture1 = provider1.startFlow(providerRewardOutputRetrievalFlow)
        val provider1RewardOutput = rewardOutputFuture1.get()

        assertNotNull(provider1RewardOutput)
        println("Reward Output 1: $provider1RewardOutput")
        network.runNetwork()

        val provider2RewardsState: RewardsState = provider2.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data
        providerRewardOutputRetrievalFlow = ProviderRewardOutputRetrievalFlow(provider2RewardsState.flowTopic)
        val rewardOutputFuture2 = provider2.startFlow(providerRewardOutputRetrievalFlow)
        val provider2RewardOutput = rewardOutputFuture2.get()
        assertNotNull(provider2RewardOutput)
        println("Reward Output 2: $provider2RewardOutput")
        network.runNetwork()

    }

    @Test
    fun consumerAggregationFlowFailTest() {
        //check unsupported data type
        val flow = ConsumerAggregationFlow("testDataType3","sample Data type description")
        val future = consumer1.startFlow(flow)
        network.runNetwork()
        assertFailsWith(ConsumerAggregationFlowException::class) { future.getOrThrow() }

        //check new consumer added to coalition without updating coalition configuration
        var consumer2: StartedMockNode = prepareNodeForRole(RoleType.DATA_CONSUMER)
        val flow2 = ConsumerAggregationFlow("testDataType2", "sample Data type description")
        val future2 = consumer2.startFlow(flow2)
        network.runNetwork()
        assertFailsWith(ConsumerAggregationFlowException::class) { future2.getOrThrow() }
    }



    private fun uploadAttachmentToNode(service: ServiceHub,
                                       dataType: String,
                                       filename: String): String {
        val attachmentHash = service.attachments.importAttachment(ClassLoader.getSystemClassLoader().getResourceAsStream(filename), dataType, filename)

        return attachmentHash.toString();
    }

}
