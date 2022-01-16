package com.protocol180.aggregator.enclave;

import com.protocol180.commons.MailType;
import com.r3.conclave.enclave.Enclave;
import com.r3.conclave.mail.EnclaveMail;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

;

/**
 * Simply reverses the bytes that are passed in.
 */
public class AggregationEnclave extends Enclave {

    //Local store
    HashMap<PublicKey, byte[]> clientToEncryptedDataMap;
    HashMap<PublicKey, ArrayList<GenericRecord>> clientToRawDataMap;
    HashMap<String, String> clientIdentityStore;
    Schema aggregateInputSchema;
    Schema aggregateOutputSchema;
    Schema provenanceOutputSchema;
    Schema identitySchema;

    String clientTypeForCurrRequest = null;

    @Override
    protected byte[] receiveFromUntrustedHost(byte[] schemaBytes) {
        // This is used for host->enclave calls so we don't have to think about authentication.

        Schema envelopSchema = new Schema.Parser().parse(new String(schemaBytes));
        aggregateInputSchema = envelopSchema.getField("aggregateInput").schema();
        aggregateOutputSchema = envelopSchema.getField("aggregateOutput").schema();
        provenanceOutputSchema = envelopSchema.getField("provenanceOutput").schema();
        identitySchema = envelopSchema.getField("identity").schema();

        return "Schema Initialized".getBytes();
    }


