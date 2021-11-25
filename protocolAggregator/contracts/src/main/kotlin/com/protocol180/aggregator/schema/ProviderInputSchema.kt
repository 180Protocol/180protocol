package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

object ProviderInputSchema

object ProviderInputSchemaV1 : MappedSchema(schemaFamily = ProviderInputSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentProviderInput::class.java)) {
    @Entity
    @Table(name = "provider_input")
    class PersistentProviderInput(@Id
                                  @Column(name = "state_ref")
                                  var stateRef: String,
                                  @Column(name = "payload")
                                  var payload: ByteArray
    ) {
        constructor() : this(
                UUID.randomUUID().toString(), "default".toByteArray()
        )
    }
}