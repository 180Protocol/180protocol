package com.protocol180.aggregator.storage.estuary;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.json.JSONArray;

import java.io.*;
import java.net.URL;

@CordaService
public class EstuaryStorageService extends SingletonSerializeAsToken {

    private final AppServiceHub serviceHub;

    public EstuaryStorageService(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
    }

    public String uploadContent(File file, String token) throws EstuaryAPICallException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://shuttle-4.estuary.tech/content/add")
                    .header("authorization", "Bearer " + token)
                    .field("data", file)
                    .asJson();

            return jsonResponse.getBody().getObject().get("cid").toString();
        } catch (UnirestException e) {
            throw new EstuaryAPICallException("Api call failed" + e.getMessage());
        }
    }

    public JSONArray fetchContent(String token) throws EstuaryAPICallException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.estuary.tech/content/list")
                    .header("authorization", "Bearer " + token)
                    .asJson();

            return jsonResponse.getBody().getArray();
        } catch (UnirestException e) {
            throw new EstuaryAPICallException("Api call failed" + e.getMessage());
        }
    }

    public JSONArray fetchContentByCid(String token, String cid) throws EstuaryAPICallException {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.estuary.tech/content/by-cid/" + cid)
                    .header("authorization", "Bearer " + token)
                    .asJson();

            return jsonResponse.getBody().getArray();
        } catch (UnirestException e) {
            throw new EstuaryAPICallException("Api call failed" + e.getMessage());
        }
    }

    public void downloadFileFromEstuary(String cid) throws IOException, EstuaryAPICallException {
        File downloadedFile = new File("downloaded.encrypted");
        downloadedFile.createNewFile();
        try (BufferedInputStream in = new BufferedInputStream(new URL("https://dweb.link/ipfs/" + cid).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream("downloaded.encrypted")) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new EstuaryAPICallException("Download file from estuary storage failed" + e.getMessage());
        }
    }
}

class EstuaryAPICallException extends Exception {
    public EstuaryAPICallException(String errorMessage) {
        super(errorMessage);
    }
}