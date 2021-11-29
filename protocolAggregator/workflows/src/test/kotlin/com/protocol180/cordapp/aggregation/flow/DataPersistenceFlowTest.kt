package com.protocol180.cordapp.aggregation.flow

import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class DataPersistenceFlowTest {
    private lateinit var network: MockNetwork
    private lateinit var host: StartedMockNode

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
        host = network.createPartyNode()
        network.runNetwork()
    }


    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun flowWritesToSchemaTableCorrectly() {
        var providerMap1 = mapOf("publickey11" to "input11".toByteArray(),
                "publickey12" to "input12".toByteArray(), "publickey13" to "input13".toByteArray())
        var providerMap2 = mapOf("publickey21" to "input21".toByteArray(),
                "publickey22" to "input22".toByteArray(), "publickey23" to "input23".toByteArray())

        var dataOutputMap = mapOf("stateref1" to providerMap1, "stateref2" to providerMap2)

        val dataPersistFlow = DataPersistenceFlow(dataOutputMap)
        val persistFuture = host.startFlow(dataPersistFlow)
        network.runNetwork()


        val dataRetrievalFlow = DataRetrievalFlow("stateref1")
        val retrievalFuture = host.startFlow(dataRetrievalFlow)
        network.runNetwork()
        val listOfDataOutputs = retrievalFuture.get() ?: return

        assertEquals(3, listOfDataOutputs.size)

        listOfDataOutputs.forEach { (publicKey, providerInput) -> println("$publicKey && $providerInput") }
    }

}