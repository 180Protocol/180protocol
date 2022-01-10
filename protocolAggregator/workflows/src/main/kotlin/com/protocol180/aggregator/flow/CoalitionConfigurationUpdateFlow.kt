package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.CoalitionConfigurationContract
import com.protocol180.aggregator.states.CoalitionConfigurationState
import com.protocol180.aggregator.states.CoalitionDataType
import com.protocol180.aggregator.states.RoleType
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
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
class CoalitionConfigurationUpdateFlow(private val coalitionPartyToRole: Map<Party, RoleType>,
                                       private val supportedCoalitionDataTypes: List<CoalitionDataType>) :
    FlowLogic<SignedTransaction>() {
    companion object{
        private val log = loggerFor<CoalitionConfigurationUpdateFlow>()
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(IncorrectRoleException::class)
    override fun call(): SignedTransaction {

        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        log.info("Using notary $notary")

        val ourRole = serviceHub.cordaService(NetworkParticipantService::class.java).role
        val coalitionConfigurationStateService = serviceHub.cordaService(CoalitionConfigurationStateService::class.java)

        if(ourRole != RoleType.COALITION_HOST){
            throw IncorrectRoleException("Only Coalition Host can modify coalition configuration")
        }
        val currentState = coalitionConfigurationStateService.
        findCoalitionConfigurationStateForParticipants(listOf(ourIdentity))

        //build transaction for coalition configuration state and verify with contract
        val builder = TransactionBuilder(notary)
        if (currentState != null) {
            builder.addInputState(currentState)
            val updatedState = CoalitionConfigurationState(currentState.state.data.linearId,
                coalitionPartyToRole, supportedCoalitionDataTypes)
            builder.addOutputState(updatedState, CoalitionConfigurationContract.ID)
            builder.addCommand(CoalitionConfigurationContract.Commands.Update(),
                updatedState.coalitionPartyToRole.keys.map{it.owningKey})
        } else {
            val updatedState = CoalitionConfigurationState(UniqueIdentifier(), coalitionPartyToRole, supportedCoalitionDataTypes)
            builder.addOutputState(updatedState, CoalitionConfigurationContract.ID)
            builder.addCommand(CoalitionConfigurationContract.Commands.Issue(),
                updatedState.coalitionPartyToRole.keys.map{it.owningKey})
        }

        //TODO: verify validity of inputs in contract verify function
        builder.verify(serviceHub)

        //initiate sessions with all parties specified in the rolePartyIdentity map except for the host and collect sigs
        val counterpartySessions : List<FlowSession> = coalitionPartyToRole.
        filter { it.key != ourIdentity }.map { initiateFlow(it.key) }

        val ptx = serviceHub.signInitialTransaction(builder)
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(ptx, counterpartySessions))
        return subFlow(FinalityFlow(fullySignedTransaction, counterpartySessions))
    }

}

@InitiatedBy(CoalitionConfigurationUpdateFlow::class)
@InitiatingFlow
class CoalitionConfigurationUpdateFlowResponder(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    companion object{
        private val log = loggerFor<CoalitionConfigurationUpdateFlowResponder>()
    }

    @Suspendable
    override fun call() {
        //finalise coalition configuration state creation
        val ourRole = serviceHub.cordaService(NetworkParticipantService::class.java).role
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                log.info("Checking issuance transaction before signing: ${stx.tx.id}")
                val tx = stx.toLedgerTransaction(serviceHub, false)
                tx.verify()
                val coalitionConfigurationState = tx.outputStates.filterIsInstance<CoalitionConfigurationState>().single()
                check(coalitionConfigurationState.getPartiesForRole(ourRole).contains(ourIdentity)){
                    "Our role in Coalition Configuration State matches our node configuration"
                }
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
