package com.protocol180.aggregator.commons;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.PostOffice;
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
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MockClientUtil {

    public MockClientUtil(){
        postOfficeMap = new HashMap<>();
        aggregationSchema = initializeSchema("aggregate");
        provenanceSchema = initializeSchema("provenance");
    }

    public static HashMap<PostOfficeMapKey, PostOffice> postOfficeMap;
    public static String topic = "aggregate";
    static Schema aggregationSchema;
    static Schema provenanceSchema;

    public Schema initializeSchema(String schemaType){
        File schemaFile = new File("src/test/resources/envelope.avsc");
        Schema schema;
        try{
            schema = new Schema.Parser().parse(schemaFile).getField(schemaType).schema();
        }
        catch (Exception e){
            schema = null;
        }
        return  schema;
    }

    public byte[] createEncryptedClientMailForAggregationSchema(EnclaveInstanceInfo attestation) throws IOException {
        PrivateKey myKey = Curve25519PrivateKey.random();
//        File aggregateFile = new File("src/test/resources/aggregate.avsc");
        PostOffice postOffice = attestation.createPostOffice(myKey, topic);
        return postOffice.encryptMail(aggregationSchema.toString().getBytes());
    }

    public PrivateKeyAndEncryptedBytes createEncryptedClientMailForAggregationData(EnclaveInstanceInfo attestation) throws IOException {
        PrivateKey myKey = Curve25519PrivateKey.random();
        //create generic records using avro schema for aggregation and append to file
        ArrayList<GenericRecord> records = createGenericSchemaRecords(aggregationSchema);
        File dataFileForAggregation = createAvroDataFileFromGenericRecords(aggregationSchema, records);
        PostOffice postOffice = attestation.createPostOffice(myKey, topic);
        postOfficeMap.put(new PostOfficeMapKey(myKey, attestation.getEncryptionKey(), topic), postOffice);
        return new PrivateKeyAndEncryptedBytes(myKey, postOffice.encryptMail(Files.readAllBytes(dataFileForAggregation.toPath())));
    }

    public ArrayList<GenericRecord> readGenericRecordsFromOutputBytesAndSchema(byte[] outputBytes, String schema) throws IOException {
        DatumReader<GenericRecord> datumReader = (schema.equals("aggregate")) ? new GenericDatumReader<>(aggregationSchema) :
                new GenericDatumReader<>(provenanceSchema);
        File dataFile = new File("dataFile");
        Files.write(dataFile.toPath(), outputBytes);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(dataFile, datumReader);
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        GenericRecord dataRecord = null;
        while (dataFileReader.hasNext()) {
            dataRecord = dataFileReader.next(dataRecord);
            System.out.println("Record: " + dataRecord);
            genericRecords.add(dataRecord);
        }
        return genericRecords;
    }

    public PrivateKeyAndEncryptedBytes createEncryptedClientMailForProvenanceSchema(EnclaveInstanceInfo attestation) throws IOException {
        PrivateKey myKey = Curve25519PrivateKey.random();
//        File provenanceFile = new File("src/test/resources/provenance.avsc");
        PostOffice postOffice = attestation.createPostOffice(myKey, topic);
        postOfficeMap.put(new PostOfficeMapKey(myKey, attestation.getEncryptionKey(), topic), postOffice);
        return new PrivateKeyAndEncryptedBytes(myKey, postOffice.encryptMail(provenanceSchema.toString().getBytes()));
    }

    public static ArrayList<GenericRecord> createGenericSchemaRecords(Schema schema){
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        for(int i=0;i<2;i++){
            genericRecords.add(generateRandomDemandRecord(schema));
        }
        return genericRecords;
    }

    public static GenericRecord generateRandomDemandRecord(Schema schema){
        String[] creditRatings = {"A", "AA", "AAA", "B", "C"};
        String[] sectors = {"FINANCIALS", "INDUSTRIALS", "IT", "INFRASTRUCTURE", "ENERGY"};
        String[] assetTypes = {"B", "PP", "L"};
        String[] duration = {"1", "2", "3", "4", "5"};
        GenericRecord demandRecord = new GenericData.Record(schema);
        Random randomizer = new Random();
        demandRecord.put("creditRating", creditRatings[randomizer.nextInt(creditRatings.length)]);
        demandRecord.put("sector", sectors[randomizer.nextInt(sectors.length)]);
        demandRecord.put("assetType", assetTypes[randomizer.nextInt(assetTypes.length)]);
        demandRecord.put("duration", duration[randomizer.nextInt(duration.length)]);
        demandRecord.put("amount", ThreadLocalRandom.current().nextInt(1000000, 1000000000 + 1));
        return demandRecord;
    }

    public static File createAvroDataFileFromGenericRecords(Schema schema, ArrayList<GenericRecord> genericRecords) throws IOException {
        File file = new File("src/test/resources/aggregate.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(schema, file);
        genericRecords.forEach(genericRecord -> {
            try {
                dataFileWriter.append(genericRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        dataFileWriter.close();
        return file;
    }

}
