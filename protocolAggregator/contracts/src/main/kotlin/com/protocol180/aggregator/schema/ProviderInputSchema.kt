package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import java.io.Serializable
import javax.persistence.*

/**
 * Schema for storing Provider Data Inputs to be used during event based rewards
 * @see com.protocol180.aggregator.flow.ConsumerAggregationFlow
 */
object ProviderInputSchema

object ProviderInputSchemaV1 : MappedSchema(schemaFamily = ProviderInputSchema.javaClass,
        version = 1,
        mappedTypes = listOf(ProviderInput::class.java, DataOutput::class.java)) {
    @Entity
    @Table(name = "PROVIDER_INPUT")
    class ProviderInput(@Id
                        @GeneratedValue(strategy = GenerationType.IDENTITY)
                        val id: Int = 0,
                        @Column(name = "public_key", nullable = false)
                        var publicKey: String,
                        @Column(name = "input", nullable = false)
                        val input: ByteArray
    ) : Serializable {
        constructor() : this(0, "", ByteArray(0))
        constructor(publicKey: String, providerInput: ByteArray) : this(
                0, publicKey, providerInput)
    }

    @Entity
    @Table(name = "DATA_OUTPUT")
    class DataOutput(@Id
                     @Column(name = "state_ref", nullable = false, unique = true)
                     val stateRef: String,
                     @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
                     val providerInputs: List<ProviderInput>
    ) : Serializable {
        constructor() : this("", listOf())

    }
}