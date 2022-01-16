package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker


/**
 * This is the flow which Retrieves Consumer aggregation output stored on that Consumer node .
 * Flow id used into consumer aggregation needs to be passed to retrieve the appropriate output from Vault.
 */
@StartableByRPC
class ConsumerDataOutputRetrievalFlow(private val flowId: String) : FlowLogic<String>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): String {

        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val consumer = ourIdentity

        return enclaveClientService.readJsonFromOutputBytesAndSchema(consumerDbStoreService.retrieveConsumerDataOutputWithFlowId(flowId)!!, "aggregate").toString()
    }
}