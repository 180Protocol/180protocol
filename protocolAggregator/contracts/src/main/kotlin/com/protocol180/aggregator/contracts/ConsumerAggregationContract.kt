package com.protocol180.aggregator.contracts

import com.protocol180.aggregator.states.ConsumerAggregationState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class ConsumerAggregationContract : Contract {
    companion object {
        @JvmStatic
        val Aggregation_Propose_CONTRACT_ID = "com.protocol180.aggregator.contracts.ConsumerAggregationContract"
    }

    /**
     * Takes an object that represents a state transition, and ensures the inputs/outputs/commands make sense.
     * Must throw an exception if there's a problem that should prevent state transition. Takes a single object
     * rather than an argument so that additional data can be added without breaking binary compatibility with
     * existing contract code.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Propose -> requireThat {
                "No inputs should be consumed when issuing an Propose." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an Propose." using (tx.outputs.size == 1)
                val consumerAggregationState = tx.outputsOfType<ConsumerAggregationState>().single()
                "A newly issued Propose must have Consumer & Host Public key" using
                        (consumerAggregationState.consumer != null && consumerAggregationState.host != null)
//                "The Type of Data required from consumer should not be empty" using (consumerAggregationState.dataType != null)
                "Both consumer and host together only may sign ConsumerAggregation Propose Transaction." using
                        (command.signers.toSet() == consumerAggregationState.participants.map { it.owningKey }.toSet())
            }

        }
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {

        class Propose : TypeOnlyCommandData(), Commands
    }
}