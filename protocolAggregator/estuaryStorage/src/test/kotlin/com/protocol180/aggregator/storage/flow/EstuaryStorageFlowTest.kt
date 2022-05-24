package com.protocol180.aggregator.storage.flow

import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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
    fun estuaryStorageEncryptionDecryptionFlowFailTest() {
        val token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.
        val updateFlow = EstuaryStorageFlow(token);
        val future = consumer.startFlow(updateFlow);
        assertFailsWith(EstuaryStorageFlowException::class) { future.getOrThrow() }
        network.runNetwork()
    }

    @Test
    fun estuaryStorageEncryptionDecryptionFlowTest() {
        val token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.
        val updateFlow = EstuaryStorageFlow(token);
        val future = consumer.startFlow(updateFlow);
        val encoded: ByteArray = Files.readAllBytes(Paths.get(File("document.decrypted").path));
        assertTrue(encoded.isNotEmpty())
        println("Download file byte array: $encoded")
        network.runNetwork()
    }
}