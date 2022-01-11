package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.CoalitionConfigurationContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.io.InputStream

@BelongsToContract(CoalitionConfigurationContract::class)
data class CoalitionConfigurationState(
    override val linearId: UniqueIdentifier,
    val coalitionPartyToRole: Map<Party, RoleType>,
    val supportedCoalitionDataTypes: List<CoalitionDataType>
) : LinearState {

    fun getPartiesForRole(role: RoleType): Set<Party> = coalitionPartyToRole.filterValues{ it == role }.keys

    fun getRoleForParty(party: Party): RoleType? = coalitionPartyToRole[party]

    fun getDataTypeForCode(dataTypeCode: String): CoalitionDataType? = supportedCoalitionDataTypes.
    firstOrNull { it.dataTypeCode == dataTypeCode }

    fun isSupportedDataType(dataTypeCode: String): Boolean = supportedCoalitionDataTypes.
    map { it.dataTypeCode }.contains(dataTypeCode)

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
data class CoalitionDataType(
    val dataTypeCode: String,
    val dataTypeDisplay: String,
    val schemaFile: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoalitionDataType

        if (dataTypeCode != other.dataTypeCode) return false
        if (dataTypeDisplay != other.dataTypeDisplay) return false
        if (!schemaFile.contentEquals(other.schemaFile)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dataTypeCode.hashCode()
        result = 31 * result + dataTypeDisplay.hashCode()
        result = 31 * result + schemaFile.contentHashCode()
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

