package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker

/**
 * This is the flow which retrieves the rewards stored on a Provider node. The provider receives the
 * rewards received from the enclave via the host in decrypted form during the [ConsumerAggregationFlow],  and stores
 * it in its local database using the flowId of the [ProviderAggregationResponseFlow] as the unique id. Flow id generated
 * during the provider aggregation response flow needs to be passed to retrieve the appropriate output from the Vault.
 * The data is then encoded from Avro format into JSON using Avro's JsonEncoder.
 */
@StartableByRPC
class ProviderRewardOutputRetrievalFlow(private val flowId: String) : FlowLogic<String>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): String {

        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val provider = ourIdentity

        return enclaveClientService.readJsonFromOutputBytesAndSchema(providerDbStoreService.retrieveRewardResponseWithFlowId(flowId)!!, "rewards").toString()
    }
}