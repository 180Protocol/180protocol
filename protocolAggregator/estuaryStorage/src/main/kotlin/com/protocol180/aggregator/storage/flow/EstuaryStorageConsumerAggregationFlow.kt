package com.protocol180.aggregator.storage.flow

import com.protocol180.aggregator.flow.*
import com.protocol180.aggregator.states.DataOutputState
import com.protocol180.aggregator.storage.estuary.EstuaryStorageService
import com.protocol180.aggregator.storage.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.storage.utils.AESUtil
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.io.File
import javax.crypto.spec.IvParameterSpec

/**
 * One of 180Protocol's supported Broker Flows. There will be more flow types in the future which are provider initiated instead
 * of consumer initiated. ConsumerAggregationFlow allows consumers in coalitions to initiate a data aggregation request. The flow accepts
 * a supported dataType as an argument and initiates a request to the coalition host to perform an aggregation by
 * requesting data from providers in the network. The flow queries and checks the CoalitionConfigurationState to
 * determine whether the requested dataType is valid and supported by the coalition.* Consumer receives an enclave attestation
 * from the host to verify the enclaves validity and utilize the same for decrypting enclave generated encrypted data outputs.
 * Consumer then creates a [DataOutputState], transacting and signing with the host to store on their respective ledgers
 * as a proof of data aggregation. The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@StartableByRPC
class EstuaryStorageConsumerAggregationFlow(
    private val dataType: String,
    private val description: String,
    private val storageType: String
) : ConsumerAggregationFlow(dataType, description, storageType) {

    companion object {
        private val log = loggerFor<EstuaryStorageConsumerAggregationFlow>()
    }

    override val progressTracker = ProgressTracker()

    override fun storeData(decryptedAggregationDataRecordBytes: ByteArray, flowId: String): Pair<String, String> {
        val decentralizedStorageEncryptionKeyService = serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val estuaryStorageService = serviceHub.cordaService(EstuaryStorageService::class.java);
        val azureKeyVaultService = serviceHub.cordaService(AzureKeyVaultService::class.java);
        val token = serviceHub.cordaService(NetworkParticipantService::class.java).token;
        val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
        val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
        val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
        val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;

        val decentralizedStorageEncryptionKeyRecord =
            decentralizedStorageEncryptionKeyService.retrieveLatestDecentralizedStorageEncryptionKey();
        val encryptedFile = File("document.encrypted")
        val decryptedDek = azureKeyVaultService.unWrapKey(tenantId, clientId, clientSecret, keyIdentifier, decentralizedStorageEncryptionKeyRecord!!.key);
        AESUtil.encryptFile(
            AESUtil.convertBytesToSecretKey(decryptedDek),
            IvParameterSpec(decentralizedStorageEncryptionKeyRecord!!.ivParameterSpec),
            decryptedAggregationDataRecordBytes,
            encryptedFile
        )
        val uploadFile = File(File("document.encrypted").path)
        val cid = estuaryStorageService.uploadContent(uploadFile, token)
        val encryptionKeyId = decentralizedStorageEncryptionKeyRecord!!.flowId;
        encryptedFile.delete();

        return Pair(encryptionKeyId, cid);
    }
}