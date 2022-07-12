package com.protocol180.aggregator.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.RewardsContract
import com.protocol180.aggregator.states.RewardsState
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519PrivateKey
import com.r3.conclave.mail.PostOffice
import net.corda.core.contracts.CommandData
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.internal.readFully
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.vault.AttachmentQueryCriteria
import net.corda.core.node.services.vault.AttachmentSort
import net.corda.core.node.services.vault.Builder
import net.corda.core.node.services.vault.Sort
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap
import java.time.Instant


/**
 * This flow is triggered by the [ConsumerAggregationFlowResponder] when a consumer data aggregation is requested.
 * The enclave is initiated and then the host gathers data from each of the providers in the network by kicking the
 * [ProviderAggregationResponseFlow]. The host sends providers the enclave attestation and the requested data type.
 * Providers then send back encrypted data to the host. Providers store sensitive data in CSV format as Corda
 * attachments for each supported Coalition Data Type. This data is serialised according to the relevant
 * Avro 'aggregateInput' sub-schema from the 'envelopeSchema', encrypted and sent to the host for aggregation. Providers
 * then receive their rewards as calculated by the enclave from the host. Providers create a [RewardsState] transaction and
 * sign with the host as a proof and receipt of the aggregation event.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatingFlow
@InitiatedBy(ConsumerAggregationFlowResponder::class)
open class ProviderAggregationResponseFlow(private val hostSession: FlowSession) : FlowLogic<SignedTransaction>() {

    companion object {
        private val log = loggerFor<ProviderAggregationResponseFlow>()
    }

    open fun fetchData(dataType: String, storageType: String): MutableList<String> {
        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val attachment = providerDbStoreService.retrieveProviderAggregationInputByDataType(dataType);
        return enclaveClientService.readInputDataFromAttachment(attachment!!.input)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val provider = ourIdentity
        val host = hostSession.counterparty
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val coalitionConfigurationStateService = serviceHub.cordaService(CoalitionConfigurationStateService::class.java)
        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)


        val attestationBytesAndDataType = hostSession.receive<Triple<ByteArray, String, String>>().unwrap { it }
        val attestationBytes = attestationBytesAndDataType.first
        val dataType = attestationBytesAndDataType.second
        val storageType = attestationBytesAndDataType.third
        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()

        //checking weather this node is participant for given coalition or not
        val coalitionConfiguration = coalitionConfigurationStateService.findCoalitionConfigurationStateForParticipants(listOf(provider))

        if (coalitionConfiguration == null) {
            throw ConsumerAggregationFlowException("Coalition Configuration is not known to node, host needs to update configuration and include node in participants")
        } else if (!coalitionConfiguration.state.data.isSupportedDataType(dataType)) {
            throw ConsumerAggregationFlowException("Unsupported data type requested for aggregation, please use a supported data type configured in the coalition configuration")
        }

        enclaveClientService.initializeSchema(String(coalitionConfiguration.state.data.getDataTypeForCode(dataType)!!.schemaFile))

        log.info("inside provider flow, postOffice has been created successfully")
        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        //vault query to get attachment for data type - zip file
        val recordList = fetchData(dataType, storageType);

        val headerLine = recordList.first()
        recordList.remove(headerLine)

        val providerDataPair = Pair(encryptionKey.publicKey.toString(), postOffice.encryptMail(enclaveClientService
                .createProviderDataRecordForAggregation(headerLine, recordList)!!))
        //Provider shares public key and encrypted data with host
        hostSession.send(providerDataPair)

        //Provider acknowledges rewards schema from host
        val rewardCalculationFlag = hostSession.receive<String>().unwrap { it }
        //Provider receives encrypted rewards data from enclave via host
        val encryptedRewardByteArray = hostSession.sendAndReceive<ByteArray>(postOffice.encryptMail(enclaveClientService.rewardsOutputSchema.toString().toByteArray())).unwrap { it }
        val decryptedRewardByteArray = postOffice.decryptMail(encryptedRewardByteArray).bodyAsBytes
        providerDbStoreService.addRewardResponseWithFlowId(this.runId.uuid.toString(), decryptedRewardByteArray,
                coalitionConfiguration.state.contract)

        val hostRewardsResponseSession = initiateFlow(host)
        val commandData: CommandData = RewardsContract.Commands.Create()
        val rewardsState = RewardsState(provider, host, decryptedRewardByteArray, Instant.now(),
            attestationBytes, flowTopic)

        val builder = TransactionBuilder(notary)
        builder.addOutputState(rewardsState, RewardsContract.ID)
        builder.addCommand(commandData, host.owningKey, provider.owningKey)
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)
        val fullySignedTransaction = subFlow(CollectSignaturesFlow(ptx, listOf(hostRewardsResponseSession)))
        return subFlow(FinalityFlow(fullySignedTransaction, listOf(hostRewardsResponseSession)))

    }
}

/**
 * Counter flow for [ProviderAggregationResponseFlow]. [RewardsState] is received and validated by the host
 * **/
@InitiatedBy(ProviderAggregationResponseFlow::class)
@InitiatingFlow
open class ProviderAggregationResponseFlowResponder(private val flowSession: FlowSession) : FlowLogic<Unit>() {

    companion object{
        private val log = loggerFor<ProviderAggregationResponseFlowResponder>()
    }

    @Suspendable
    override fun call() {
        //finalise rewards state creation
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {
                log.info("Checking issuance transaction before signing: ${stx.tx.id}")
                val tx = stx.toLedgerTransaction(serviceHub, false)
                tx.verify()
                val rewardsState = tx.outputStates.filterIsInstance<RewardsState>().single()
                check(rewardsState.host == ourIdentity){
                    "Reward State responder must be verified by host"
                }
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}

