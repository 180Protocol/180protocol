package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker


/**
 * This is the flow which retrieves the data aggregation outputs stored on a Consumer's node. The consumer receives the
 * data output received from the enclave via the host in encrypted form during the [ConsumerAggregationFlow], decrypts it
 * and stores it in its local database using the flowId of the [ConsumerAggregationFlow] as the unique id.
 * Flow id generated during the consumer aggregation flow needs to be passed to retrieve the appropriate output from the Vault.
 * The data is then encoded from Avro format into JSON using Avro's JsonEncoder.
 */
@StartableByRPC
class ConsumerDataOutputRetrievalFlow(
    private val flowId: String
) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): String {
        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val consumer = ourIdentity
        return enclaveClientService.readJsonFromOutputBytesAndSchema(
            consumerDbStoreService.retrieveConsumerDataOutputWithFlowId(
                flowId
            )!!, "aggregate"
        ).toString()
    }
}