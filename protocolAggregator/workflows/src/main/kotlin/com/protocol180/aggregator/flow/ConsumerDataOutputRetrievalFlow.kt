package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.storage.EstuaryStorageService
import com.protocol180.aggregator.utils.AESUtil
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.spec.IvParameterSpec


/**
 * This is the flow which retrieves the data aggregation outputs stored on a Consumer's node. The consumer receives the
 * data output received from the enclave via the host in encrypted form during the [ConsumerAggregationFlow], decrypts it
 * and stores it in its local database using the flowId of the [ConsumerAggregationFlow] as the unique id.
 * Flow id generated during the consumer aggregation flow needs to be passed to retrieve the appropriate output from the Vault.
 * The data is then encoded from Avro format into JSON using Avro's JsonEncoder.
 */
@StartableByRPC
class ConsumerDataOutputRetrievalFlow(
    private val flowId: String,
    private val storageType: String,
    private val cid: String? = null,
    private val encryptionKeyId: String? = null
) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): String {

        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val decentralizedStorageEncryptionKeyService =
            serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val estuaryStorageService = EstuaryStorageService();
        val azureKeyVaultService = AzureKeyVaultService();
        val consumer = ourIdentity
        if (storageType == "local") {
            return enclaveClientService.readJsonFromOutputBytesAndSchema(
                consumerDbStoreService.retrieveConsumerDataOutputWithFlowId(
                    flowId
                )!!, "aggregate"
            ).toString()
        } else if (storageType == "filecoin") {
            val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
            val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
            val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
            val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;
            estuaryStorageService.downloadFileFromEstuary(cid);
            val decentralizedStorageEncryptionKeyRecord =
                encryptionKeyId?.let {
                    decentralizedStorageEncryptionKeyService.retrieveDecentralizedStorageEncryptionKeyWithFlowId(
                        it
                    )
                };
            val downloadedFile = File(File("downloaded.encrypted").path);
            val decryptedFile = File(File("document.decrypted").path);
            val decryptedDek = azureKeyVaultService.unWrapKey(
                tenantId,
                clientId,
                clientSecret,
                keyIdentifier,
                decentralizedStorageEncryptionKeyRecord!!.key
            );
            AESUtil.decryptFile(
                AESUtil.convertBytesToSecretKey(decryptedDek), IvParameterSpec(
                    decentralizedStorageEncryptionKeyRecord.ivParameterSpec
                ), downloadedFile, decryptedFile
            );
            val encoded: ByteArray = Files.readAllBytes(Paths.get(File("document.decrypted").path))
            downloadedFile.delete();
            decryptedFile.delete();
            return enclaveClientService.readJsonFromOutputBytesAndSchema(
                encoded,
                "aggregate"
            ).toString();
        } else {
            throw Error("Given storage type is not supported.");
        }
    }
}