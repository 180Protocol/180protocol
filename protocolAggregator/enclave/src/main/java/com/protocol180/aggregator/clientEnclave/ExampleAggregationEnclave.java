package com.protocol180.aggregator.clientEnclave;

import com.protocol180.aggregator.enclave.AggregationEnclave;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleAggregationEnclave extends AggregationEnclave {

    final String TEST_SCHEMA_1 = "testSchema1";
    final String TEST_SCHEMA_2 = "testSchema2";

    protected File createRewardsDataOutput(PublicKey providerKey) throws IOException {

        //populate rewards output file here based on raw client data
        File outputFile = new File("rewardsOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(rewardsOutputSchema);

        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(rewardsOutputSchema, outputFile);

        GenericRecord rewardRecord = new GenericData.Record(rewardsOutputSchema);

        switch (envelopeSchema.getName()) {
            case TEST_SCHEMA_1:
                rewardRecord.put("client", Base64.getEncoder().encodeToString(providerKey.getEncoded()));
                rewardRecord.put("allocation", calculateRewardsForSchema1(clientToRawDataMap.get(providerKey)));
                break;
            case TEST_SCHEMA_2:
                rewardRecord.put("client", Base64.getEncoder().encodeToString(providerKey.getEncoded()));
                rewardRecord.put("allocation", calculateRewardsForSchema2(clientToRawDataMap.get(providerKey)));
                break;
            default:
        }
        try {
            dataFileWriter.append(rewardRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataFileWriter.close();
        return outputFile;
    }

    private Object calculateRewardsForSchema2(ArrayList<GenericRecord> records) {
        //calculations for rewards allocation on fixed income demand data
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

    private Object calculateRewardsForSchema1(ArrayList<GenericRecord> records) {
        //calculations for rewards allocation on fixed income demand data
        ArrayList<Integer> allocationScores = new ArrayList<>();
        Map<Integer, Integer> riskScore = Stream.of(
                new AbstractMap.SimpleEntry<>(50, 2),
                new AbstractMap.SimpleEntry<>(100, 3),
                new AbstractMap.SimpleEntry<>(30, 1),
                new AbstractMap.SimpleEntry<>(80, 3),
                new AbstractMap.SimpleEntry<>(60, 2)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> company = Stream.of(
                new AbstractMap.SimpleEntry<>("FINANCIALS", 1),
                new AbstractMap.SimpleEntry<>("INDUSTRIALS", 2),
                new AbstractMap.SimpleEntry<>("IT", 3),
                new AbstractMap.SimpleEntry<>("INFRASTRUCTURE", 1),
                new AbstractMap.SimpleEntry<>("ENERGY", 5)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> region = Stream.of(
                new AbstractMap.SimpleEntry<>("EUROPE", 3),
                new AbstractMap.SimpleEntry<>("AMERICA", 3),
                new AbstractMap.SimpleEntry<>("CHINA", 1),
                new AbstractMap.SimpleEntry<>("ASIA", 2)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        records.forEach(record -> {
            allocationScores.add((riskScore.get((Integer) record.get("riskScore")) + company.get(record.get("company").toString()) +
                    region.get(record.get("region").toString())));
        });
        return allocationScores.stream().mapToInt(a -> a).sum();

    }

    @Override
    protected File createAggregateDataOutput() throws IOException {
        //populate aggregate logic here based on raw client data and return output file
        convertEncryptedClientDataToRawData();

        ArrayList<GenericRecord> allRecords = new ArrayList<>();
        clientToRawDataMap.values().forEach(genericRecords -> allRecords.addAll(genericRecords));

        File outputFile = new File("aggregateOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(aggregateOutputSchema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(aggregateOutputSchema, outputFile);


        //simple aggregation of records into one file
        //other possibilities include creating a output with a specified schema

        switch (envelopeSchema.getName()) {
            case TEST_SCHEMA_1:
                allRecords.forEach(genericRecord -> {
                    try {
                        dataFileWriter.append(genericRecord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case TEST_SCHEMA_2:
                allRecords.forEach(genericRecord -> {
                    try {
                        dataFileWriter.append(genericRecord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
        }


        dataFileWriter.close();
        return outputFile;
    }

}
