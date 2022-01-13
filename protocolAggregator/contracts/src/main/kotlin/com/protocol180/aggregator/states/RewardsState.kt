package com.protocol180.aggregator.states

import com.protocol180.aggregator.contracts.RewardsContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.time.Instant

@BelongsToContract(RewardsContract::class)
data class RewardsState(val provider: Party,
                        val host: Party,
                        val decryptedRewardsBytes: ByteArray,
                        val dateCreated: Instant,
                        val enclaveAttestation: ByteArray,
                        val flowTopic: String) : ContractState {

    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  consumer or host.
     */
    override val participants: List<AbstractParty> get() = listOf(host, provider)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RewardsState

        if (provider != other.provider) return false
        if (host != other.host) return false
        if (!decryptedRewardsBytes.contentEquals(other.decryptedRewardsBytes)) return false
        if (dateCreated != other.dateCreated) return false
        if (enclaveAttestation != other.enclaveAttestation) return false
        if (flowTopic != other.flowTopic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = provider.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + decryptedRewardsBytes.contentHashCode()
        result = 31 * result + dateCreated.hashCode()
        result = 31 * result + enclaveAttestation.hashCode()
        result = 31 * result + flowTopic.hashCode()
        return result
    }

}
