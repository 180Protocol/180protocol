package com.protocol180.aggregator.storage.keyVault;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;

import java.security.NoSuchAlgorithmException;

@CordaService
public class AzureKeyVaultService extends SingletonSerializeAsToken {

    private final AppServiceHub serviceHub;

    public AzureKeyVaultService(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
    }

    public byte[] wrapKey(String tenantId, String clientId, String clientSecret, String keyIdentifier, byte[] key) throws IllegalArgumentException, NoSuchAlgorithmException {
        ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(tenantId).clientId(clientId).clientSecret(clientSecret).build();

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .credential(credential)
                .keyIdentifier(keyIdentifier)
                .buildClient();

        WrapResult wrapResult = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key);
        return wrapResult.getEncryptedKey();
    }

    public byte[] unWrapKey(String tenantId, String clientId, String clientSecret, String keyIdentifier, byte[] key) throws IllegalArgumentException, NoSuchAlgorithmException {
        ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(tenantId).clientId(clientId).clientSecret(clientSecret).build();

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .credential(credential)
                .keyIdentifier(keyIdentifier)
                .buildClient();

        UnwrapResult unwrapResult = cryptoClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, key);
        return unwrapResult.getKey();
    }
}
