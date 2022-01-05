package com.protocol180.cordapp.aggregation.flow

import com.protocol180.aggregator.schema.ConsumerAggregationDataOutputSchemaV1
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken


/**
 * A database service subclass for handling a table used for persisting decrypted consumer aggregation data output bytes
 *
 * @param services The node's service hub.
 */
@CordaService
class ConsumerDBStoreService(val services: ServiceHub) : SingletonSerializeAsToken() {

    /**
     * Adds a decrypted Consumer Data Output Bytes received from enclave into consumer db store.
     */
    fun addConsumerDataOutputWithFlowId(flowId: String, consumerAggregationDataOutputResponseBytes: ByteArray, dataType: String) {
        val decryptedConsumerDataOutput = ConsumerAggregationDataOutputSchemaV1.ConsumerDataOutput(flowId, consumerAggregationDataOutputResponseBytes, dataType)
        services.withEntityManager {
            persist(decryptedConsumerDataOutput)
        }
    }

    /**
     * Retrieves a decrypted aggregation data output from consumer db store.
     */
    fun retrieveConsumerDataOutputWithFlowId(flowId: String): ByteArray? {
        var result: MutableList<ConsumerAggregationDataOutputSchemaV1.ConsumerDataOutput>? = null
        services.withEntityManager {
            val query = criteriaBuilder.createQuery(ConsumerAggregationDataOutputSchemaV1.ConsumerDataOutput::class.java)
            val type = query.from(ConsumerAggregationDataOutputSchemaV1.ConsumerDataOutput::class.java)
            query.select(type).where(criteriaBuilder.equal(type.get<Set<String>>("flowId"), flowId))
            result = createQuery(query).resultList
        }
        if (result?.size == 0)
            return null

        val dataOutputBytes = result?.get(0)

        return dataOutputBytes?.consumerDataOutputBytes
    }
}