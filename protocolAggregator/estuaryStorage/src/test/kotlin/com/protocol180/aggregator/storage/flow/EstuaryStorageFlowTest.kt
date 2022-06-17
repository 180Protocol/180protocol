package com.protocol180.aggregator.storage.flow

import net.corda.core.internal.readFully
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
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
        deleteFiles()
        network.stopNodes()
    }

    private fun deleteFiles() {
        try {
            val encryptedFile = Paths.get(File("document.encrypted").path);
            val downloadedDecryptedFile = Paths.get(File("downloaded.decrypted").path);
            val downloadedEncryptedFile = Paths.get(File("downloaded.encrypted").path);
            val encryptedFileDeleteResult = Files.deleteIfExists(encryptedFile);
            if (encryptedFileDeleteResult) {
                println("Encrypted file deleted successfully.")
            } else {
                println("Encrypted file deletion failed.")
            }
            val decryptedFileDeleteResult = Files.deleteIfExists(downloadedDecryptedFile);
            if (decryptedFileDeleteResult) {
                println("Decrypted file deleted successfully.")
            } else {
                println("Decrypted file deletion failed.")
            }
            val downloadedFileDeleteResult = Files.deleteIfExists(downloadedEncryptedFile);
            if (downloadedFileDeleteResult) {
                println("Download file deleted successfully.")
            } else {
                println("Download file deletion failed.")
            }
        }catch (e: IOException) {
            println("Deletion failed.")
            e.printStackTrace()
        }
    }

    @Test
    fun estuaryStorageEncryptionDecryptionFlowFailTest() {
        val token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.
        val failFlow = EstuaryStorageFlow(token)
        val future = consumer.startFlow(failFlow)
        network.runNetwork()
        assertFailsWith(EstuaryStorageFlowException::class) { future.getOrThrow() }
        deleteFiles()
    }

    @Test
    fun estuaryStorageEncryptionDecryptionFlowTest() {
        val token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.
        val successFlow = EstuaryStorageFlow(token)
        val future = consumer.startFlow(successFlow)
        network.runNetwork()
        //val encoded: ByteArray = ClassLoader.getSystemClassLoader().getResourceAsStream("downloaded.decrypted").readFully()
        val encoded: ByteArray = Files.readAllBytes(Paths.get("downloaded.decrypted"))
        assertTrue(encoded.isNotEmpty())
        println("Download file byte array: $encoded")
        deleteFiles()
    }
}