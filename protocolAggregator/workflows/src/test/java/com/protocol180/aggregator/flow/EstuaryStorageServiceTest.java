package com.protocol180.aggregator.flow;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.protocol180.aggregator.utils.AESUtil;
import org.json.JSONArray;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
        String algorithm = "AES/CBC/PKCS5Padding";
        IvParameterSpec ivParameterSpec = AESUtil.generateIv();
        File encryptedFile = new File("document.encrypted");
        File decryptedFile = new File("document.decrypted");

        AESUtil.encryptFile(algorithm, key, ivParameterSpec, inputFile, encryptedFile);
        File uploadFile = new File(new File("document.encrypted").getPath());
        String cid = estuaryStorageService.uploadContent(uploadFile, token);
        JSONArray contents = estuaryStorageService.fetchContent(token);
        assertTrue(checkValueExistsInJsonArray(contents, "cid", cid));
        DownloadFileFromEstuary(cid);
        File downloadedFile = new File("downloaded.encrypted");
        AESUtil.decryptFile(algorithm, key, ivParameterSpec, downloadedFile, decryptedFile);
    }

    public void DownloadFileFromEstuary(String cid) {
        try (BufferedInputStream in = new BufferedInputStream(new URL("https://dweb.link/ipfs/" + cid).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream("downloaded.encrypted")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
        }
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