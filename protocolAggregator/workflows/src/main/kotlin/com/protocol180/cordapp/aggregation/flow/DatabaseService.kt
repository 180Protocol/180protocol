package com.protocol180.cordapp.aggregation.flow

import com.protocol180.aggregator.schema.ProviderInputSchemaV1
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken


/**
 * A database service subclass for handling a table used for persisting encrypted Provider inputs.
 *
 * @param services The node's service hub.
 */
@CordaService
class DatabaseService(val services: ServiceHub) : SingletonSerializeAsToken() {

    /**
     * Adds a List of Provider Input into database table for particular transaction.
     */
    fun addProviderInputWithStateRef(stateRef: String, providerInputMap: Map<String, ByteArray>) {
        val providerList: MutableList<ProviderInputSchemaV1.ProviderInput> = mutableListOf()
        providerInputMap.forEach { (publicKey, value) -> providerList.add(ProviderInputSchemaV1.ProviderInput(publicKey, value)) }
        val dataOutput = ProviderInputSchemaV1.DataOutput(stateRef, providerList)
        services.withEntityManager {
            persist(dataOutput)
        }
    }

    /**
     * Retrieve the list of Provider Inputs for that particular transaction
     */
    fun retrieveProviderInputForStateRef(stateRef: String): Map<String, ByteArray>? {
        var result: MutableList<ProviderInputSchemaV1.DataOutput>? = null
        services.withEntityManager {
            val query = criteriaBuilder.createQuery(ProviderInputSchemaV1.DataOutput::class.java)
            val type = query.from(ProviderInputSchemaV1.DataOutput::class.java)
            query.select(type).where(criteriaBuilder.equal(type.get<Set<String>>("stateRef"), stateRef))
            result = createQuery(query).resultList
        }

        if (result?.size == 0)
            return null

        val dataOutput = result?.get(0)

        return dataOutput?.providerInputs?.map { it.publicKey to it.input }?.toMap()
    }

}