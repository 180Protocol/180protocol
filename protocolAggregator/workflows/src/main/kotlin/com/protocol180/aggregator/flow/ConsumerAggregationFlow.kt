package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.DataOutputContract
import com.protocol180.aggregator.states.DataOutputState
import com.protocol180.aggregator.states.RoleType
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519PrivateKey
import com.r3.conclave.mail.PostOffice
import net.corda.core.contracts.CommandData
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap
import java.security.PublicKey
import java.time.Instant

/**
 * ConsumerAggregationFlow allows consumers in coalitions to initiate a data aggregation request. The flow accepts
 * a supported dataType as an argument and initiates a request to the coalition host to perform an aggregation by
 * requesting data from providers in the network. The flow queries and checks the CoalitionConfigurationState to
 * determine whether the requested dataType is valid and supported by the coalition.
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationFlow(private val dataType: String) : FlowLogic<SignedTransaction>() {

    companion object{
        private val log = loggerFor<ConsumerAggregationFlow>()
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(ConsumerAggregationFlowException::class)
    override fun call(): SignedTransaction {

        //TODO: change API to accept data type argument - query host identity from coalition config state - fail if state not present

        val coalitionConfigurationStateService = serviceHub.cordaService(CoalitionConfigurationStateService::class.java)
        val consumerDbStoreService = serviceHub.cordaService(ConsumerDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)

        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val consumer = ourIdentity

        val coalitionConfiguration = coalitionConfigurationStateService.findCoalitionConfigurationStateForParticipants(listOf(ourIdentity))

        if(coalitionConfiguration == null){
            throw ConsumerAggregationFlowException("Coalition Configuration is not known to node, host needs to update configuration and include node in participants")
        } else if (!coalitionConfiguration.state.data.isSupportedDataType(dataType)){
            throw ConsumerAggregationFlowException("Unsupported data type requested for aggregation, please use a supported data type configured in the coalition configuration")
        }

        enclaveClientService.initializeSchema(String(coalitionConfiguration.state.data.getDataTypeForCode(dataType)!!.schemaFile))

        val host = coalitionConfiguration.state.data.getPartiesForRole(RoleType.COALITION_HOST).single()
        log.info("Found host in configuration state: $host")
        val hostSession = initiateFlow(host)
        //receive attestation from host
        val attestationBytes = hostSession.sendAndReceive<ByteArray>(dataType).unwrap { it }
        //key for this aggregation
        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()
        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        //send data output schema to be aggregated to host
        val encryptedAggregationDataRecordBytes = hostSession.sendAndReceive<ByteArray>(postOffice.encryptMail(enclaveClientService
                .aggregationOutputSchema.toString().toByteArray())).unwrap { it }
        val decryptedAggregationDataRecordBytes = postOffice.decryptMail(encryptedAggregationDataRecordBytes).bodyAsBytes

        //Store aggregation output data received from enclave into consumer's local db
        consumerDbStoreService.addConsumerDataOutputWithFlowId(this.runId.uuid.toString(), decryptedAggregationDataRecordBytes, dataType)

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

    companion object{
        private val log = loggerFor<ConsumerAggregationFlowResponder>()
    }

    @Suspendable
    @Throws(ConsumerAggregationFlowException::class)
    override fun call() {
        log.info("Inside Responder flow available to host")
        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        //receive data type from consumer
        val dataType = flowSession.receive<String>().unwrap { it }

        //verify that host has agreed for aggregation of given data type
        val coalitionConfigurationStateService = serviceHub.cordaService(CoalitionConfigurationStateService::class.java)
        val coalitionConfiguration = coalitionConfigurationStateService.findCoalitionConfigurationStateForParticipants(listOf(ourIdentity))

        if (coalitionConfiguration == null) {
            throw ConsumerAggregationFlowException("Coalition Configuration is not known to node, host needs to update configuration and include node in participants")
        } else if (!coalitionConfiguration.state.data.isSupportedDataType(dataType)) {
            throw ConsumerAggregationFlowException("Unsupported data type requested for aggregation, please use a supported data type configured in the coalition configuration")
        }

        // initiate & configure enclave service to be used for aggregation
        val enclaveService = this.serviceHub.cordaService(AggregationEnclaveService::class.java)
        val attestationBytes = enclaveService.attestationBytes
        enclaveService.initializeAvroSchema(coalitionConfiguration.state.data.getDataTypeForCode(dataType)!!.schemaFile)

        // Initiate Provider flows and acquire encrypted payload according to given schema
        val providers = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.get(0) } - ourIdentity - notary - flowSession.counterparty
        val clientKeyMapWithRandomKeyGenerated = mutableMapOf<PublicKey, Pair<String, ByteArray>>()

        val providerSessions = providers.map { initiateFlow(it) }
        providerSessions.forEach { providerSession ->
            //receive provider data pair - provider public key -> encrypted input data payload
            val providerDataPair = providerSession.sendAndReceive<Pair<String, ByteArray>>(Pair(attestationBytes, dataType)).unwrap { it }
            log.info("Provider Data Pair:$providerDataPair")
            clientKeyMapWithRandomKeyGenerated[providerSession.counterparty.owningKey] = providerDataPair
            val encryptedPayloadFromProvider = providerDataPair.second
            //send data to enclave
            val encryptedResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedPayloadFromProvider))
            log.info(String(encryptedResponseByteFromEnclave))
        }

        //send attestation to consumer
        val encryptedBytesFromConsumer = flowSession.sendAndReceive<ByteArray>(attestationBytes).unwrap { it }
        //compute data output for consumer using enclave and share with consumer
        val encryptedConsumerResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, encryptedBytesFromConsumer))
        flowSession.send(encryptedConsumerResponseByteFromEnclave)

        // Calculate reward points for each provider & submit reward response back to provider
        providerSessions.forEach { providerSession ->
            val providerEncryptedBytesForRewards = providerSession.sendAndReceive<ByteArray>(dataType).unwrap { it }
            val encryptedRewardResponseByteFromEnclave = this.await(enclaveService.deliverAndPickUpMail(this, providerEncryptedBytesForRewards))
            log.info(String(encryptedRewardResponseByteFromEnclave))
            providerSession.send(encryptedRewardResponseByteFromEnclave)
        }

        //finalise data output state creation
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                log.info("Checking issuance transaction before signing: ${stx.tx.id}")
                val tx = stx.toLedgerTransaction(serviceHub, false)
                tx.verify()
                val dataOutputState = tx.outputStates.filterIsInstance<DataOutputState>().single()
                check(dataOutputState.host == ourIdentity){
                    "Data Output State responder must be verified by host"
                }
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}

/**
 * Thrown when the Consumer Aggregation Flow fails
 */
class ConsumerAggregationFlowException(private val reason: String)
    : FlowException("Consumer Aggregation Flow failed: $reason")
