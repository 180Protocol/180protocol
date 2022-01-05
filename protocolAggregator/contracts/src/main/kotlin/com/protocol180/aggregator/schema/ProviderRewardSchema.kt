package com.protocol180.aggregator.schema

import net.corda.core.schemas.MappedSchema
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


object ProviderRewardSchema

object ProviderRewardSchemaV1 : MappedSchema(schemaFamily = ProviderRewardSchema.javaClass,
        version = 1,
        mappedTypes = listOf(ProviderReward::class.java)) {
    @Entity
    @Table(name = "PROVIDER_REWARD")
    class ProviderReward(@Id
                         @Column(name = "flow_id", nullable = false, unique = true)
                         var flowId: String,
                         @Column(name = "reward_generic_record_bytes", nullable = false)
                         val rewardGenericRecordBytes: ByteArray,
                         @Column(name = "reward_output_data_type", nullable = false)
                         val rewardOutputDataType: String
    ) {
        constructor() : this("", ByteArray(0), "")
    }

}