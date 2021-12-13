package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

object EnclavePostOfficeSchema

object EnclavePostOfficeSchemaV1 : MappedSchema(schemaFamily = EnclavePostOfficeSchema.javaClass,
        version = 1,
        mappedTypes = listOf(EnclavePostOfficeSchema::class.java)) {
    @Entity
    @Table(name = "ENCLAVE_POST_OFFICE")
    class EnclavePostOffice(@Id
                            @Column(name = "flow_id", nullable = false, unique = true)
                            val flowId: String,
                            @Column(name = "post_office_bytes", nullable = false)
                            val postOfficeBytes: ByteArray) {
        constructor() : this(
                "", ByteArray(0))
    }


}