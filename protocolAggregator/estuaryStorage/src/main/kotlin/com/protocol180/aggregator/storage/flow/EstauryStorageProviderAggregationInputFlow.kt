package com.protocol180.aggregator.storage.flow

import com.protocol180.aggregator.flow.NetworkParticipantService
import com.protocol180.aggregator.flow.ProviderAggregationInputFlow
import com.protocol180.aggregator.flow.ProviderDBStoreService
import com.protocol180.aggregator.storage.estuary.EstuaryStorageService
import com.protocol180.aggregator.storage.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.storage.utils.AESUtil
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.io.File
import java.io.InputStream
import javax.crypto.spec.IvParameterSpec

@StartableByRPC
class EstauryStorageProviderAggregationInputFlow(
    private val file: ByteArray,
    private val dataType: String,
    private val storageType: String,
    private val encryptionKeyId: String
) : ProviderAggregationInputFlow(file, dataType, storageType, encryptionKeyId)  {
    companion object {
        private val log = loggerFor<EstuaryStorageConsumerAggregationFlow>()
    }

    override val progressTracker = ProgressTracker()

    override fun storeData(aggregationInputBytes: ByteArray, encryptionKeyId: String) {
        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val decentralizedStorageEncryptionKeyService = serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val estuaryStorageService = serviceHub.cordaService(EstuaryStorageService::class.java);
        val azureKeyVaultService = serviceHub.cordaService(AzureKeyVaultService::class.java);
        val token = serviceHub.cordaService(NetworkParticipantService::class.java).token;
        val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
        val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
        val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
        val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;

        val decentralizedStorageEncryptionKeyRecord =
            decentralizedStorageEncryptionKeyService.retrieveDecentralizedStorageEncryptionKeyWithFlowId(encryptionKeyId);
        val encryptedFile = File("document.encrypted")
        val decryptedDek = azureKeyVaultService.unWrapKey(tenantId, clientId, clientSecret, keyIdentifier, decentralizedStorageEncryptionKeyRecord!!.key);
        AESUtil.encryptFile(
            AESUtil.convertBytesToSecretKey(decryptedDek),
            IvParameterSpec(decentralizedStorageEncryptionKeyRecord!!.ivParameterSpec),
            aggregationInputBytes,
            encryptedFile
        )
        val uploadFile = File(File("document.encrypted").path)
        val cid = estuaryStorageService.uploadContent(uploadFile, token)
        val encryptionId = decentralizedStorageEncryptionKeyRecord!!.flowId;
        encryptedFile.delete();

        return providerDbStoreService.addProviderAggregationInput(
            dataType,
            aggregationInputBytes,
            storageType,
            cid,
            encryptionId
        )
    }
}