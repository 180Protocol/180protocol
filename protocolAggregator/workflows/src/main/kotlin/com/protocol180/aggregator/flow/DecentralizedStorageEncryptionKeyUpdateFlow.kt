package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
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
class DecentralizedStorageEncryptionKeyUpdateFlow(private val key: String) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val decentralizedStorageEncryptionKeyService =
            serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)

        val kek = AESUtil.convertStringToSecretKey(key);
        val dek = AESUtil.convertSecretKeyToString(AESUtil.generateKey(256));
        val ivParameterSpec = AESUtil.generateIv()
        val encryptedDek = AESUtil.encrypt(dek, kek, ivParameterSpec)

        return decentralizedStorageEncryptionKeyService.addDecentralizedStorageEncryptionKeyWithFlowId(
            this.runId.uuid.toString(),
            encryptedDek,
            ivParameterSpec.iv
        )
    }
}