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
    fun flowWritesAndRetrievesFromSchemaTableCorrectly() {
        val flow = DataPersistenceFlow("Test Value 1".toByteArray())
        val future = host.startFlow(flow)
        network.runNetwork()
        val listOfProviderInputs = future.get() ?: return

        assertEquals(1, listOfProviderInputs.size)

        assertEquals("Test Value 1", String(listOfProviderInputs[0].payload))

        listOfProviderInputs.forEach{ println(String(it.payload) + " & " + it.stateRef) }
    }

}