package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.ConsumerAggregationContract
import com.protocol180.aggregator.cordapp.sample.host.AggregationEnclaveService
import com.protocol180.aggregator.states.ConsumerAggregationState
import com.protocol180.utils.MockClientUtil
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519PrivateKey
import com.r3.conclave.mail.PostOffice
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandData
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import java.security.PublicKey

/**
 * This is the flow which handles issuance of new Aggregation Request .
 * Providing the information related to different provider required to be participated in the flow .
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationProposeFlow(val host: Party, val envelopeSchema: Schema) : FlowLogic<List<GenericRecord>>() {

    override val progressTracker = ProgressTracker()


    @Suspendable
    override fun call(): List<GenericRecord> {


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

        val hostSession = initiateFlow(host)

        val attestationBytes = hostSession.sendAndReceive<ByteArray>(envelopeSchema.toString()).unwrap { it }

        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()
        val mockClientUtil = com.protocol180.utils.MockClientUtil()

        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        val encryptedAggregationDataRecordBytes = hostSession.sendAndReceive<ByteArray>(postOffice.encryptMail(MockClientUtil.aggregationOutputSchema.toString().toByteArray())).unwrap { it }

        val decryptedAggregationDataRecordBytes = postOffice.decryptMail(encryptedAggregationDataRecordBytes).bodyAsBytes


        return MockClientUtil.readGenericRecordsFromOutputBytesAndSchema(decryptedAggregationDataRecordBytes, "aggregate")

    }
}

/**
 * This is the flow which signs Aggregation Propose Transaction.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(ConsumerAggregationProposeFlow::class)
@InitiatingFlow
class ConsumerAggregationProposeFlowResponder(val flowSession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        println("Inside Responder flow available to host")
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val envelopeSchema = flowSession.receive<String>().unwrap { it }

        val providerInputSchema= Schema.Parser().parse(envelopeSchema).getField("aggregateInput").schema().toString()

        val providerRewardsSchema= Schema.Parser().parse(envelopeSchema).getField("provenanceOutput").schema().toString()


        // initiate & configure enclave service to be used for aggregation
        val enclaveService = this.serviceHub.cordaService(AggregationEnclaveService::class.java)

        val attestationBytes = enclaveService.attestationBytes

        enclaveService.initializeAvroSchema(envelopeSchema.toByteArray())


        // Initiate Provider flows and acquire encrypted payload according to given schema
        val providers = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.get(0) } - ourIdentity - notary - flowSession.counterparty

        val clientKeyMapWithRandomKeyGenerated = mutableMapOf<PublicKey, Pair<String, ByteArray>>()


        val providerSessions = providers.map { initiateFlow(it) }
        providerSessions.forEach { providerSession ->
            val providerDataPair = providerSession.sendAndReceive<Pair<String, ByteArray>>(Pair(attestationBytes, providerInputSchema)).unwrap { it }
            println("Provider Data Pair:" + providerDataPair.toString())
            clientKeyMapWithRandomKeyGenerated.put(providerSession.counterparty.owningKey, providerDataPair)

            val encryptedPayloadFromProvider = providerDataPair.second

            val encryptedResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedPayloadFromProvider))
            println(String(encryptedResponseByteFromEnclave))
        }

        val encryptedBytesFromConsumer = flowSession.sendAndReceive<ByteArray>(attestationBytes).unwrap { it }

        val encryptedConsumerResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedBytesFromConsumer))

        flowSession.send(encryptedConsumerResponseByteFromEnclave)

        // Calculate reward points for each provider & submit reward response back to provider
        providerSessions.forEach { providerSession ->
            val providerEncryptedBytesForRewards = providerSession.sendAndReceive<ByteArray>(providerRewardsSchema).unwrap { it }
//            clientKeyMapWithRandomKeyGenerated.put(providerSession.counterparty.owningKey, providerDataPair)

//            val encryptedPayloadFromProvider = providerDataPair.second

            val encryptedRewardResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, providerEncryptedBytesForRewards))
            println(String(encryptedRewardResponseByteFromEnclave))
            providerSession.send(encryptedRewardResponseByteFromEnclave)
        }


    }
}