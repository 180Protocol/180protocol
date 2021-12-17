package com.protocol180.cordapp.aggregation.flow

import co.paralleluniverse.fibers.Suspendable
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
class ProviderAggregationResponseFlow(val hostSession: FlowSession) : FlowLogic<Unit>() {
    private val mockConstraint = "S:0000000000000000000000000000000000000000000000000000000000000000 PROD:1 SEC:INSECURE"

    @Suspendable
    override fun call() {
        val provider = ourIdentity
        val attestationBytes: ByteArray = hostSession.receive<ByteArray>().unwrap { it }

        val encryptionKey = Curve25519PrivateKey.random()
        val flowTopic: String = this.runId.uuid.toString()
        println("inside provider flow, postoffice has been created successfully")
        val postOffice: PostOffice = EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        val providerDataPair = Pair(encryptionKey.publicKey.toString(), postOffice.encryptMail("sample data".toByteArray()))

        println(providerDataPair)
        hostSession.send(providerDataPair)

    }


}

