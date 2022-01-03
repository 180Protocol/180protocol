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
//    lateinit var provider2: StartedMockNode

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
//        provider2 = network.createNode()
        network.runNetwork()
    }


    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun flowWorksWithEnclaveProperly() {
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
            val rewardsState : RewardsState = provider1.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(output.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer.info.legalIdentities.first(), dataOutputState.consumer)

            assertEquals(host.info.legalIdentities[0], rewardsState.host)
            assertEquals(provider1.info.legalIdentities.first(), rewardsState.provider)
        }

        //check data output transaction for consumer
        consumer.transaction {
            val dataOutputState : DataOutputState = host.services.vaultService.queryBy<DataOutputState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(output.flowTopic, dataOutputState.flowTopic)
            assertEquals(host.info.legalIdentities[0], dataOutputState.host)
            assertEquals(consumer.info.legalIdentities.first(), dataOutputState.consumer)
        }

        //check rewards transaction for provider
        provider1.transaction {
            val rewardsState : RewardsState = provider1.services.vaultService.queryBy<RewardsState>(
                VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).states.single().state.data

            assertEquals(host.info.legalIdentities[0], rewardsState.host)
            assertEquals(provider1.info.legalIdentities.first(), rewardsState.provider)
        }

    }


}
