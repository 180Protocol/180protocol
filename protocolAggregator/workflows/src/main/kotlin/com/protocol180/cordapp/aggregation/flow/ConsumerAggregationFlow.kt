package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.DataOutputContract
import com.protocol180.aggregator.cordapp.sample.host.AggregationEnclaveService
import com.protocol180.aggregator.states.DataOutputState
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519PrivateKey
import com.r3.conclave.mail.PostOffice
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
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import org.apache.avro.Schema
import java.security.PublicKey
import java.time.Instant

/**
 * This is the flow which handles issuance of new Aggregation Request .
 * Providing the information related to different provider required to be participated in the flow .
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationFlow(val host: Party) : FlowLogic<SignedTransaction>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): SignedTransaction {

        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)

        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val consumer = ourIdentity

        val hostSession = initiateFlow(host)
        //receive attestation from host
        val attestationBytes = hostSession.sendAndReceive<ByteArray>(enclaveClientService.envelopeSchema.toString()).unwrap { it }
        //key for this aggregation
        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()
        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        //send data output schema to be aggregated to host
        val encryptedAggregationDataRecordBytes = hostSession.sendAndReceive<ByteArray>(postOffice.encryptMail
        (enclaveClientService.aggregationOutputSchema.toString().toByteArray())).unwrap { it }
        val decryptedAggregationDataRecordBytes = postOffice.decryptMail(encryptedAggregationDataRecordBytes).bodyAsBytes
        consumerDbStoreService.addConsumerDataOutputWithFlowId(this.runId.uuid.toString(), decryptedAggregationDataRecordBytes, "Temp_Data_Type")

        //optional reading of records - needed for the front end read flow
        //enclaveClientService.readGenericRecordsFromOutputBytesAndSchema(decryptedAggregationDataRecordBytes, "aggregate")
        val commandData: CommandData = DataOutputContract.Commands.Create()
        val dataOutputState = DataOutputState(consumer, host, encryptedAggregationDataRecordBytes, Instant.now(),
                attestationBytes, flowTopic)

        val builder = TransactionBuilder(notary)
        builder.addOutputState(dataOutputState, DataOutputContract.ID)
        builder.addCommand(commandData, host.owningKey, consumer.owningKey)
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(ptx, listOf(hostSession)))
        return subFlow(FinalityFlow(fullySignedTransaction, listOf(hostSession)))
    }
}

/**
 * This is the flow which signs Aggregation Propose Transaction.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(ConsumerAggregationFlow::class)
@InitiatingFlow
class ConsumerAggregationFlowResponder(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        println("Inside Responder flow available to host")
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        //receive data output schema from consumer
        val envelopeSchema = flowSession.receive<String>().unwrap { it }
        val providerInputSchema = Schema.Parser().parse(envelopeSchema).getField("aggregateInput").schema().toString()
        val providerRewardsSchema = Schema.Parser().parse(envelopeSchema).getField("provenanceOutput").schema().toString()

        // initiate & configure enclave service to be used for aggregation
        val enclaveService = this.serviceHub.cordaService(AggregationEnclaveService::class.java)
        val attestationBytes = enclaveService.attestationBytes
        enclaveService.initializeAvroSchema(envelopeSchema.toByteArray())

        // Initiate Provider flows and acquire encrypted payload according to given schema
        val providers = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.get(0) } - ourIdentity - notary - flowSession.counterparty
        val clientKeyMapWithRandomKeyGenerated = mutableMapOf<PublicKey, Pair<String, ByteArray>>()

        val providerSessions = providers.map { initiateFlow(it) }
        providerSessions.forEach { providerSession ->
            //receive provider data pair - provider public key -> encrypted input data payload
            val providerDataPair = providerSession.sendAndReceive<Pair<String, ByteArray>>(Pair(attestationBytes, providerInputSchema)).unwrap { it }
            println("Provider Data Pair:$providerDataPair")
            clientKeyMapWithRandomKeyGenerated[providerSession.counterparty.owningKey] = providerDataPair
            val encryptedPayloadFromProvider = providerDataPair.second
            //send data to enclave
            val encryptedResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedPayloadFromProvider))
            println(String(encryptedResponseByteFromEnclave))
        }

        //send attestation to consumer
        val encryptedBytesFromConsumer = flowSession.sendAndReceive<ByteArray>(attestationBytes).unwrap { it }
        //compute data output for consumer using enclave and share with consumer
        val encryptedConsumerResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedBytesFromConsumer))
        flowSession.send(encryptedConsumerResponseByteFromEnclave)

        // Calculate reward points for each provider & submit reward response back to provider
        providerSessions.forEach { providerSession ->
            val providerEncryptedBytesForRewards = providerSession.sendAndReceive<ByteArray>(providerRewardsSchema).unwrap { it }
            val encryptedRewardResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, providerEncryptedBytesForRewards))
            println(String(encryptedRewardResponseByteFromEnclave))
            providerSession.send(encryptedRewardResponseByteFromEnclave)
        }

        //finalise data output state creation
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}