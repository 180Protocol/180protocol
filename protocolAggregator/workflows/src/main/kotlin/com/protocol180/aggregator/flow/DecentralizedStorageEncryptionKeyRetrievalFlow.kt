package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker


/**
 * DecentralizedStorageEncryptionKeyFlow allows storing secret key and ivParameterSpec into db.
 */

@InitiatingFlow
@StartableByRPC
class DecentralizedStorageEncryptionKeyRetrievalFlow : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val decentralizedStorageEncryptionKeyService =
            serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)

        val decentralizedStorageEncryptionKeyRecord =
            decentralizedStorageEncryptionKeyService.retrieveLatestDecentralizedStorageEncryptionKey();
        return decentralizedStorageEncryptionKeyRecord!!.key;
    }
}