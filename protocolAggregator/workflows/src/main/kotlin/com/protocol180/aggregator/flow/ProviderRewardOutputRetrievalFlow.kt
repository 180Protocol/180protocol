package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import org.apache.avro.generic.GenericRecord
import java.util.*

/**
 * This is the flow which Retrieves Reward output stored on that Provider node for particular flow-id(Aggregation Session) .
 * Flow id used into consumer aggregation needs to be passed to retrieve the appropriate reward output from Vault.
 */
@StartableByRPC
class ProviderRewardOutputRetrievalFlow(private val flowId: String) : FlowLogic<List<GenericRecord?>>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): ArrayList<GenericRecord?> {

        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val provider = ourIdentity

        return enclaveClientService.readGenericRecordsFromOutputBytesAndSchema(providerDbStoreService.retrieveRewardResponseWithFlowId(flowId)!!, "rewards")
    }
}