package com.protocol180.aggregator.storage.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.storage.estuary.EstuaryStorageService
import com.protocol180.aggregator.storage.utils.AESUtil
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.internal.readFully
import net.corda.core.utilities.ProgressTracker
import org.json.JSONArray
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.SecretKey


/**
 * EstuaryStorageFlow utilized to test EstuaryStorageService
 */

@InitiatingFlow
@StartableByRPC
class EstuaryStorageFlow(private val token: String) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val estuaryStorageService = serviceHub.cordaService(EstuaryStorageService::class.java)
        val inputFile = ClassLoader.getSystemClassLoader().getResourceAsStream("Provider2InputData.csv").readFully();
        val key = AESUtil.generateKey(256);
        val ivParameterSpec = AESUtil.generateIv();
        val encryptedFile = File("document.encrypted");
        val decryptedFile = File("document.decrypted");
        AESUtil.encryptFile(key, ivParameterSpec, inputFile, encryptedFile);
        val uploadFile = File("document.encrypted");
        val cid = estuaryStorageService.uploadContent(uploadFile, token);
        val contents = estuaryStorageService.fetchContent(token);
        estuaryStorageService.downloadFileFromEstuary(cid);
        val downloadedFile = File("downloaded.encrypted");
        AESUtil.decryptFile(key, ivParameterSpec, downloadedFile, decryptedFile);
    }
}