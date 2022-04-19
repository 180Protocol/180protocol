package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.utils.AESUtil
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import java.io.File
import javax.crypto.spec.IvParameterSpec
import com.protocol180.aggregator.storage.EstuaryStorageService;


/**
 * This is the flow which retrieves the data aggregation outputs stored on a Consumer's node. The consumer receives the
 * data output received from the enclave via the host in encrypted form during the [ConsumerAggregationFlow], decrypts it
 * and stores it in its local database using the flowId of the [ConsumerAggregationFlow] as the unique id.
 * Flow id generated during the consumer aggregation flow needs to be passed to retrieve the appropriate output from the Vault.
 * The data is then encoded from Avro format into JSON using Avro's JsonEncoder.
 */
@StartableByRPC
class ConsumerDataOutputRetrievalFlow(private val flowId: String, private val storageType: String,  private val cid: String, private val encryptionKeyId: String) : FlowLogic<String>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): String {

        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val decentralizedStorageEncryptionKeyService = serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val estuaryStorageService = serviceHub.cordaService(EstuaryStorageService::class.java);
        val consumer = ourIdentity

        if (storageType === "local") {
            return enclaveClientService.readJsonFromOutputBytesAndSchema(
                consumerDbStoreService.retrieveConsumerDataOutputWithFlowId(
                    flowId
                )!!, "aggregate"
            ).toString()
        } else {
            estuaryStorageService.downloadFileFromEstuary(cid);
            val decentralizedStorageEncryptionKeyRecord = decentralizedStorageEncryptionKeyService.retrieveDecentralizedStorageEncryptionKeyWithFlowId(encryptionKeyId);
            val downloadedFile = File(File("downloaded.encrypted").path);
            return enclaveClientService.readJsonFromOutputBytesAndSchema(
                AESUtil.decryptFile(AESUtil.convertStringToSecretKey(decentralizedStorageEncryptionKeyRecord!!.key), IvParameterSpec(decentralizedStorageEncryptionKeyRecord!!.ivParameterSpec), downloadedFile)!!,
                "aggregate"
            ).toString();
        }
    }
}