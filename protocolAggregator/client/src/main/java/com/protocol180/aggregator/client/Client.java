package com.protocol180.aggregator.client;

import com.protocol180.aggregator.commons.MockClientUtil;
import com.protocol180.aggregator.commons.Utility;
import com.r3.conclave.client.EnclaveConstraint;
import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.PostOffice;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

public class Client {

    // Create list of identities used in network
    static Map<Curve25519PrivateKey, String> randomClients = new HashMap<>();
    static Curve25519PrivateKey provider1 = Curve25519PrivateKey.random();
    static Curve25519PrivateKey provider2 = Curve25519PrivateKey.random();
    static Curve25519PrivateKey consumer = Curve25519PrivateKey.random();
    static Curve25519PrivateKey provenance = Curve25519PrivateKey.random();

    File dataInputFileForAggregation;
    File envelopeFile;
    File identitiesFile;
    PostOffice postOffice;
    Schema aggregationOutputSchema;
    Schema provenanceOutputSchema;


    public Client() throws IOException {
        randomClients.put(provider1, Utility.CLIENT_PROVIDER);
        randomClients.put(provider2, Utility.CLIENT_PROVIDER);
        randomClients.put(consumer, Utility.CLIENT_CONSUMER);
        randomClients.put(provenance, Utility.CLIENT_PROVENANCE);
    }

    public void initClient() throws IOException {
        envelopeFile = new File(ClassLoader.getSystemClassLoader().getResource("envelope.avsc").getPath());
        Schema envelopeSchema = new Schema.Parser().parse(envelopeFile);
        Schema aggregationInputSchema = envelopeSchema.getField("aggregateInput").schema();
        aggregationOutputSchema = envelopeSchema.getField("aggregateOutput").schema();
        provenanceOutputSchema = envelopeSchema.getField("provenanceOutput").schema();
        Schema identitySchema = envelopeSchema.getField("identity").schema();

        //create generic records using avro schema for aggregation input to the enclave and append to file
        ArrayList<GenericRecord> records = MockClientUtil.createGenericSchemaRecords(aggregationInputSchema);
        dataInputFileForAggregation = MockClientUtil.createAvroDataFileFromGenericRecords(aggregationInputSchema, records, "aggregateInput.avro");

        //create generic records using identities schema for identify client inside enclave and append to file
        ArrayList<GenericRecord> identitiesRecords = new ArrayList<>();
        for (Map.Entry<Curve25519PrivateKey, String> clientEntry : randomClients.entrySet()) {
            GenericRecord demandRecord = new GenericData.Record(identitySchema);
            demandRecord.put("publicKey", Base64.getEncoder().encodeToString(clientEntry.getKey().getPublicKey().getEncoded()));
            demandRecord.put("clientType", clientEntry.getValue());
            identitiesRecords.add(demandRecord);
        }

        identitiesFile = MockClientUtil.createAvroDataFileFromGenericRecords(identitySchema, identitiesRecords, "identities.avro");


    }


    public static void main(String[] args) {


        // Connect to the host, it will send us a remote attestation (EnclaveInstanceInfo).
        try {
            Client client = new Client();
            client.initClient();
            int clientPort = 9999;

            int messageCounter = 0;
            while (messageCounter < 5) {
                System.out.println("Sending Mail: " + messageCounter + " to Host.");
                Thread.sleep(20000);

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), clientPort), 5000);

                DataInputStream fromHost = new DataInputStream(socket.getInputStream());
                DataOutputStream toHost = new DataOutputStream(socket.getOutputStream());


                //Reading enclave attestation from host and validating it.
                byte[] attestationBytes = new byte[fromHost.readInt()];
                fromHost.readFully(attestationBytes);
                EnclaveInstanceInfo attestation = EnclaveInstanceInfo.deserialize(attestationBytes);
                // Check it's the enclave we expect. This will throw InvalidEnclaveException if not valid.
                System.out.println("Connected to " + attestation);
                EnclaveConstraint.parse("S:360585776942A4E8A6BD70743E7C114A81F9E901BF90371D27D55A241C738AD9 "
                        + "S:4924CA3A9C8241A3C0AA1A24A407AA86401D2B79FA9FF84932DA798A942166D4 PROD:1 SEC:INSECURE").check(attestation);


                byte[] encryptedMail = client.getEncryptedMail(messageCounter, attestation);
                toHost.writeInt(encryptedMail.length);
                toHost.write(encryptedMail);
                toHost.flush();

                if (messageCounter == 3 || messageCounter == 4) {
                    Thread.sleep(3000);
                    byte[] encryptedReply = new byte[fromHost.readInt()];
                    fromHost.readFully(encryptedReply);
                    System.out.println("Reading reply mail of length " + encryptedReply.length + " bytes.");
                    // The same post office will decrypt the response.
                    EnclaveMail reply = client.postOffice.decryptMail(encryptedReply);
                    ArrayList<GenericRecord> outputRecords = null;
                    if (messageCounter == 3)
                        outputRecords = readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), client.aggregationOutputSchema);
                    else if (messageCounter == 4)
                        outputRecords = readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), client.provenanceOutputSchema);
                    System.out.println("Enclave reply: '" + outputRecords + "'");
                }
                toHost.close();
                fromHost.close();
                socket.close();
                messageCounter++;
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static ArrayList<GenericRecord> readGenericRecordsFromOutputBytesAndSchema(byte[] outputBytes, Schema schema) throws IOException {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        File dataFile = new File("outputDataFile");
        Files.write(dataFile.toPath(), outputBytes);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(dataFile, datumReader);
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        GenericRecord dataRecord = null;
        while (dataFileReader.hasNext()) {
            dataRecord = dataFileReader.next(dataRecord);
            genericRecords.add(dataRecord);
        }
        return genericRecords;
    }

    private byte[] getEncryptedMail(int messageCounter, EnclaveInstanceInfo attestation) throws IOException {
        byte[] encryptedMail;
        if (messageCounter == 0) {
            postOffice = attestation.createPostOffice(Curve25519PrivateKey.random(), "aggregate");
            encryptedMail = postOffice.encryptMail(Files.readAllBytes(envelopeFile.toPath()));
        } else if (messageCounter == 1) {
            postOffice = attestation.createPostOffice(Curve25519PrivateKey.random(), "aggregate");
            encryptedMail = postOffice.encryptMail(Files.readAllBytes(identitiesFile.toPath()));
        } else if (messageCounter == 2) {
            postOffice = attestation.createPostOffice(provider1, "aggregate");
            encryptedMail = postOffice.encryptMail(Files.readAllBytes(dataInputFileForAggregation.toPath()));
        } else if (messageCounter == 3) {
            postOffice = attestation.createPostOffice(consumer, "aggregate");
            encryptedMail = postOffice.encryptMail("test consumer".getBytes());
        } else {

            postOffice = attestation.createPostOffice(provenance, "aggregate");
            encryptedMail = postOffice.encryptMail("test provenance".getBytes());
        }

        return encryptedMail;
    }

}
