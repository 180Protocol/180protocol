package com.protocol180.aggregator.storage.flow

import co.paralleluniverse.fibers.Suspendable
import com.protocol180.aggregator.contracts.RewardsContract
import com.protocol180.aggregator.flow.*
import com.protocol180.aggregator.states.RewardsState
import com.protocol180.aggregator.storage.estuary.EstuaryStorageService
import com.protocol180.aggregator.storage.keyVault.AzureKeyVaultService
import com.protocol180.aggregator.storage.utils.AESUtil
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
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.unwrap
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.spec.IvParameterSpec


@InitiatedBy(ConsumerAggregationFlowResponder::class)
class EstuaryStorageProviderAggregationResponseFlow(
    private val hostSession: FlowSession
) : ProviderAggregationResponseFlow(hostSession) {

    companion object {
        private val log = loggerFor<ProviderAggregationResponseFlow>()
    }

    override fun fetchData(dataType: String, storageType: String): MutableList<String> {
        val providerDbStoreService = serviceHub.cordaService(ProviderDBStoreService::class.java)
        val enclaveClientService = serviceHub.cordaService(EnclaveClientService::class.java)
        val attachment = providerDbStoreService.retrieveProviderAggregationInputByDataType(dataType);
        if (storageType == "local") {
            return enclaveClientService.readInputDataFromAttachment(attachment!!.input)
        } else {
            val decentralizedStorageEncryptionKeyService =
                serviceHub.cordaService(DecentralizedStorageEncryptionKeyService::class.java)
            val estuaryStorageService = serviceHub.cordaService(EstuaryStorageService::class.java);
            val azureKeyVaultService = serviceHub.cordaService(AzureKeyVaultService::class.java);
            val tenantId = serviceHub.cordaService(NetworkParticipantService::class.java).tenantId;
            val clientId = serviceHub.cordaService(NetworkParticipantService::class.java).clientId;
            val clientSecret = serviceHub.cordaService(NetworkParticipantService::class.java).clientSecret;
            val keyIdentifier = serviceHub.cordaService(NetworkParticipantService::class.java).keyIdentifier;
            estuaryStorageService.downloadFileFromEstuary(attachment!!.cid);
            val decentralizedStorageEncryptionKeyRecord =
                attachment!!.encryptionKeyId?.let {
                    decentralizedStorageEncryptionKeyService.retrieveDecentralizedStorageEncryptionKeyWithFlowId(
                        it
                    )
                };
            val downloadedFile = File(File("downloaded.encrypted").path);
            val decryptedFile = File(File("document.decrypted").path);
            val decryptedDek = azureKeyVaultService.unWrapKey(
                tenantId,
                clientId,
                clientSecret,
                keyIdentifier,
                decentralizedStorageEncryptionKeyRecord!!.key
            );
            AESUtil.decryptFile(
                AESUtil.convertBytesToSecretKey(decryptedDek), IvParameterSpec(
                    decentralizedStorageEncryptionKeyRecord.ivParameterSpec
                ), downloadedFile, decryptedFile
            );
            val encoded: ByteArray = Files.readAllBytes(Paths.get(File("document.decrypted").path))
            downloadedFile.delete();
            decryptedFile.delete();
            return enclaveClientService.readInputDataFromAttachment(encoded)
        }
    }
}