package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.CoalitionConfigurationContract
import com.protocol180.aggregator.states.CoalitionConfigurationState
import com.protocol180.aggregator.states.RoleType
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor


/**
 * CoalitionConfigurationFlow allows the coalition host to -
 * 1. Define the roles for parties in the network
 * 2. Supported data types for aggregation by the coalition members
 * 3. Any other governance items and shared truths between coalition members
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */

@InitiatingFlow
@StartableByRPC
class CreateCoalitionConfigurationFlow(private val coalitionConfigurationState: CoalitionConfigurationState) :
    FlowLogic<SignedTransaction>() {

    companion object{
        private val log = loggerFor<CreateCoalitionConfigurationFlow>()
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(IncorrectRoleException::class)
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        log.info("Using notary $notary")

        val ourRole = serviceHub.cordaService(NetworkParticipantService::class.java).role

        if(ourRole != RoleType.COALITION_HOST){
            throw IncorrectRoleException("Only Coalition Host can modify coalition configuration")
        }

        //query vault for existing state - update with new parameters - add as input state to transaction
        //verify validity of inputs in contract verify function
        //

        //build transaction for coalition configuration state and verify with contract
        val builder = TransactionBuilder(notary)
        builder.addOutputState(coalitionConfigurationState, CoalitionConfigurationContract.ID)
        builder.addCommand(CoalitionConfigurationContract.Commands.Issue(),
            coalitionConfigurationState.coalitionPartyToRole.keys.map{it.owningKey})
        builder.verify(serviceHub)


        //initiate sessions with all parties specified in the rolePartyIdentity map except for the host and collect sigs
        val counterpartySessions : List<FlowSession> = coalitionConfigurationState.coalitionPartyToRole.
        filter { it.key != ourIdentity }.map { initiateFlow(it.key) }

        val ptx = serviceHub.signInitialTransaction(builder)
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(ptx, counterpartySessions))
        return subFlow(FinalityFlow(fullySignedTransaction, counterpartySessions))
    }

}

@InitiatedBy(CreateCoalitionConfigurationFlow::class)
@InitiatingFlow
class CreateCoalitionConfigurationFlowResponder(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    companion object{
        private val log = loggerFor<CreateCoalitionConfigurationFlowResponder>()
    }

    @Suspendable
    override fun call() {
        //finalise rewards state creation
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}




/**
 * Thrown when the handshake flow on either side cannot accommodate requested set of protocol features
 */
class IncorrectRoleException(private val reason: String)
    : FlowException("Operation failed due to an incorrect role: $reason")
