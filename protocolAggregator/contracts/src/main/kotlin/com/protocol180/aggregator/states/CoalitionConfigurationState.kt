package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.CoalitionConfigurationContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

/**
 * State for managing the lifecycle of Coalition Configuration
 * @see coalitionPartyToRole - Map containing the Coalition Roles and Corda Parties that fulfill them within the Coalition network
 * @see supportedCoalitionDataTypes - List of Coalitions Data Types that are supported for aggregation
 */
@BelongsToContract(CoalitionConfigurationContract::class)
data class CoalitionConfigurationState(
    override val linearId: UniqueIdentifier,
    val coalitionPartyToRole: Map<RoleType, Set<Party>>,
    val supportedCoalitionDataTypes: List<CoalitionDataType>
) : LinearState {

    fun getPartiesForRole(role: RoleType): Set<Party>? = coalitionPartyToRole[role]

    fun getRolesForParty(party: Party): Set<RoleType>? = coalitionPartyToRole.filterValues{ it.contains(party) }.keys

    fun getDataTypeForCode(dataTypeCode: String): CoalitionDataType? = supportedCoalitionDataTypes.
    firstOrNull { it.dataTypeCode == dataTypeCode }

    fun isSupportedDataType(dataTypeCode: String): Boolean = supportedCoalitionDataTypes.
    map { it.dataTypeCode }.contains(dataTypeCode)

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  coalition participants.
     */
    override val participants: List<AbstractParty> get() = coalitionPartyToRole.values.fold(listOf()){
        acc, e -> acc + e
    }
}

/**
 * Data types for aggregation that are supported by any coalition. This contains
 *  @see dataTypeCode - used as a unique identifier for a given data type, used by APIs
 *  @see dataTypeCode - used as a display name for a given data type
 *  @see schemaFile - a file object that contains the avro envelope schema for the data type; used to initialize the
 *  @see com.protocol180.aggregator.flow.EnclaveClientService and dictate enclave communication
 */
@CordaSerializable
data class CoalitionDataType(
    val dataTypeCode: String,
    val dataTypeDisplay: String,
    val schemaFile: ByteArray,
    val enclaveName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoalitionDataType

        if (dataTypeCode != other.dataTypeCode) return false
        if (dataTypeDisplay != other.dataTypeDisplay) return false
        if (!schemaFile.contentEquals(other.schemaFile)) return false
        if (!enclaveName.contentEquals(other.enclaveName)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dataTypeCode.hashCode()
        result = 31 * result + dataTypeDisplay.hashCode()
        result = 31 * result + schemaFile.contentHashCode()
        result = 31 * result + enclaveName.hashCode()
        return result
    }
}

/**
 * Various Roles that can be played by nodes in coalitions.
 */
@CordaSerializable
enum class RoleType {

    COALITION_HOST,

    DATA_PROVIDER,

    DATA_CONSUMER
}

