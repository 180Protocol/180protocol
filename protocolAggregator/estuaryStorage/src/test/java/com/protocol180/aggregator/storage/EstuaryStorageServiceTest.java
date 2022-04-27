package com.protocol180.aggregator.storage;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.protocol180.aggregator.utils.AESUtil;
import org.json.JSONArray;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class EstuaryStorageServiceTest {
    EstuaryStorageService estuaryStorageService = new EstuaryStorageService();
    String token = "EST8752f2e8-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXX"; // Api key to authenticate estuary apis.

    @Test
    public void uploadTest() throws UnirestException {
        File uploadFile = new File(ClassLoader.getSystemClassLoader().getResource("Provider2InputData.csv").getPath());
        String cid = estuaryStorageService.uploadContent(uploadFile, token);
        JSONArray contents = estuaryStorageService.fetchContent(token);
        assertTrue(checkValueExistsInJsonArray(contents, "cid", cid));
    }

    @Test
    public void fetchTest() throws UnirestException {
        JSONArray contents = estuaryStorageService.fetchContent(token);
        assertFalse(contents.getJSONObject(0).has("error"));
    }

    @Test
    public void fetchByCidTest() throws UnirestException {
        JSONArray contents = estuaryStorageService.fetchContent(token);
        JSONArray info = estuaryStorageService.fetchContentByCid(token, contents.getJSONObject(0).getString("cid"));
        assertEquals(contents.getJSONObject(0).getString("cid"), info.getJSONObject(0).getJSONObject("content").getString("cid"));
    }

    @Test
    public void uploadEncryptDecryptDataTest() throws UnirestException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
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
}