package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.utils.MockClientUtil
import com.r3.conclave.common.EnclaveInstanceInfo
import com.r3.conclave.mail.Curve25519PrivateKey
import com.r3.conclave.mail.PostOffice
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.utilities.unwrap


/**
 * This is the flow which signs Aggregation Propose Transaction.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(ConsumerAggregationProposeFlowResponder::class)
//class ProviderAggregationResponseFlow(val attestation: EnclaveInstanceInfo) : FlowLogic<ByteArray>() {
class ProviderAggregationResponseFlow(private val hostSession: FlowSession) : FlowLogic<Unit>() {
    private val mockConstraint = "S:0000000000000000000000000000000000000000000000000000000000000000 PROD:1 SEC:INSECURE"

    @Suspendable
    override fun call() {
        val provider = ourIdentity
        val attestationBytesAndInputSchemaType = hostSession.receive<Pair<ByteArray, String>>().unwrap { it }

        val attestationBytes = attestationBytesAndInputSchemaType.first
        val inputSchemaType = attestationBytesAndInputSchemaType.second



        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()

        val mockClientUtil = com.protocol180.utils.MockClientUtil()
        if(!inputSchemaType.equals(MockClientUtil.aggregationInputSchema.toString()))
            throw IllegalArgumentException("Wrong schema provided from host.")

        println("inside provider flow, postOffice has been created successfully")
        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        val providerDataPair = Pair(encryptionKey.publicKey.toString(), postOffice.encryptMail(mockClientUtil.createProviderDataRecordForAggregation()))

        println(providerDataPair)
        hostSession.send(providerDataPair)

        val providerRewardSchema=hostSession.receive<String>().unwrap{it}

        val encryptedRewardByteArray=hostSession.sendAndReceive<ByteArray>(postOffice.encryptMail(providerRewardSchema.toByteArray())).unwrap{it}

        println(MockClientUtil.readGenericRecordsFromOutputBytesAndSchema(postOffice.decryptMail(encryptedRewardByteArray).bodyAsBytes,"provenance"))

    }


}

