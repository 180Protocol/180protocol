package com.protocol180.aggregator.storage.flow

import com.protocol180.aggregator.schema.DecentralizedStorageEncryptionKeySchemaV1;
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.time.Instant

/**
 * Service for Decentralized Storage Encryption Key State vault queries used in the flows.
 */
@CordaService
class DecentralizedStorageEncryptionKeyService(val services: AppServiceHub) : SingletonSerializeAsToken() {
    /**
     * Adds a decentralized storage encryption key into encryption key db store.
     */
    fun addDecentralizedStorageEncryptionKeyWithFlowId(
        flowId: String,
        key: ByteArray,
        ivParameterSpec: ByteArray
    ) {
        val decentralizedStorageEncryptionKey =
            DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage(flowId, key, ivParameterSpec, Instant.now())
        services.withEntityManager {
            persist(decentralizedStorageEncryptionKey)
        }
    }

    /**
     * Retrieves a decentralized storage encryption key using flow id from encryption key db store.
     */
    fun retrieveDecentralizedStorageEncryptionKeyWithFlowId(flowId: String): DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage? {
        var result: MutableList<DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage>? = null
        services.withEntityManager {
            val query =
                criteriaBuilder.createQuery(DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage::class.java)
            val type = query.from(DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage::class.java)
            query.select(type).where(criteriaBuilder.equal(type.get<Set<String>>("flowId"), flowId))
            result = createQuery(query).resultList
        }
        if (result?.size == 0)
            return null

        return result?.get(0)
    }

    /**
     * Retrieves the latest decentralized storage encryption key from encryption key db store.
     */
    fun retrieveLatestDecentralizedStorageEncryptionKey(): DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage? {
        var result: MutableList<DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage>? = null
        services.withEntityManager {
            val query =
                criteriaBuilder.createQuery(DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage::class.java)
            val type = query.from(DecentralizedStorageEncryptionKeySchemaV1.EncryptionKeyStorage::class.java)
            query.select(type).orderBy(criteriaBuilder.desc(type.get<Instant>("dateCreated")));
            result = createQuery(query).resultList
        }
        if (result?.size == 0)
            return null

        return result?.get(0);
    }
}