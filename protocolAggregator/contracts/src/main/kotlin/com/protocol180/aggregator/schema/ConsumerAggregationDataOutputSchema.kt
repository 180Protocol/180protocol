package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table

/**
 * Schema for storing Consumer Data Outputs generated from
 * @see com.protocol180.aggregator.flow.ConsumerDataOutputRetrievalFlow
 */
object ConsumerAggregationDataOutputSchema

object ConsumerAggregationDataOutputSchemaV1 : MappedSchema(schemaFamily = ConsumerAggregationDataOutputSchema.javaClass,
        version = 1,
        mappedTypes = listOf(ConsumerDataOutput::class.java)) {
    @Entity
    @Table(name = "CONSUMER_DATA_OUTPUT")
    class ConsumerDataOutput(@Id
                             @Column(name = "flow_id", nullable = false, unique = true)
                             var flowId: String,
                             @Lob
                             @Column(name = "consumer_data_output_bytes", nullable = false)
                             val consumerDataOutputBytes: ByteArray,
                             @Column(name = "consumer_output_data_type", nullable = false)
                             val consumerOutputDataType: String
    ) : Serializable {
        constructor() : this("", ByteArray(0), "")
    }

}