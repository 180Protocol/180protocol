package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.ConsumerAggregationContract
import com.protocol180.aggregator.states.ConsumerAggregationState
import com.protocol180.aggregator.states.ProviderAggregationState
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandData
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
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 * This is the flow which handles issuance of new Aggregation Request .
 * Providing the information related to different provider required to be participated in the flow .
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationProposeFlow(val host : Party) : FlowLogic<SignedTransaction>() {

    override val progressTracker = ProgressTracker()


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

        val consumer = ourIdentity

        var consumerAggregationState = ConsumerAggregationState(consumer.anonymise(),host,null, null,null)

        // Step 2. Create a new propose command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val commandData: CommandData = ConsumerAggregationContract.Commands.Propose()

//        val proposeCommand = Command(ConsumerAggregationContract.Commands.Propose(), consumerAggregationState.participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the ConsumerAggregationState as an output state, as well as a command to the transaction builder.
        builder.addOutputState(consumerAggregationState, ConsumerAggregationContract.Aggregation_Propose_CONTRACT_ID)
        builder.addCommand(commandData, host.owningKey, consumer.owningKey)

        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)



        // we sign the transaction with our private key & making it immutable.
        val ptx = serviceHub.signInitialTransaction(builder)

        val session = initiateFlow(host)

        //The counterparty(host) signs the transaction.
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(ptx, listOf(session)))

        // Assuming no exceptions, we can now finalise the transaction.
        return subFlow(FinalityFlow(fullySignedTransaction, listOf(session)))

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
                "This Output state must be type of ConsumerAggregationState " using (output is ConsumerAggregationState)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
//        subFlow(ProviderAggregationProposeFlow(ProviderAggregationState(output.)))
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}