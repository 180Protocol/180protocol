package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.ConsumerAggregationContract
import com.protocol180.aggregator.states.ConsumerAggregationState
import com.protocol180.aggregator.states.ProviderAggregationState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

/**
 * This is the flow which handles issuance of new Aggregation Request .
 * Providing the information related to different provider required to be participated in the flow .
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationProposeFlow(val state: ConsumerAggregationState) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.

        /**
         *  METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
         *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flow or parsed from config (Preferred)
         *
         *  * - For production you always want to use Method 2 as it guarantees the expected notary is returned.
         */
        val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1
        // val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")) // METHOD 2

        // Step 2. Create a new issue command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val issueCommand = Command(ConsumerAggregationContract.Commands.Propose(), state.participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the ConsumerAggregationState as an output state, as well as a command to the transaction builder.
        builder.addOutputState(state, ConsumerAggregationContract.Aggregation_Propose_CONTRACT_ID)
        builder.addCommand(issueCommand)

        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction.
        return subFlow(FinalityFlow(stx, sessions))
    }
}

/**
 * This is the flow which signs Aggregation Propose Transaction.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(ConsumerAggregationProposeFlow::class)
class ConsumerAggregationProposeFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                val proposalToHost = serviceHub.vaultService.queryBy<ConsumerAggregationState>().states.single()
                "This Output state must be injected into ProviderAggregationFlow " using (output is ConsumerAggregationState)
                if (ourIdentity.anonymise() != proposalToHost.state.data.consumer) {
                    throw IllegalArgumentException("Proposal for Aggregation must be initiated from Consumer.")
                }
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
//        subFlow(ProviderAggregationProposeFlow(ProviderAggregationState(output.)))
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}