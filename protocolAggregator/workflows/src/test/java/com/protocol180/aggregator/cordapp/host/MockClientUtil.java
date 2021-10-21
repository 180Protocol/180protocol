package com.protocol180.aggregator.cordapp.host;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MockClientUtil {

    static Schema envelopeSchema;
    static Schema aggregationInputSchema;
    static Schema aggregationOutputSchema;
    static Schema provenanceOutputSchema;
    static Schema identitySchema;

    public MockClientUtil() {
        envelopeSchema = initializeSchema();
        aggregationInputSchema = envelopeSchema.getField("aggregateInput").schema();
        aggregationOutputSchema = envelopeSchema.getField("aggregateOutput").schema();
        provenanceOutputSchema = envelopeSchema.getField("provenanceOutput").schema();
        identitySchema = envelopeSchema.getField("identity").schema();
    }

    private Schema initializeSchema() {
        File schemaFile = new File(ClassLoader.getSystemClassLoader().getResource("envelope.avsc").getPath());
        Schema schema;
        try {
            schema = new Schema.Parser().parse(schemaFile);
        } catch (Exception e) {
            schema = null;
        }
        return schema;
    }

    public byte[] createMailForIdentities(Map<PublicKey, String> clients) throws IOException {
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        for (Map.Entry<PublicKey, String> clientEntry : clients.entrySet()) {

            GenericRecord demandRecord = new GenericData.Record(identitySchema);
            demandRecord.put("publicKey", Base64.getEncoder().encodeToString(clientEntry.getKey().getEncoded()));
            demandRecord.put("clientType", clientEntry.getValue());
            genericRecords.add(demandRecord);
        }

        File clientIdentitiesFile = createAvroDataFileFromGenericRecords(identitySchema, genericRecords, "identities.avro");
        return Files.readAllBytes(clientIdentitiesFile.toPath());
    }


    public byte[] createProviderMailForAggregationData() throws IOException {
        //create generic records using avro schema for aggregation and append to file
        ArrayList<GenericRecord> records = createGenericSchemaRecords(aggregationInputSchema);
        File dataFileForAggregation = createAvroDataFileFromGenericRecords(aggregationInputSchema, records, "aggregate.avro");
        return Files.readAllBytes(dataFileForAggregation.toPath());
    }

    public static File createAvroDataFileFromGenericRecords(Schema schema, ArrayList<GenericRecord> genericRecords, String filename) throws IOException {
        File file = new File(filename);
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

    public static ArrayList<GenericRecord> createGenericSchemaRecords(Schema schema) {
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            genericRecords.add(generateRandomDemandRecord(schema));
        }
        return genericRecords;
    }

    public static GenericRecord generateRandomDemandRecord(Schema schema) {
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

    public ArrayList<GenericRecord> readGenericRecordsFromOutputBytesAndSchema(byte[] outputBytes, String schemaType) throws IOException {
        DatumReader<GenericRecord> datumReader = (schemaType.equals("aggregate")) ? new GenericDatumReader<>(aggregationInputSchema) :
                new GenericDatumReader<>(provenanceOutputSchema);
        File dataFile = new File("dataFile");
        Files.write(dataFile.toPath(), outputBytes);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(dataFile, datumReader);
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        GenericRecord dataRecord = null;
        while (dataFileReader.hasNext()) {
            dataRecord = dataFileReader.next();
            System.out.println("Record: " + dataRecord);
            genericRecords.add(dataRecord);
        }
        return genericRecords;
    }

}
