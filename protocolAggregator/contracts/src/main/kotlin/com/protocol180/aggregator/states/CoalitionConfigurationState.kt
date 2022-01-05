package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.CoalitionConfigurationContract
import com.r3.conclave.cordapp.common.DataTypes
import com.r3.conclave.cordapp.common.RoleType
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party

@BelongsToContract(CoalitionConfigurationContract::class)
data class CoalitionConfigurationState(
    val rolePartyIdentity: Map<Party, RoleType>,
    val dataTypes: DataTypes
) : ContractState {

    fun getPartyFromRole(role: String): Map<Party, RoleType> {
        return rolePartyIdentity.filter { it.value.type == role }
    }

    fun getRoleFromParty(party: Party): Map<Party, RoleType> {
        return rolePartyIdentity.filter { it.key.owningKey == party.owningKey }
    }
}
