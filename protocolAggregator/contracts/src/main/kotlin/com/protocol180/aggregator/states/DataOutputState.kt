package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.DataOutputContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.time.Instant

/**
 * State acting as receipt of Data Output compute by host on behalf of client
 * @see consumer - consumer for Data Output
 * @see host - host running enclave that computes the Data Output
 * @see enclaveAttestation - Enclave attestation bytes for verification
 * @see flowTopic - Flow topic that resulted in creation of state
 */
@BelongsToContract(DataOutputContract::class)
data class DataOutputState(val consumer: Party,
                           val host: Party,
                           val dataType: String,
                           val description: String,
                           val dateCreated: Instant,
                           val enclaveAttestation: ByteArray,
                           val flowTopic: String
) : ContractState {

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  consumer or host.
     */
    override val participants: List<AbstractParty> get() = listOf(consumer, host)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataOutputState

        if (consumer != other.consumer) return false
        if (host != other.host) return false
        if (dataType != other.dataType) return false
        if (description != other.description) return false
        if (dateCreated != other.dateCreated) return false
        if (enclaveAttestation != other.enclaveAttestation) return false
        if (flowTopic != other.flowTopic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = consumer.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + dateCreated.hashCode()
        result = 31 * result + enclaveAttestation.hashCode()
        result = 31 * result + flowTopic.hashCode()
        return result
    }

}
