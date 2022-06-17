package com.protocol180.aggregator.storage.keyVault;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.protocol180.aggregator.storage.utils.AESUtil;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureKeyVaultServiceTest {
    String tenantId = "c7e48871-70ff-48d5-8934-a8e594bc040b";
    String clientId = "dd115d03-42ee-43db-98e1-8b6a475275f8";
    String clientSecret = "KEZmQT-~mvUlNS61E1ZBj75W~DrSTZtYKO";
    String keyIdentifier = "https://keyvaulttest180p.vault.azure.net/keys/180PTest/06ff554fc7af4be685eec9f841ce2e60";

    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a key in the key vault.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    @Test
    public void keyWrapUnwrapTest() throws IllegalArgumentException, NoSuchAlgorithmException {
        // Instantiate a key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        ClientSecretCredential credential =
                new ClientSecretCredentialBuilder().tenantId(tenantId).clientId(clientId).clientSecret(clientSecret).build();

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .credential(credential)
                .keyIdentifier(keyIdentifier)
                .buildClient();

        byte[] key = AESUtil.generateKey(256).getEncoded();
        System.out.println(key.length);

        // Let's wrap a simple dummy key content.
        WrapResult wrapResult = cryptoClient.wrapKey(KeyWrapAlgorithm.RSA_OAEP, key);
        System.out.printf("Returned encrypted key size is %d bytes with algorithm %s\n", wrapResult.getEncryptedKey().length, wrapResult.getAlgorithm().toString());

        //Let's unwrap the encrypted key response.
        UnwrapResult unwrapResult = cryptoClient.unwrapKey(KeyWrapAlgorithm.RSA_OAEP, wrapResult.getEncryptedKey());
        System.out.printf("Returned unwrapped key size is %d bytes\n", unwrapResult.getKey().length);

        assertEquals(key.length, unwrapResult.getKey().length);
    }
}
