package com.protocol180.aggregator.enclave;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.PostOffice;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MockClientUtil {

    byte[] createEncryptedClientMailForAggregationSchema(EnclaveInstanceInfo attestation) throws IOException {
        PrivateKey myKey = Curve25519PrivateKey.random();
        File aggregateFile = new File("src/test/resources/aggregate.avsc");
        PostOffice postOffice = attestation.createPostOffice(myKey, "aggregate");
        return postOffice.encryptMail(Files.readAllBytes(aggregateFile.toPath()));
    }

    byte[] createEncryptedClientMailForAggregationData(EnclaveInstanceInfo attestation) throws IOException {
        PrivateKey myKey = Curve25519PrivateKey.random();
        File aggregateFile = new File("src/test/resources/aggregate.avsc");
        Schema aggregateSchema = new Schema.Parser().parse(aggregateFile);
        //create generic records using avro schema for aggregation and append to file
        ArrayList<GenericRecord> records = createGenericSchemaRecords(aggregateSchema);
        File dataFileForAggregation = createAvroDataFileFromGenericRecords(aggregateSchema, records);
        PostOffice postOffice = attestation.createPostOffice(myKey, "aggregate");
        return postOffice.encryptMail(Files.readAllBytes(dataFileForAggregation.toPath()));
    }

    byte[] createEncryptedClientMailForProvenanceSchema(EnclaveInstanceInfo attestation) throws IOException {
        PrivateKey myKey = Curve25519PrivateKey.random();
        File provenanceFile = new File("src/test/resources/provenance.avsc");
        PostOffice postOffice = attestation.createPostOffice(myKey, "aggregate");
        return postOffice.encryptMail(Files.readAllBytes(provenanceFile.toPath()));
    }

    private static ArrayList<GenericRecord> createGenericSchemaRecords(Schema schema){
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        for(int i=0;i<2;i++){
            genericRecords.add(generateRandomDemandRecord(schema));
        }
        return genericRecords;
    }

    private static GenericRecord generateRandomDemandRecord(Schema schema){
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

    private static File createAvroDataFileFromGenericRecords(Schema schema, ArrayList<GenericRecord> genericRecords) throws IOException {
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
