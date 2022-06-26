package com.protocol180.samples.example.aggregator.clientEnclave;

import com.protocol180.aggregator.enclave.AggregationEnclave;
import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.host.EnclaveHost;
import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.MailDecryptionException;
import com.r3.conclave.mail.PostOffice;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class ExampleAggregationEnclaveTest {
    @Test
    void aggregateDataOutputTest() throws IOException, EnclaveLoadException, AggregationEnclave.UnsupportedDataTypeException, MailDecryptionException {
        EnclaveHost mockHost = EnclaveHost.load("com.protocol180.samples.example.aggregator.clientEnclave.ExampleAggregationEnclave");
        mockHost.start(null, null, null, (commands) -> {
        });

        System.out.println(ClassLoader.getSystemClassLoader().getResource("financial-services-alm.avsc"));

        File almSchemaFile = new File(ClassLoader.getSystemClassLoader().getResource("financial-services-alm.avsc").getPath());
        File almDataFile = new File(ClassLoader.getSystemClassLoader().getResource("alm.csv").getPath());
        /*
         * Directly accessing a field (or a method) of the enclave is only possible in mock mode
         */
        ExampleAggregationEnclave enclave = (ExampleAggregationEnclave) mockHost.getMockEnclave();

        Schema automotiveSchema = new Schema.Parser().parse(almSchemaFile);

        assertNotNull(mockHost.callEnclave(Files.readAllBytes(Paths.get(almSchemaFile.getPath()))));

        Curve25519PrivateKey providerPrivateKey = Curve25519PrivateKey.random();

        mockHost.deliverMail(createEncryptedClientMailForSampleCSV(mockHost.getEnclaveInstanceInfo(), almDataFile,
                automotiveSchema.getField("aggregateInput").schema(), providerPrivateKey), "provider_routing_hint");

        File dataOutputFile = enclave.createAggregateDataOutput();
        File RewardOutputFile = enclave.createRewardsDataOutput(providerPrivateKey.getPublicKey());

        assertEquals(new File("aggregateOutput.avro"), dataOutputFile);
        assertEquals(new File("rewardsOutput.avro"), RewardOutputFile);
    }

    private byte[] createEncryptedClientMailForSampleCSV(EnclaveInstanceInfo attestation, File csvFile, Schema aggregateInputSchema, Curve25519PrivateKey providerPrivateKey) throws IOException {
        PostOffice postOffice = attestation.createPostOffice(providerPrivateKey, "topic");

        List<String> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(csvFile);) {
            while (scanner.hasNextLine()) {
                records.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String headerLine = records.get(0);
        records.remove(0);

        List<GenericRecord> genericRecords = new ArrayList<>();
        String[] headers = headerLine.split("\\|");

        for (String record : records) {
            GenericRecord demandRecord = new GenericData.Record(aggregateInputSchema);
            String[] dataValues = record.split("\\|");
            int counter = 0;
            for (String header : headers) {
                if (!dataValues[counter].contains("\"")) {
                    try {
                        demandRecord.put(header, Integer.parseInt(dataValues[counter].trim()));
                    } catch (NumberFormatException e) {
                        //not int
                    }
                    try {
                        demandRecord.put(header, Float.parseFloat(dataValues[counter].trim()));
                    } catch (NumberFormatException e) {
                        //not float
                    }

                } else {
                    demandRecord.put(header, dataValues[counter].trim());
                }
                counter++;
            }
            genericRecords.add(demandRecord);
        }

        return postOffice.encryptMail(createAvroDataFileFromGenericRecords(aggregateInputSchema, genericRecords));
    }

    private byte[] createAvroDataFileFromGenericRecords(Schema schema, List<GenericRecord> genericRecords) throws IOException {
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        dataFileWriter.create(schema, byteOutputStream);
        genericRecords.forEach(genericRecord -> {
            try {
                dataFileWriter.append(genericRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        byteOutputStream.close();
        dataFileWriter.close();
        return byteOutputStream.toByteArray();
    }
}