package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.RewardsContract
import com.protocol180.aggregator.states.RewardsState
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519PrivateKey
import com.r3.conclave.mail.PostOffice
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
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

    companion object{
        private val log = loggerFor<ProviderAggregationResponseFlow>()
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val provider = ourIdentity
        val host = hostSession.counterparty
        val notary = serviceHub.networkMapCache.notaryIdentities.single()

        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)

        val attestationBytesAndInputSchemaType = hostSession.receive<Pair<ByteArray, String>>().unwrap { it }
        val attestationBytes = attestationBytesAndInputSchemaType.first
        val inputSchemaType = attestationBytesAndInputSchemaType.second
        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()

        if (inputSchemaType != enclaveClientService.aggregationInputSchema.toString())
            throw IllegalArgumentException("Wrong schema provided from host.")
        log.info("inside provider flow, postOffice has been created successfully")
        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)
        //vault query to get attachment for data type - zip file
        //convert zip to
        val providerDataPair = Pair(encryptionKey.publicKey.toString(), postOffice.encryptMail(enclaveClientService.createProviderDataRecordForAggregation()!!))
        //Provider shares public key and encrypted data with host
        hostSession.send(providerDataPair)

        //Provider acknowledges rewards schema from host
        val providerRewardSchema = hostSession.receive<String>().unwrap { it }
        //Provider receives encrypted rewards data from enclave via host
        val encryptedRewardByteArray = hostSession.sendAndReceive<ByteArray>(postOffice.encryptMail(providerRewardSchema.toByteArray())).unwrap { it }
        providerDbStoreService.addRewardResponseWithFlowId(this.runId.uuid.toString(), postOffice.decryptMail(encryptedRewardByteArray).bodyAsBytes, "Temp_Data_Type")
        log.info("Provider Rewards: " + enclaveClientService.readGenericRecordsFromOutputBytesAndSchema(providerDbStoreService.retrieveRewardResponseWithFlowId(this.runId.uuid.toString())!!, "provenance"))


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
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}

