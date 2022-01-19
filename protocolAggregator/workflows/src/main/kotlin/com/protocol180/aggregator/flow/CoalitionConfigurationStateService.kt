package com.protocol180.aggregator.flow

import com.protocol180.aggregator.states.CoalitionConfigurationState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.SingletonSerializeAsToken

/**
 * Service for Coalition Configuration State vault queries used in the flows.
 */
@CordaService
class CoalitionConfigurationStateService(private val hub: AppServiceHub) : SingletonSerializeAsToken() {

    /**
     * Returns the coalition configuration state for a specific linear id
     */
    fun findCoalitionConfigurationStateForId(id: UniqueIdentifier) : StateAndRef<CoalitionConfigurationState>? =
        hub.vaultService.queryBy<CoalitionConfigurationState>(
            QueryCriteria.LinearStateQueryCriteria(
                linearId = listOf(id),
                status = Vault.StateStatus.UNCONSUMED,
                relevancyStatus = Vault.RelevancyStatus.ALL)).states.singleOrNull()

    /**
     * Returns the coalition configuration state for a list of participants
     */
    fun findCoalitionConfigurationStateForParticipants(participants: List<AbstractParty>) : StateAndRef<CoalitionConfigurationState>? =
        hub.vaultService.queryBy<CoalitionConfigurationState>(
            QueryCriteria.VaultQueryCriteria(
                participants = participants,
                status = Vault.StateStatus.UNCONSUMED,
                relevancyStatus = Vault.RelevancyStatus.ALL)).states.singleOrNull()
}
