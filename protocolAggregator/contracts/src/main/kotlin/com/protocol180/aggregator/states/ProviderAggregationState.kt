package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.AggregationContract
import com.protocol180.commons.SchemaType
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StaticPointer
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party

@BelongsToContract(AggregationContract::class)
data class ProviderAggregationState(val provider: AnonymousParty,
                                    val host: Party,
                                    val failedReason:String,
                                    val dataInput: ByteArray,
                                    val pointedToConsumerAggregationState: StaticPointer<ConsumerAggregationState>,
                                    val pointedToRewardsState: StaticPointer<RewardsState>) : ContractState {

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  provider or host.
     */
    override val participants: List<AbstractParty> get() = listOf(provider, host)

}