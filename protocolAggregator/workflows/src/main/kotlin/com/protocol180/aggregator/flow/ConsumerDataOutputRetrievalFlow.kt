package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import org.apache.avro.generic.GenericRecord
import java.util.*


/**
 * This is the flow which Retrieves Consumer aggregation output stored on that Consumer node .
 * Flow id used into consumer aggregation needs to be passed to retrieve the appropriate output from Vault.
 */
@StartableByRPC
class ConsumerDataOutputRetrievalFlow(private val flowId: String) : FlowLogic<List<GenericRecord?>>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): ArrayList<GenericRecord?> {

        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val consumer = ourIdentity

        return enclaveClientService.readGenericRecordsFromOutputBytesAndSchema(consumerDbStoreService.retrieveConsumerDataOutputWithFlowId(flowId)!!, "aggregate")
    }
}