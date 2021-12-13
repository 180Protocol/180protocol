package com.protocol180.cordapp.aggregation.flow

import com.protocol180.aggregator.schema.EnclavePostOfficeSchemaV1
import com.protocol180.aggregator.schema.ProviderInputSchemaV1
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

/**
 * A database service subclass for handling a table used for persisting post office object created with help of attestation & Curve25519PrivateKey for
 * client node to enclave communication
 * @param services The node's service hub.
 */
@CordaService
class PostOfficeDbStoreService(val services: ServiceHub) : SingletonSerializeAsToken() {

    /**
     * Adds a Post Office to database table with unique Flow Id.
     */
    fun addPostOfficeWithFlowId(flowId: String, postOfficeByteArray: ByteArray) {
        val postOfficeByteArray = EnclavePostOfficeSchemaV1.EnclavePostOffice(flowId,postOfficeByteArray)
        services.withEntityManager {
            persist(postOfficeByteArray)
        }
    }

    /**
     * Retrieve the Post office from database with unique Flow Id provided.
     */
    fun retrievePostOfficeWithFlowId(flowId: String): ByteArray? {
        var result: MutableList<EnclavePostOfficeSchemaV1.EnclavePostOffice>? = null
        services.withEntityManager {
            val query = criteriaBuilder.createQuery(EnclavePostOfficeSchemaV1.EnclavePostOffice::class.java)
            val type = query.from(EnclavePostOfficeSchemaV1.EnclavePostOffice::class.java)
            query.select(type).where(criteriaBuilder.equal(type.get<Set<String>>("flowId"), flowId))
            result = createQuery(query).resultList
        }

        if (result?.size == 0)
            return null

        val postOffice = result?.get(0)

        return postOffice?.postOfficeBytes
    }

}