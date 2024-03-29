package com.protocol180.aggregator.storage.flow

import com.protocol180.aggregator.flow.*
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
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
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
class EstuaryStorageConsumerAggregationFlowTest {
    lateinit var network: MockNetwork
    lateinit var consumer1: StartedMockNode
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
                    TestCordapp.findCordapp("com.protocol180.aggregator.flow"),
                    TestCordapp.findCordapp("com.protocol180.aggregator.storage.flow")
                )
            )
        )
        consumer1 = prepareNodeForRole(RoleType.DATA_CONSUMER)
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
                        .withConfig(
                            mapOf(
                                Pair(
                                    NetworkParticipantService.PARTICIPANT_ROLE_CONFIG_KEY,
                                    role.name
                                ),
                                Pair(
                                    NetworkParticipantService.ESTUARY_STORAGE_TOKEN,
                                    "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_TENANT_ID,
                                    "XXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_ID,
                                    "XXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_SECRET,
                                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_KEY_IDENTIFIER,
                                    "keyvaulttestXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                                )
                            )
                        )
                )
            )
        )
    }

    private fun createConfigurationState() {
        var coalitionPartyToRole = mapOf(RoleType.COALITION_HOST to setOf(host.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_CONSUMER to setOf(consumer1.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_PROVIDER to setOf(provider1.info.chooseIdentityAndCert().party.name,
                provider2.info.chooseIdentityAndCert().party.name))

        val dataTypes = listOf(
            CoalitionDataType("testDataType1", "Test Data Type 1",
                ClassLoader.getSystemClassLoader().getResourceAsStream("testSchema1.avsc").readFully(),"com.protocol180.aggregator.sample.ExampleAggregationEnclave")
        )

        val flow1 = CoalitionConfigurationUpdateFlow(coalitionPartyToRole, dataTypes)
        val future1 = host.startFlow(flow1)
        network.runNetwork()
        val signedTransaction = future1.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        coalitionConfigurationState = signedTransaction.tx.getOutput(0) as CoalitionConfigurationState
    }

    @Test
    fun estauryStorageConsumerAggregationFlowStateCreationTest() {
        val dataType = "testDataType1"
        val description = "test schema for DataType1 code"

        val provider1KeyFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        val provider1KeyFlowFuture = provider1.startFlow(provider1KeyFlow);
        val provider1Key = provider1KeyFlowFuture.get();
        network.runNetwork()

        val provider1Flow = EstauryStorageProviderAggregationInputFlow(ClassLoader.getSystemClassLoader().getResourceAsStream("Provider1InputData.csv").readFully(), dataType, "filecoin", provider1Key);
        val provider1FlowFuture = provider1.startFlow(provider1Flow)
        network.runNetwork()

        val provider2KeyFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        val provider2KeyFlowFuture = provider2.startFlow(provider2KeyFlow);
        val provider2Key = provider2KeyFlowFuture.get();
        network.runNetwork()

        val provider2Flow = EstauryStorageProviderAggregationInputFlow(ClassLoader.getSystemClassLoader().getResourceAsStream("Provider2InputData.csv").readFully(), dataType, "filecoin", provider2Key);
        val provider2FlowFuture = provider2.startFlow(provider2Flow)
        network.runNetwork()

        val consumer1KeyFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        val consumer1KeyFlowFuture = consumer1.startFlow(consumer1KeyFlow)
        network.runNetwork()

        val flow = EstuaryStorageConsumerAggregationFlow(
            dataType,
            description,
            "filecoin"
        )
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
    fun estauryStorageConsumerOutputQueryTestAfterAggregation() {
        val dataType = "testDataType1"
        val description = "test schema for DataType1 code"

        val provider1KeyFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        val provider1KeyFlowFuture = provider1.startFlow(provider1KeyFlow);
        val provider1Key = provider1KeyFlowFuture.get();
        network.runNetwork()

        val provider1Flow = EstauryStorageProviderAggregationInputFlow(ClassLoader.getSystemClassLoader().getResourceAsStream("Provider1InputData.csv").readFully(), dataType, "filecoin", provider1Key);
        val provider1FlowFuture = provider1.startFlow(provider1Flow)
        network.runNetwork()

        val provider2KeyFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        val provider2KeyFlowFuture = provider2.startFlow(provider2KeyFlow);
        val provider2Key = provider2KeyFlowFuture.get();
        network.runNetwork()

        val provider2Flow = EstauryStorageProviderAggregationInputFlow(ClassLoader.getSystemClassLoader().getResourceAsStream("Provider2InputData.csv").readFully(), dataType, "filecoin", provider2Key);
        val provider2FlowFuture = provider2.startFlow(provider2Flow)
        network.runNetwork()

        val consumer1KeyFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        val consumer1KeyFlowFuture = consumer1.startFlow(consumer1KeyFlow)
        network.runNetwork()

        val flow = EstuaryStorageConsumerAggregationFlow(
            "testDataType1",
            description,
            "filecoin"
        )
        val future = consumer1.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val output = signedTransaction.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output.host)
        assertEquals(consumer1.info.legalIdentities.first(), output.consumer)

        //Check data output from consumer node
        val consumerDataOutputRetrievalFlow = EstuaryStorageConsumerDataOutputRetrievalFlow(
            output.cid,
            output.encryptionKeyId
        );
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
}
