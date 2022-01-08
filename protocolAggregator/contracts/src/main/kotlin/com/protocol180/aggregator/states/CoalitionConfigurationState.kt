package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.CoalitionConfigurationContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.io.File

@BelongsToContract(CoalitionConfigurationContract::class)
data class CoalitionConfigurationState(
    val coalitionPartyToRole: Map<Party, RoleType>,
    val supportedDataTypes: DataTypes
) : ContractState {

    fun getPartiesForRole(role: RoleType): Set<Party> {
        return coalitionPartyToRole.filterValues{ it == role }.keys
    }

    fun getRoleForParty(party: Party): RoleType? {
        return coalitionPartyToRole[party]
    }

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  consumer or host.
     */
    override val participants: List<AbstractParty> get() = coalitionPartyToRole.keys.toList()
}

/**
 * Data types for aggregation that are supported by any coalition. This contains
 *  dataTypeCode - used as a unique identifier for a given data type, used by APIs
 *  dataTypeCode - used as a display name for a given data type
 *  schemaFile - a file object that contains the avro envelope schema for the data type; used to initialize the
 *  EnclaveClientService and dictate enclave communication
 */
@CordaSerializable
data class DataTypes(
    val dataTypeCode: String,
    val dataTypeDisplay: String,
    val schemaFile: File
)

/**
 * Various Roles that can be played by nodes in coalitions.
 */
@CordaSerializable
enum class RoleType {

    COALITION_HOST,

    DATA_PROVIDER,

    DATA_CONSUMER
}

