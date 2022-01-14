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
import net.corda.core.node.services.vault.Builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap
import java.time.Instant


/**
 * This is the flow which signs Aggregation Propose Transaction.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatingFlow
@InitiatedBy(ConsumerAggregationFlowResponder::class)
class ProviderAggregationResponseFlow(private val hostSession: FlowSession) : FlowLogic<SignedTransaction>() {

    companion object {
        private val log = loggerFor<ProviderAggregationResponseFlow>()
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val provider = ourIdentity
        val host = hostSession.counterparty
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val coalitionConfigurationStateService = serviceHub.cordaService(CoalitionConfigurationStateService::class.java)
        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)


        val attestationBytesAndDataType = hostSession.receive<Pair<ByteArray, String>>().unwrap { it }
        val attestationBytes = attestationBytesAndDataType.first
        val dataType = attestationBytesAndDataType.second
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
        val listOfAttachmentHash: List<AttachmentId> = serviceHub.attachments.queryAttachments(AttachmentQueryCriteria.AttachmentsQueryCriteria(uploaderCondition = Builder.equal(dataType)))

        val attachment = serviceHub.attachments.openAttachment(listOfAttachmentHash.single())

        val recordList = enclaveClientService.readInputDataFromAttachment(attachment!!.open().readFully())


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
        providerDbStoreService.addRewardResponseWithFlowId(this.runId.uuid.toString(), postOffice.decryptMail(encryptedRewardByteArray).bodyAsBytes,
                coalitionConfiguration.state.contract)
        log.info("Provider Rewards: " + enclaveClientService.readGenericRecordsFromOutputBytesAndSchema(providerDbStoreService
                .retrieveRewardResponseWithFlowId(this.runId.uuid.toString())!!, "rewards"))


        val hostRewardsResponseSession = initiateFlow(host)
        val commandData: CommandData = RewardsContract.Commands.Create()
        val rewardsState = RewardsState(provider, host, encryptedRewardByteArray, Instant.now(),
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

@InitiatedBy(ProviderAggregationResponseFlow::class)
@InitiatingFlow
class ProviderAggregationResponseFlowResponder(private val flowSession: FlowSession) : FlowLogic<Unit>() {

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

