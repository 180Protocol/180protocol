package com.protocol180.aggregator.storage.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.flow.NetworkParticipantService
import com.protocol180.aggregator.storage.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.storage.utils.AESUtil
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker


/**
 * DecentralizedStorageEncryptionKeyFlow allows storing secret key and ivParameterSpec into db.
 */

@InitiatingFlow
@StartableByRPC
class DecentralizedStorageEncryptionKeyUpdateFlow() : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val decentralizedStorageEncryptionKeyService =
            serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val azureKeyVaultService = serviceHub.cordaService(AzureKeyVaultService::class.java)
        val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
        val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
        val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
        val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;

        val dek = AESUtil.convertSecretKeyToBytes(AESUtil.generateKey(256));
        val ivParameterSpec = AESUtil.generateIv()
        val encryptedDek = azureKeyVaultService.wrapKey(tenantId, clientId, clientSecret, keyIdentifier, dek);

        decentralizedStorageEncryptionKeyService.addDecentralizedStorageEncryptionKeyWithFlowId(
            this.runId.uuid.toString(),
            encryptedDek,
            ivParameterSpec.iv
        )

        return this.runId.uuid.toString()
    }
}