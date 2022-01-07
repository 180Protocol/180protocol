package com.protocol180.cordapp.aggregation.flow

import com.protocol180.aggregator.states.DataOutputState
import com.protocol180.aggregator.states.RewardsState
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
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
    lateinit var consumer: StartedMockNode
    lateinit var host: StartedMockNode
    lateinit var provider1: StartedMockNode
    lateinit var provider2: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
                MockNetworkParameters(
                        cordappsForAllNodes = listOf(
                                TestCordapp.findCordapp("com.protocol180.aggregator.contracts"),
                                TestCordapp.findCordapp("com.protocol180.cordapp.aggregation.flow")
                        )
                )
        )
        consumer = network.createPartyNode()
        host = network.createPartyNode()
        provider1 = network.createNode()
        provider2 = network.createNode()
        network.runNetwork()
    }


    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun consumerAggregationFlowStateCreationTest() {
        val flow = ConsumerAggregationFlow(host.info.chooseIdentityAndCert().party)
        val future = consumer.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val output = signedTransaction.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output.host)
        assertEquals(consumer.info.legalIdentities.first(), output.consumer)

        //check data output and rewards transaction for host
        host.transaction {
            val dataOutputState : DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data
            val rewardsStates = provider1.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states

            assertEquals(output.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer.info.legalIdentities.first(), dataOutputState.consumer)
            assertTrue { dataOutputState.participants.containsAll(
                listOf(host.info.legalIdentities[0], consumer.info.legalIdentities.first())) }

            val rewardsState1 = rewardsStates[0].state.data
            val rewardsState2 = rewardsStates[1].state.data

            assertEquals(host.info.legalIdentities[0], rewardsState1.host)
            assertEquals(provider1.info.legalIdentities.first(), rewardsState1.provider)
            assertTrue { rewardsState1.participants.containsAll(
                listOf(host.info.legalIdentities[0], provider1.info.legalIdentities.first())) }

            assertEquals(host.info.legalIdentities[0], rewardsState2.host)
            assertEquals(provider2.info.legalIdentities.first(), rewardsState2.provider)
            assertTrue { rewardsState2.participants.containsAll(
                listOf(host.info.legalIdentities[0], provider2.info.legalIdentities.first())) }
        }

        //check data output transaction for consumer
        consumer.transaction {
            val dataOutputState : DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(output.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer.info.legalIdentities.first(), dataOutputState.consumer)
            assertTrue { dataOutputState.participants.containsAll(
                listOf(host.info.legalIdentities[0], consumer.info.legalIdentities.first())) }
        }

        //check rewards transaction for provider
        provider1.transaction {
            val rewardsState : RewardsState = provider1.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(host.info.legalIdentities[0], rewardsState.host)
            assertEquals(provider1.info.legalIdentities.first(), rewardsState.provider)
            assertTrue { rewardsState.participants.containsAll(
                listOf(host.info.legalIdentities[0], provider1.info.legalIdentities.first())) }
        }

        //check rewards transaction for provider
        provider2.transaction {
            val rewardsState : RewardsState = provider2.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(host.info.legalIdentities[0], rewardsState.host)
            assertEquals(provider2.info.legalIdentities.first(), rewardsState.provider)
            assertTrue { rewardsState.participants.containsAll(
                listOf(host.info.legalIdentities[0], provider2.info.legalIdentities.first())) }
        }

    }


    @Test
    fun consumerOutputQueryTestAfterAggregation() {
        val flow = ConsumerAggregationFlow(host.info.chooseIdentityAndCert().party)
        val future = consumer.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val output = signedTransaction.tx.getOutput(0) as DataOutputState
        assertEquals(host.info.legalIdentities[0], output.host)
        assertEquals(consumer.info.legalIdentities.first(), output.consumer)

        //Check data output from consumer node
        val consumerDataOutputRetrievalFlow = ConsumerDataOutputRetrievalFlow(output.flowTopic)
        val dataOutputFuture = consumer.startFlow(consumerDataOutputRetrievalFlow)
        val dataOutputRecords = dataOutputFuture.get()
        assert(dataOutputRecords.size > 1)
        println("Consumer Data Output Retrieval : $dataOutputRecords")
        network.runNetwork()


        //Check reward output for each providers node
        val provider1RewardsState: RewardsState = provider1.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data
        var providerRewardOutputRetrievalFlow = ProviderRewardOutputRetrievalFlow(provider1RewardsState.flowTopic)
        val rewardOutputFuture1 = provider1.startFlow(providerRewardOutputRetrievalFlow)
        val provider1RewardOutput = rewardOutputFuture1.get()
        println("Provider 1 Rewards Retrieval : $provider1RewardOutput")
        assertEquals(provider1RewardOutput.size, 1)
        network.runNetwork()

        val provider2RewardsState: RewardsState = provider2.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data
        providerRewardOutputRetrievalFlow = ProviderRewardOutputRetrievalFlow(provider2RewardsState.flowTopic)
        val rewardOutputFuture2 = provider2.startFlow(providerRewardOutputRetrievalFlow)
        val provider2RewardOutput = rewardOutputFuture2.get()
        println("Provider 2 Rewards Retrieval : $provider2RewardOutput")
        assertEquals(provider2RewardOutput.size, 1)
        network.runNetwork()

    }

}
