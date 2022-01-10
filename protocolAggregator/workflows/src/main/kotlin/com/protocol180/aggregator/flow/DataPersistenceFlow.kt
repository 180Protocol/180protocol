package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker


@InitiatingFlow
@StartableByRPC
class DataPersistenceFlow(private val dataOutputMap: Map<String, Map<String, ByteArray>>) :
        FlowLogic<Unit>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call() {


        val databaseService = serviceHub.cordaService(ProviderDBStoreService::class.java)

        // Persisting State Ref pointer & payload(Byte array) into Table
        dataOutputMap.forEach { (stateRef, providerInputMap) -> databaseService.addProviderInputWithStateRef(stateRef, providerInputMap) }

    }
}

@InitiatingFlow
@StartableByRPC
class DataRetrievalFlow(private val stateRef: String) :
        FlowLogic<Map<String, ByteArray>?>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): Map<String, ByteArray>? {


        val databaseService = serviceHub.cordaService(ProviderDBStoreService::class.java)

        // Retrieving the Record from the schema created for persisting data

        return databaseService.retrieveProviderInputForStateRef(stateRef)
    }
}

