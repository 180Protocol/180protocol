package com.protocol180.aggregator.storage.flow

import com.google.common.collect.ImmutableList
import net.corda.core.node.AppServiceHub
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class EstuaryStorageFlowTest {
    lateinit var network: MockNetwork
    lateinit var consumer: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.protocol180.aggregator.storage")
                )
            )
        )
        consumer = network.createPartyNode(null);
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun estuaryStorageEncryptionDecryptionFlowTest() {
        val token = "EST4d1f0d52-6e39-4982-8f43-a5222af1ff86ARY"; // Api key to authenticate estuary apis.
        val updateFlow = EstuaryStorageFlow(token);
        consumer.startFlow(updateFlow)
        network.runNetwork()
    }
}