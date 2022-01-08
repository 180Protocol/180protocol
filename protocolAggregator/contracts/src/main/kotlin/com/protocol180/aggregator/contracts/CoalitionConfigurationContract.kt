package com.protocol180.aggregator.contracts

import com.protocol180.aggregator.states.CoalitionConfigurationState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class CoalitionConfigurationContract : Contract {
    companion object {
        @JvmStatic
        val ID = "com.protocol180.aggregator.contracts.CoalitionConfigurationContract"
    }

    /**
     * Contract to verify Data Output States
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing a data output state." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a data output state." using (tx.outputs.size == 1)
                val coalitionConfigurationState = tx.outputsOfType<CoalitionConfigurationState>().single()

            }

        }
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {

        class Issue : TypeOnlyCommandData(), Commands
    }
}