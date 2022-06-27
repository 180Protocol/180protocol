package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.internal.readFully
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

@InitiatingFlow
@StartableByRPC
open class ProviderAggregationInputFlow(
    private val file: File,
    private val dataType: String,
    private val storageType: String,
    private val encryptionKeyId: String
) : FlowLogic<Unit>() {
    companion object {
        private val log = loggerFor<ProviderAggregationInputFlow>()
    }

    override val progressTracker = ProgressTracker()

    open fun storeData(aggregationInputBytes: ByteArray, encryptionKeyId: String) {
        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)

        return providerDbStoreService.addProviderAggregationInput(
            dataType,
            aggregationInputBytes,
            storageType,
            "",
            ""
        )
    }

    @Suspendable
    override fun call() {
        val aggregationInputBytes: ByteArray = file.readBytes();
        return storeData(aggregationInputBytes, encryptionKeyId);
    }
}