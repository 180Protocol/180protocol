package com.protocol180.aggregator.storage.flow

import net.corda.core.internal.readFully
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.AfterEach
import java.io.File
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

    @AfterEach
    fun deleteFiles() {
        val encryptedFile = File("src/test/resources/document.encrypted");
        val decryptedFile = File("src/test/resources/document.decrypted");
        val downloadedFile = File("downloaded.encrypted");
        encryptedFile.delete();
        decryptedFile.delete();
        downloadedFile.delete();
    }

    @Test
    fun estuaryStorageEncryptionDecryptionFlowFailTest() {
        val token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.
        val failFlow = EstuaryStorageFlow(token);
        val future = consumer.startFlow(failFlow);
        assertFailsWith(EstuaryStorageFlowException::class) { future.getOrThrow() }
        network.runNetwork()
    }

    @Test
    fun estuaryStorageEncryptionDecryptionFlowTest() {
        val token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.
        val successFlow = EstuaryStorageFlow(token);
        val future = consumer.startFlow(successFlow);
        val encoded: ByteArray = ClassLoader.getSystemClassLoader().getResourceAsStream("document.decrypted").readFully();
        assertTrue(encoded.isNotEmpty())
        println("Download file byte array: $encoded")
        network.runNetwork()
    }
}