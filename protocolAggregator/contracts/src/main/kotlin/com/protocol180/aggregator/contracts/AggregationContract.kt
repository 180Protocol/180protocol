package com.protocol180.aggregator.contracts

import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class AggregationContract:Contract {
    /**
     * Takes an object that represents a state transition, and ensures the inputs/outputs/commands make sense.
     * Must throw an exception if there's a problem that should prevent state transition. Takes a single object
     * rather than an argument so that additional data can be added without breaking binary compatibility with
     * existing contract code.
     */
    override fun verify(tx: LedgerTransaction) {
        TODO("Not yet implemented")
    }
}