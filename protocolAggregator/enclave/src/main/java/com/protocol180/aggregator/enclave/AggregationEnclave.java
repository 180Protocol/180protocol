package com.protocol180.aggregator.enclave;

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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simply reverses the bytes that are passed in.
 */
public class AggregationEnclave extends Enclave {
    // We store the previous result to showcase that the enclave internals can be examined in a mock test.
    byte[] previousResult;

    //not needed - used for demonstration of comms with host
    @Override
    protected byte[] receiveFromUntrustedHost(byte[] bytes) {
        // This is used for host->enclave calls so we don't have to think about authentication.
        final String input = new String(bytes);
        byte[] result = reverse(input).getBytes();
        previousResult = result;
        return result;
    }

    private static String reverse(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = input.length() - 1; i >= 0; i--)
            builder.append(input.charAt(i));
        return builder.toString();
    }

    private void convertEncryptedClientDataToRawData(){
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(aggregateSchema);
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

    private int calculateProvenanceAllocation(ArrayList<GenericRecord> records){
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
                    assetTypes.get(record.get("assetType").toString()) + durations.get(record.get("duration").toString())) + amount/1000000);
        });
        return allocationScores.stream().mapToInt(a -> a).sum();
    }

    protected File createProvenanceDataOutput() throws IOException{
        //populate provenance output file here based on raw client data
        File outputFile = new File("provenanceOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(provenanceSchema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(provenanceSchema, outputFile);

        clientToRawDataMap.entrySet().forEach(entry -> {
            GenericRecord provenanceRecord = new GenericData.Record(provenanceSchema);
            provenanceRecord.put("client", entry.getKey().toString());
            provenanceRecord.put("allocation", calculateProvenanceAllocation(entry.getValue()));
            try {
                dataFileWriter.append(provenanceRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        dataFileWriter.close();
        return outputFile;
    }

    protected File createAggregateDataOutput() throws IOException{
        //populate aggregate logic here based on raw client data and return output file
        System.out.println("Encrypted Client Data to Process: " + clientToEncryptedDataMap.toString());
        convertEncryptedClientDataToRawData();

        ArrayList<GenericRecord> allRecords = new ArrayList<>();
        clientToRawDataMap.values().forEach(genericRecords -> allRecords.addAll(genericRecords));

        File outputFile = new File("aggregateOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(aggregateSchema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(aggregateSchema, outputFile);


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

    protected void initializeLocalStore(){
        clientToEncryptedDataMap = new HashMap();
        clientToRawDataMap = new HashMap();
    }

    protected void clearLocalStore(){
        clientToEncryptedDataMap = null;
        clientToRawDataMap = null;
    }

    protected void putUnencryptedMailToClient(PublicKey sender, byte[] mailBytes){
        clientToEncryptedDataMap.put(sender, mailBytes);
    }

    @Override
    protected void receiveMail(long id, EnclaveMail mail, String routingHint) {
        final byte[] unencryptedMail = mail.getBodyAsBytes();
        final PublicKey sender = mail.getAuthenticatedSender();
        if (sender == null)
            throw new IllegalArgumentException("Mail sent to this enclave must be authenticated so we can reply.");
        try {

            if(routingHint.equals("schema")){
                //Read and store data input schema file
                //File aggregateSchemaFile = new File("aggregateSchemaFile");
                Path aggregateSchemaFilePath = Paths.get("aggregateSchemaFile");
                //Path aggregateSchemaFile = Files.createFile(aggregateSchemaFilePath);
                SeekableByteChannel sbc = Files.newByteChannel(aggregateSchemaFilePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
                sbc.write(ByteBuffer.wrap(unencryptedMail));
                sbc.close();
                //Files.write(aggregateSchemaFile, unencryptedMail);
                aggregateSchema = new Schema.Parser().parse(aggregateSchemaFilePath.toFile());
                acknowledgeMail(id);
            }
            else if (routingHint.equals("self")) {
                //store mail contents for aggregation
                System.out.println("Ack Mail");
                if(clientToEncryptedDataMap == null && clientToRawDataMap == null){
                    initializeLocalStore();
                }
                putUnencryptedMailToClient(sender, unencryptedMail);
                acknowledgeMail(id);
            } else if (routingHint.equals("consumer")) {
                //send aggregation output to consumer
                System.out.println("Aggregate Mail");
                putUnencryptedMailToClient(sender, unencryptedMail);
                //create aggregate output
                File aggregateOutput = createAggregateDataOutput();
                final byte[] responseBytes = postOffice(mail).encryptMail(Files.readAllBytes(aggregateOutput.toPath()));
                postMail(responseBytes, routingHint);
            } else if (routingHint.equals("provenance")) {
                //send provenance result to required party
                System.out.println("Provenance Mail");
                // Read and store provenance schema
                File provenanceSchemaFile = new File("provenanceSchemaFile");
                Files.write(provenanceSchemaFile.toPath(), unencryptedMail);
                provenanceSchema = new Schema.Parser().parse(provenanceSchemaFile);
                //create provenance output
                File provenanceOutput = createProvenanceDataOutput();
                final byte[] responseBytes = postOffice(mail).encryptMail(Files.readAllBytes(provenanceOutput.toPath()));
                postMail(responseBytes, routingHint);
                clearLocalStore();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //Local store
    HashMap<PublicKey, byte[]> clientToEncryptedDataMap;
    HashMap<PublicKey, ArrayList<GenericRecord>> clientToRawDataMap;
    Schema aggregateSchema;
    Schema provenanceSchema;
}
