package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.ProvenanceAggreagationContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StaticPointer
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party
import java.util.*

@BelongsToContract(ProvenanceAggreagationContract::class)
data class RewardsState(val provider: AnonymousParty,
                        val host: Party,
                        val rewards: ByteArray,
                        val dateCreated: Date,
                        val pointedToState: StaticPointer<ProviderAggregationState>) : ContractState {

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  consumer or host.
     */
    override val participants: List<AbstractParty> get() = listOf(host, provider)

}
