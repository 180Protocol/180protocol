package com.protocol180.aggregator.contracts

import com.protocol180.aggregator.states.DataOutputState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class DataOutputContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.protocol180.aggregator.contracts.DataOutputContract"
    }

    /**
     * Contract to verify
     * @see com.protocol180.aggregator.states.DataOutputState produced during the
     * @see com.protocol180.aggregator.flow.ConsumerAggregationFlow
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing a data output state." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a data output state." using (tx.outputs.size == 1)
                val dataOutputState = tx.outputsOfType<DataOutputState>().single()
                "A newly issued data output must have a consumer & host" using
                        (dataOutputState.consumer != null && dataOutputState.host != null)
                "The enclave attestation used to create the data output must not be null" using
                        (dataOutputState.enclaveAttestation != null)
                "The data type requested to create the data output must not be null" using
                        (dataOutputState.dataType != null)
                "The description provided to create the data output must not be null" using
                        (dataOutputState.description != null)
                "The flow topic used to create the data output must not be null" using
                        (dataOutputState.flowTopic != null)
                "Only the consumer and host may sign the Data Output State Transaction" using
                        (command.signers.toSet() == dataOutputState.participants.map { it.owningKey }.toSet())
            }

        }
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * Commands.Issue - Issues the given state
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {

        class Issue : TypeOnlyCommandData(), Commands
    }
}