package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import java.io.Serializable
import java.time.Instant
import javax.persistence.*

object DecentralizedStorageEncryptionKeySchema

object DecentralizedStorageEncryptionKeySchemaV1 : MappedSchema(
    schemaFamily = DecentralizedStorageEncryptionKeySchema.javaClass,
    version = 1,
    mappedTypes = listOf(EncryptionKeyStorage::class.java)
) {
    @Entity
    @Table(name = "DECENTRALIZED_STORAGE_ENCRYPTION_KEY")
    class EncryptionKeyStorage(
        @Id
        @Column(name = "flow_id", nullable = false, unique = true)
        var flowId: String,
        @Lob
        @Column(name = "key", nullable = false)
        val key: String,
        @Column(name = "ivParameterSpec", nullable = false)
        val ivParameterSpec: ByteArray,
        @Column(name = "dateCreated", nullable = false)
        val dateCreated: Instant
    ) : Serializable {
        constructor() : this("", "", ByteArray(0), Instant.now())
    }

}