package com.protocol180.aggregator.contracts

import com.protocol180.aggregator.states.RewardsState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class RewardsContract: Contract {
    companion object {
        @JvmStatic
        val ID = "com.protocol180.aggregator.contracts.RewardsContract"
    }

    /**
     * Contract to verify
     * @see com.protocol180.aggregator.states.RewardsState produced during the
     * @see com.protocol180.aggregator.flow.ConsumerAggregationFlow
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when issuing a rewards state." using (tx.inputs.isEmpty())
                "Only one rewards state should be created when issuing a rewards state." using (tx.outputs.size == 1)
                val rewardsState = tx.outputsOfType<RewardsState>().single()
                "A newly issued rewards must have a provider & host" using
                        (rewardsState.provider != null && rewardsState.host != null)
                "The enclave attestation used to create the rewards must not be null" using
                        (rewardsState.enclaveAttestation != null)
                "The flow topic used to create the rewards must not be null" using
                        (rewardsState.flowTopic != null)
                "Only the provider and host may sign the Rewards State Transaction" using
                        (command.signers.toSet() == rewardsState.participants.map { it.owningKey }.toSet())
            }

        }
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * Commands.Issue - Issues the given state
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {

        class Create : TypeOnlyCommandData(), Commands
    }
}