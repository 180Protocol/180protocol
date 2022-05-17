package com.protocol180.aggregator.storage.estuary;

import com.google.common.collect.ImmutableList;
import com.protocol180.aggregator.storage.utils.AESUtil;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.NetworkParameters;
import net.corda.testing.node.*;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class EstuaryStorageServiceTest {
    /*
    private MockNetwork network;
    private StartedMockNode consumer1;

    EstuaryStorageService estuaryStorageService;
    String token = "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"; // Api key to authenticate estuary apis.

    @Before
    public void setup() {
        network = new MockNetwork(
                new MockNetworkParameters(
                        ImmutableList.of(TestCordapp.findCordapp("com.protocol180.aggregator.storage"))));
        consumer1 = network.createPartyNode(null);
        network.runNetwork();
        estuaryStorageService = new EstuaryStorageService((AppServiceHub) consumer1.getServices());
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void uploadTest() throws EstuaryAPICallException {
        File uploadFile = new File(ClassLoader.getSystemClassLoader().getResource("Provider2InputData.csv").getPath());
        String cid = estuaryStorageService.uploadContent(uploadFile, token);
        JSONArray contents = estuaryStorageService.fetchContent(token);
        assertTrue(checkValueExistsInJsonArray(contents, "cid", cid));
    }

    @Test
    public void fetchTest() throws EstuaryAPICallException {
        JSONArray contents = estuaryStorageService.fetchContent(token);
        assertFalse(contents.getJSONObject(0).has("error"));
    }

    @Test
    public void fetchByCidTest() throws EstuaryAPICallException {
        JSONArray contents = estuaryStorageService.fetchContent(token);
        JSONArray info = estuaryStorageService.fetchContentByCid(token, contents.getJSONObject(0).getString("cid"));
        assertEquals(contents.getJSONObject(0).getString("cid"), info.getJSONObject(0).getJSONObject("content").getString("cid"));
    }

    @Test
    public void uploadEncryptDecryptDataTest() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException, EstuaryAPICallException {
        File inputFile = new File(ClassLoader.getSystemClassLoader().getResource("Provider2InputData.csv").getPath());
        SecretKey key = AESUtil.generateKey(256);
        IvParameterSpec ivParameterSpec = AESUtil.generateIv();
        File encryptedFile = new File("document.encrypted");
        File decryptedFile = new File("document.decrypted");
        AESUtil.encryptFile(key, ivParameterSpec, Files.readAllBytes(Paths.get(inputFile.getPath())), encryptedFile);
        File uploadFile = new File(new File("document.encrypted").getPath());
        String cid = estuaryStorageService.uploadContent(uploadFile, token);
        JSONArray contents = estuaryStorageService.fetchContent(token);
        assertTrue(checkValueExistsInJsonArray(contents, "cid", cid));
        estuaryStorageService.downloadFileFromEstuary(cid);
        File downloadedFile = new File("downloaded.encrypted");
        AESUtil.decryptFile(key, ivParameterSpec, downloadedFile, decryptedFile);
        encryptedFile.delete();
        downloadedFile.delete();
        decryptedFile.delete();
    }

    public boolean checkValueExistsInJsonArray(JSONArray array, String key, String value) {
        boolean check = false;
        for (int i = 0; i < array.length(); i++) {
            if (Objects.equals(array.getJSONObject(i).getString(key), value)) {
                check = true;
            }
        }
        return check;
    }

     */
}