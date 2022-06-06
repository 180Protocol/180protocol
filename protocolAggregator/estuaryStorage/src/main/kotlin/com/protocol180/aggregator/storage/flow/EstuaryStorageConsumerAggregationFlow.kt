package com.protocol180.aggregator.storage.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.DataOutputContract
import com.protocol180.aggregator.flow.*
import com.protocol180.aggregator.states.DataOutputState
import com.protocol180.aggregator.states.RoleType
import com.protocol180.aggregator.storage.estuary.EstuaryStorageService
import com.protocol180.aggregator.storage.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.storage.utils.AESUtil
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
import java.io.File
import java.security.PublicKey
import java.time.Instant
import javax.crypto.spec.IvParameterSpec

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
@StartableByRPC
class EstuaryStorageConsumerAggregationFlow(
    private val dataType: String,
    private val description: String,
    private val storageType: String
) : ConsumerAggregationFlow(dataType, description, storageType) {

    companion object {
        private val log = loggerFor<EstuaryStorageConsumerAggregationFlow>()
    }

    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val coalitionConfigurationStateService = serviceHub.cordaService(CoalitionConfigurationStateService::class.java)
        val decentralizedStorageEncryptionKeyService = serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val estuaryStorageService = serviceHub.cordaService(EstuaryStorageService::class.java);
        val azureKeyVaultService = serviceHub.cordaService(AzureKeyVaultService::class.java);
        val token = serviceHub.cordaService(NetworkParticipantService::class.java).token;
        val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
        val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
        val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
        val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;

        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val consumer = ourIdentity

        val coalitionConfiguration =
            coalitionConfigurationStateService.findCoalitionConfigurationStateForParticipants(listOf(ourIdentity))

        if (coalitionConfiguration == null) {
            throw EstuaryStorageConsumerAggregationFlowException("Coalition Configuration is not known to node, host needs to update configuration and include node in participants")
        } else if (!coalitionConfiguration.state.data.isSupportedDataType(dataType)) {
            throw EstuaryStorageConsumerAggregationFlowException("Unsupported data type requested for aggregation, please use a supported data type configured in the coalition configuration")
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
        val postOffice: PostOffice =
            EnclaveInstanceInfo.deserialize(attestationBytes).createPostOffice(encryptionKey, flowTopic)

        //send data output schema to be aggregated to host
        val encryptedAggregationDataRecordBytes = hostSession.sendAndReceive<ByteArray>(
            postOffice.encryptMail(
                enclaveClientService
                    .aggregationOutputSchema.toString().toByteArray()
            )
        ).unwrap { it }
        val decryptedAggregationDataRecordBytes =
            postOffice.decryptMail(encryptedAggregationDataRecordBytes).bodyAsBytes
        //Store aggregation output data received from enclave into consumer's local db
        val decentralizedStorageEncryptionKeyRecord =
            decentralizedStorageEncryptionKeyService.retrieveLatestDecentralizedStorageEncryptionKey();
        val encryptedFile = File("document.encrypted")
        val decryptedDek = azureKeyVaultService.unWrapKey(tenantId, clientId, clientSecret, keyIdentifier, decentralizedStorageEncryptionKeyRecord!!.key);
        AESUtil.encryptFile(
            AESUtil.convertBytesToSecretKey(decryptedDek),
            IvParameterSpec(decentralizedStorageEncryptionKeyRecord!!.ivParameterSpec),
            decryptedAggregationDataRecordBytes,
            encryptedFile
        )
        val uploadFile = File(File("document.encrypted").path)
        val cid = estuaryStorageService.uploadContent(uploadFile, token)
        val encryptionKeyId = decentralizedStorageEncryptionKeyRecord!!.flowId;
        encryptedFile.delete();

        //optional reading of records - needed for the front end read flow
        val commandData: CommandData = DataOutputContract.Commands.Issue()
        val dataOutputState = DataOutputState(
            consumer,
            host,
            dataType,
            description,
            Instant.now(),
            attestationBytes,
            flowTopic,
            encryptionKeyId,
            storageType,
            cid
        );

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
 * Thrown when the Consumer Aggregation Flow fails
 */
class EstuaryStorageConsumerAggregationFlowException(private val reason: String) :
    FlowException("Consumer Aggregation Flow failed: $reason")