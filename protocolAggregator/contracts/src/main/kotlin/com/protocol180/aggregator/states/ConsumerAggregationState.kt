package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.ConsumerAggregationContract
import com.protocol180.commons.SchemaType
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StaticPointer
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(ConsumerAggregationContract::class)
data class ConsumerAggregationState(
    val consumer: AnonymousParty,
    val host: Party,
    var failedReason: String?,
    var pointedToProviderAggregationState: List<StaticPointer<ProviderAggregationState>>?,
    var pointedToDataOutputState: StaticPointer<DataOutputState>?
) : ContractState   {

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  consumer or host.
     */
    override val participants: List<AbstractParty> get() = listOf(consumer, host)



}
