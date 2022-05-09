package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.utils.AESUtil
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker


/**
 * DecentralizedStorageEncryptionKeyFlow allows storing secret key and ivParameterSpec into db.
 */

@InitiatingFlow
@StartableByRPC
class DecentralizedStorageEncryptionKeyUpdateFlow() : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val decentralizedStorageEncryptionKeyService =
            serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val azureKeyVaultService = AzureKeyVaultService();
        val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
        val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
        val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
        val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;

        val dek = AESUtil.convertSecretKeyToBytes(AESUtil.generateKey(256));
        val ivParameterSpec = AESUtil.generateIv()
        val encryptedDek = azureKeyVaultService.wrapKey(tenantId, clientId, clientSecret, keyIdentifier, dek);

        return decentralizedStorageEncryptionKeyService.addDecentralizedStorageEncryptionKeyWithFlowId(
            this.runId.uuid.toString(),
            encryptedDek,
            ivParameterSpec.iv
        )
    }
}