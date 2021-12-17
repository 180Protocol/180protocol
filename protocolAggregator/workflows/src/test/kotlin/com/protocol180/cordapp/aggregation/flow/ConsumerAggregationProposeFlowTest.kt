package com.protocol180.cordapp.aggregation.flow

import com.protocol180.aggregator.states.ConsumerAggregationState
import groovy.util.GroovyTestCase.assertEquals
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Practical exercise instructions Flows part 1.
 * Uncomment the unit tests and use the hints + unit test body to complete the Flows such that the unit tests pass.
 * Note! These tests rely on Quasar to be loaded, set your run configuration to "-ea -javaagent:lib/quasar.jar"
 * Run configuration can be edited in IntelliJ under Run -> Edit Configurations -> VM options
 * On some machines/configurations you may have to provide a full path to the quasar.jar file.
 * On some machines/configurations you may have to use the "JAR manifest" option for shortening the command line.
 */
class ConsumerAggregationProposeFlowTest {
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
    fun flowUsesCorrectNotary() {
        val flow = ConsumerAggregationProposeFlow(host.info.chooseIdentityAndCert().party)
        val future = consumer.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)

        assertEquals(network.notaryNodes[0].info.legalIdentities[0], signedTransaction.notary)

    }

    @Test
    fun txHasOnlyOneValidOutputState() {
        val flow = ConsumerAggregationProposeFlow(host.info.chooseIdentityAndCert().party)
        val future = consumer.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()

        assertEquals(1, signedTransaction.tx.outputStates.size)

        val output = signedTransaction.tx.getOutput(0) as ConsumerAggregationState

        assertEquals(host.info.legalIdentities[0], output.host)

    }

    @Test
    fun flowWorksWithEnclaveProperly() {
        val flow = ConsumerAggregationProposeFlow(host.info.chooseIdentityAndCert().party)
        val future = consumer.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()

        assertEquals(1, signedTransaction.tx.outputStates.size)

        val output = signedTransaction.tx.getOutput(0) as ConsumerAggregationState

        assertEquals(host.info.legalIdentities[0], output.host)

    }


}
