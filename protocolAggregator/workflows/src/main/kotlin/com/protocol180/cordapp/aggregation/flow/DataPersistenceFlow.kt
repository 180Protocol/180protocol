package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.schema.ProviderInputSchemaV1
import com.protocol180.aggregator.states.DataOutputState
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.StaticPointer
import net.corda.core.contracts.hash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.utilities.ProgressTracker
import java.util.*


@InitiatingFlow
@StartableByRPC
class DataPersistenceFlow(val data: ByteArray) :
        FlowLogic<MutableList<ProviderInputSchemaV1.PersistentProviderInput>?>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): MutableList<ProviderInputSchemaV1.PersistentProviderInput>? {

        val pointedToState: StaticPointer<DataOutputState>

        val consumer = ourIdentity

        var dataOutputState = DataOutputState(consumer.anonymise(), consumer, data, Date())

        pointedToState =
                StaticPointer<DataOutputState>(StateRef(dataOutputState.hash(), 0), DataOutputState::class.java)


        // Persisting State Ref pointer & payload(Byte array) into Table
        val providerInputData = ProviderInputSchemaV1.PersistentProviderInput(pointedToState.toString(), data)
        serviceHub.withEntityManager {
            persist(providerInputData)
        }

        println(pointedToState.toString() + " && " + data.toString())

        // Retrieving the Record from the schema created for persisting data
        var result: MutableList<ProviderInputSchemaV1.PersistentProviderInput>? = null
        serviceHub.withEntityManager {
            val query = criteriaBuilder.createQuery(ProviderInputSchemaV1.PersistentProviderInput::class.java)
            val type = query.from(ProviderInputSchemaV1.PersistentProviderInput::class.java)
            query.select(type)
            result = createQuery(query).resultList
        }

        return result
    }
}

