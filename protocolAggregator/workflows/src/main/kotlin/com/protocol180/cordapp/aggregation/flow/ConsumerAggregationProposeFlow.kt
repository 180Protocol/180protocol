package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.ConsumerAggregationContract
import com.protocol180.aggregator.cordapp.sample.host.AggregationEnclaveService
import com.protocol180.aggregator.states.ConsumerAggregationState
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import java.security.PublicKey

/**
 * This is the flow which handles issuance of new Aggregation Request .
 * Providing the information related to different provider required to be participated in the flow .
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationProposeFlow(val host: Party) : FlowLogic<SignedTransaction>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): SignedTransaction {


        //Get a reference to the notary service on our network and our key pair.
        val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1

        val consumer = ourIdentity


        // Step 2. Create a new propose command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val commandData: CommandData = ConsumerAggregationContract.Commands.Propose()

        var consumerAggregationState = ConsumerAggregationState(consumer.anonymise(), host, null, null, null)

        val proposeCommand = Command(ConsumerAggregationContract.Commands.Propose(), consumerAggregationState.participants.map { it.owningKey })

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
@InitiatingFlow
class ConsumerAggregationProposeFlowResponder(val flowSession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        println("Inside Responder flow available to host")
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val enclaveService = this.serviceHub.cordaService(AggregationEnclaveService::class.java)

        val attestationBytes = enclaveService.attestationBytes
        val providers = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.get(0) } - ourIdentity - notary - flowSession.counterparty

        val clientKeyMapWithRandomKeyGenerated = mutableMapOf<PublicKey, String>()
//        enclaveService.deliverAndPickUpMail()

        val providerSessions = providers.map { initiateFlow(it) }
        providerSessions.forEach { providerSession ->
            val providerDataPair = providerSession.sendAndReceive<Pair<String, ByteArray>>(attestationBytes).unwrap { it }
            println("Provider Data Pair:" + providerDataPair.toString())
            clientKeyMapWithRandomKeyGenerated.put(providerSession.counterparty.owningKey, providerDataPair.first)

            val encryptedPayloadFromProvider = providerDataPair.second

            val encryptedResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedPayloadFromProvider))
            println(String(encryptedResponseByteFromEnclave))
        }


        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {

            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}