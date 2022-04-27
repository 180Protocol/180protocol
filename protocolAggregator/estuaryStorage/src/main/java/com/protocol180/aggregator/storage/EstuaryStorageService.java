package com.protocol180.aggregator.storage;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;

import java.io.*;
import java.net.URL;


public class EstuaryStorageService {
    public String uploadContent(File file, String token) throws UnirestException {
        HttpResponse<JsonNode> jsonResponse = Unirest.post("https://shuttle-4.estuary.tech/content/add")
                .header("authorization", "Bearer " + token)
                .field("data", file)
                .asJson();

        return jsonResponse.getBody().getObject().get("cid").toString();
    }

    public JSONArray fetchContent(String token) throws UnirestException {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.estuary.tech/content/list")
                .header("authorization", "Bearer " + token)
                .asJson();

        return jsonResponse.getBody().getArray();
    }

    public JSONArray fetchContentByCid(String token, String cid) throws UnirestException {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.estuary.tech/content/by-cid/" + cid)
                .header("authorization", "Bearer " + token)
                .asJson();

        return jsonResponse.getBody().getArray();
    }

    public void downloadFileFromEstuary(String cid) throws IOException {
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
            // handle exception
        }
    }
}
