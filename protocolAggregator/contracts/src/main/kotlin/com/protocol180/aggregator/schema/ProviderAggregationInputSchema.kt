package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import java.io.Serializable
import javax.persistence.*

object ProviderAggregationInputSchema

object ProviderAggregationInputSchemaV1 : MappedSchema(
    schemaFamily = ProviderAggregationInputSchema.javaClass,
    version = 1,
    mappedTypes = listOf(ProviderAggregationInput::class.java)
) {
    @Entity
    @Table(name = "PROVIDER_AGGREGATION_INPUT")
    class ProviderAggregationInput(
        @Id
        @Column(name = "dataType", nullable = false, unique = true)
        var dataType: String,
        @Lob
        @Column(name = "input", nullable = false)
        val input: ByteArray,
        @Column(name = "storageType", nullable = false)
        var storageType: String,
        @Column(name = "cid", nullable = true)
        var cid: String,
        @Column(name = "encryptionKeyId", nullable = true)
        var encryptionKeyId: String
    ) : Serializable {
        constructor() : this("", ByteArray(0), "", "", "")
    }
}