    private void convertEncryptedClientDataToRawData() {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(aggregateInputSchema);
        clientToEncryptedDataMap.entrySet().forEach(entry ->
                {
                    try {
                        File dataFile = new File("dataFile");
                        Files.write(dataFile.toPath(), entry.getValue());
                        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(dataFile, datumReader);
                        ArrayList<GenericRecord> recordsForClient = new ArrayList<>();
                        while (dataFileReader.hasNext()) {
                            GenericRecord dataRecord = null;
                            dataRecord = dataFileReader.next(dataRecord);
                            recordsForClient.add(dataRecord);
                        }
                        System.out.println("Raw Records for client: " + recordsForClient);
                        clientToRawDataMap.put(entry.getKey(), recordsForClient);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        System.out.println("Raw Client Data to Process: " + clientToRawDataMap.toString());
    }

    private int calculateProvenanceAllocation(ArrayList<GenericRecord> records) {
        //calculations for provenance allocation on fixed income demand data
        ArrayList<Integer> allocationScores = new ArrayList<>();
        Map<String, Integer> creditRatings = Stream.of(
                new AbstractMap.SimpleEntry<>("A", 1),
                new AbstractMap.SimpleEntry<>("AA", 2),
                new AbstractMap.SimpleEntry<>("AAA", 3),
                new AbstractMap.SimpleEntry<>("B", 1),
                new AbstractMap.SimpleEntry<>("C", 0)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> sectors = Stream.of(
                new AbstractMap.SimpleEntry<>("FINANCIALS", 1),
                new AbstractMap.SimpleEntry<>("INDUSTRIALS", 2),
                new AbstractMap.SimpleEntry<>("IT", 3),
                new AbstractMap.SimpleEntry<>("INFRASTRUCTURE", 1),
                new AbstractMap.SimpleEntry<>("ENERGY", 5)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> assetTypes = Stream.of(
                new AbstractMap.SimpleEntry<>("B", 3),
                new AbstractMap.SimpleEntry<>("PP", 2),
                new AbstractMap.SimpleEntry<>("L", 1)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> durations = Stream.of(
                new AbstractMap.SimpleEntry<>("1", 1),
                new AbstractMap.SimpleEntry<>("2", 2),
                new AbstractMap.SimpleEntry<>("3", 3),
                new AbstractMap.SimpleEntry<>("4", 4),
                new AbstractMap.SimpleEntry<>("5", 5)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        records.forEach(record -> {
            Integer amount = (Integer) record.get("amount");
            allocationScores.add((creditRatings.get(record.get("creditRating").toString()) + sectors.get(record.get("sector").toString()) +
                    assetTypes.get(record.get("assetType").toString()) + durations.get(record.get("duration").toString())) + amount / 1000000);
        });
        return allocationScores.stream().mapToInt(a -> a).sum();
    }

    protected File createProvenanceDataOutput(PublicKey providerKey) throws IOException {
        //populate provenance output file here based on raw client data
        File outputFile = new File("provenanceOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(provenanceOutputSchema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(provenanceOutputSchema, outputFile);

        GenericRecord provenanceRecord = new GenericData.Record(provenanceOutputSchema);
        provenanceRecord.put("client", Base64.getEncoder().encodeToString(providerKey.getEncoded()));
        provenanceRecord.put("allocation", calculateProvenanceAllocation(clientToRawDataMap.get(providerKey)));
        try {
            dataFileWriter.append(provenanceRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataFileWriter.close();
        return outputFile;
    }

    protected File createAggregateDataOutput() throws IOException {
        //populate aggregate logic here based on raw client data and return output file
        System.out.println("Encrypted Client Data to Process: " + clientToEncryptedDataMap.toString());
        convertEncryptedClientDataToRawData();

        ArrayList<GenericRecord> allRecords = new ArrayList<>();
        clientToRawDataMap.values().forEach(genericRecords -> allRecords.addAll(genericRecords));

        File outputFile = new File("aggregateOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(aggregateOutputSchema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(aggregateOutputSchema, outputFile);


        //simple aggregation of records into one file
        //other possibilities include creating a output with a specified schema
        allRecords.forEach(genericRecord -> {
            try {
                dataFileWriter.append(genericRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        dataFileWriter.close();
        return outputFile;
    }

    protected void initializeLocalStore() {
        clientToEncryptedDataMap = new HashMap();
        clientToRawDataMap = new HashMap();
    }

    protected void clearLocalStore() {
        clientToEncryptedDataMap = null;
        clientToRawDataMap = null;
    }

    protected void putUnencryptedMailToClient(PublicKey sender, byte[] mailBytes) {
        clientToEncryptedDataMap.put(sender, mailBytes);
    }

    private void initializeLocalClientIdentityStore() {
        clientIdentityStore = new HashMap<>();
    }


    private void convertIdentitiesToRawIdentities(byte[] unencryptedMail) {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(identitySchema);
        try {
            File dataFile = new File("identityFile");
            Files.write(dataFile.toPath(), unencryptedMail);
            System.out.println("identity schema inside enclave is: " + identitySchema);
            DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(dataFile, datumReader);
            while (dataFileReader.hasNext()) {
                GenericRecord dataRecord = null;
                dataRecord = dataFileReader.next(dataRecord);
                clientIdentityStore.put(dataRecord.get("publicKey").toString(), dataRecord.get("clientType").toString());
            }
            System.out.println("Local Client Store: " + clientIdentityStore);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void receiveMail(EnclaveMail mail, String routingHint) {
        final byte[] unencryptedMail = mail.getBodyAsBytes();


        MailType mailType = getMailType(new String(unencryptedMail));
        System.out.println("Type of mail for current request is:" + mailType);

        String senderEncodedPublicKey = Base64.getEncoder().encodeToString(mail.getAuthenticatedSender().getEncoded());
        System.out.println("Clients randomly generated public key is: " + senderEncodedPublicKey);

        try {
            if (mailType.equals(MailType.TYPE_PROVIDER)) {
                clientTypeForCurrRequest = MailType.TYPE_PROVIDER.type;
                //store mail contents for aggregation
                System.out.println("Ack Mail for provider");
                if (clientToEncryptedDataMap == null && clientToRawDataMap == null) {
                    initializeLocalStore();
                }
                putUnencryptedMailToClient(mail.getAuthenticatedSender(), unencryptedMail);
                System.out.println(clientToEncryptedDataMap.size());

                final byte[] responseBytes = postOffice(mail).encryptMail(String.valueOf(clientToEncryptedDataMap.size()).getBytes());
                postMail(responseBytes, routingHint);

            } else if (mailType.equals(MailType.TYPE_CONSUMER)) {
                clientTypeForCurrRequest = MailType.TYPE_CONSUMER.type;
                //send aggregation output to consumer
                System.out.println("Aggregate Data request Mail from consumer");
                //create aggregate output
                File aggregateOutput = createAggregateDataOutput();
                System.out.println(new String(Files.readAllBytes(aggregateOutput.toPath())));
                final byte[] responseBytes = postOffice(mail).encryptMail(Files.readAllBytes(aggregateOutput.toPath()));
                postMail(responseBytes, routingHint);

            } else if (mailType.equals(MailType.TYPE_PROVENANCE)) {
                clientTypeForCurrRequest = MailType.TYPE_PROVENANCE.type;
                //send provenance result to required party
                System.out.println("Provenance Mail");
                //create provenance output
                File provenanceOutput = createProvenanceDataOutput(mail.getAuthenticatedSender());
                final byte[] responseBytes = postOffice(mail).encryptMail(Files.readAllBytes(provenanceOutput.toPath()));
                postMail(responseBytes, routingHint);

            } else {
                clientTypeForCurrRequest = null;
                System.out.println("Unauthenticated client request");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MailType getMailType(String unencryptedMail) {
        if (unencryptedMail.contains("AggregateInput"))
            return MailType.TYPE_PROVIDER;
        else if (unencryptedMail.contains("AggregateOutput"))
            return MailType.TYPE_CONSUMER;
        else if (unencryptedMail.contains("Provenance"))
            return MailType.TYPE_PROVENANCE;
        return null;
    }


}
