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
 * One of 180Protocol's supported Broker Flows. There will be more flow types in the future which are provider initiated instead
 * of consumer initiated. ConsumerAggregationFlow allows consumers in coalitions to initiate a data aggregation request. The flow accepts
 * a supported dataType as an argument and initiates a request to the coalition host to perform an aggregation by
 * requesting data from providers in the network. The flow queries and checks the CoalitionConfigurationState to
 * determine whether the requested dataType is valid and supported by the coalition.* Consumer receives an enclave attestation
 * from the host to verify the enclaves validity and utilize the same for decrypting enclave generated encrypted data outputs.
 * Consumer then creates a [DataOutputState], transacting and signing with the host to store on their respective ledgers
 * as a proof of data aggregation. The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class ConsumerAggregationFlow(private val dataType: String, private val description: String) : FlowLogic<SignedTransaction>() {

    companion object{
        private val log = loggerFor<ConsumerAggregationFlow>()
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    @Throws(ConsumerAggregationFlowException::class)
    override fun call(): SignedTransaction {
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

        val host = coalitionConfiguration.state.data.getPartiesForRole(RoleType.COALITION_HOST)!!.single()
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
        val commandData: CommandData = DataOutputContract.Commands.Issue()
        val dataOutputState = DataOutputState(consumer, host, dataType, description, Instant.now(), attestationBytes, flowTopic)

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
 * Counter flow for [ConsumerAggregationFlow]. Host handles this flow and receives the aggregation request from the
 * consumer, including a supported coalition data type to aggregate. Host confirms the validity of the data type by querying
 * and checking against the [com.protocol180.aggregator.states.CoalitionConfigurationState]. Once the data type validity
 * is confirmed the host initiates the enclave using [AggregationEnclaveService]. The host passes the data type from the
 * consumer to the enclave and initiates using the data types associated Avro schema (envelope schema). The enclave is
 * initiated and then the host gathers data from each of the providers in the network by kicking the [ProviderAggregationResponseFlow]
 * The host sends providers the enclave attestation and the requested data type. Providers then send back encrypted data
 * to the host. Host sends this data to the enclave and requests the output. The encrypted Data Output from the enclave is sent back
 * to the consumer who creates a [DataOutputState] transaction using this data.
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

        var flowId= this.runId.uuid.toString()

        // Load enclave specific to current flow only
        enclaveService.loadEnclaveForAggregation(flowId)
        val attestationBytes = enclaveService.getAttestationBytes(flowId)
        enclaveService.initializeAvroSchema(flowId,
                                            coalitionConfiguration.state.data.getDataTypeForCode(dataType)!!.schemaFile)

        // Initiate Provider flows and acquire encrypted payload according to given schema
        val providers = coalitionConfiguration.state.data.coalitionPartyToRole[RoleType.DATA_PROVIDER]
        val clientKeyMapWithRandomKeyGenerated = mutableMapOf<PublicKey, Pair<String, ByteArray>>()

        val providerSessions = providers!!.map { initiateFlow(it) }
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

        enclaveService.removeEnclave(flowId)

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
