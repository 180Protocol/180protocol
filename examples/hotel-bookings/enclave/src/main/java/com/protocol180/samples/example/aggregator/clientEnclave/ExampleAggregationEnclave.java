package com.protocol180.samples.example.aggregator.clientEnclave;

import com.protocol180.aggregator.enclave.AggregationEnclave;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The below enclave provides an example of how to write 180Protocol Broker Flow compatible Enclaves.
 * Example enclaves must handle data output and rewards calculations for coalition supported data types. These data
 * types and their associated schemas must be indicated as an Enum inside the Enclave.
 * The 'createRewardsDataOutput' and 'createAggregateDataOutput' methods must be designed to handle computations for each
 * of the supported data types (and their corresponding schemas).
 **/
public class ExampleAggregationEnclave extends AggregationEnclave {

    enum SupportedDataTypes {
        testSchema1,
        testSchema2
    }

    Random random = new Random();

    ArrayList<String> pivot = new ArrayList<String>() {
        {
            add("customerAge");
            add("partySize");
            add("bookedOnline");
            add("roomPrice");
        }
    };

    /**
     * method defined on AggregationEnclave interface that is overridden in the child enclave.
     * Used to calculate rewards for a specific provider.
     * Accepts the key of the provider for which the Rewards computation is done. Future implementations will support
     * calling a Rewards engine that calculates rewards factors automatically and based on regression.
     **/
    protected File createRewardsDataOutput(PublicKey providerKey) throws IOException {
        ArrayList<GenericRecord> clientRecords = clientToRawDataMap.get(providerKey);
        ArrayList<GenericRecord> allRecords = new ArrayList<>();
        clientToRawDataMap.values().forEach(genericRecords -> allRecords.addAll(genericRecords));

        //populate rewards output file here based on raw client data
        File outputFile = new File("rewardsOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(rewardsOutputSchema);

        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(rewardsOutputSchema, outputFile);

        GenericRecord rewardRecord = new GenericData.Record(rewardsOutputSchema);

        int generalMinLimit = 1;
        int generalMaxLimit = 10;
        int finalRewardMinLimit = 1;
        int finalRewardMaxLimit = 1000;

        switch (SupportedDataTypes.valueOf(envelopeSchema.getName())) {
            case testSchema1:
                rewardRecord.put("amountProvided", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("completeness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("uniqueness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("updateFrequency", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("qualityScore", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("rewards", getRandomNumber(finalRewardMinLimit, finalRewardMaxLimit, 1));
                rewardRecord.put("dataType", envelopeSchema.getName());
                break;
            case testSchema2:
                rewardRecord.put("amountProvided", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("completeness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("uniqueness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("updateFrequency", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("qualityScore", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("rewards", getRandomNumber(finalRewardMinLimit, finalRewardMaxLimit, 1));
                rewardRecord.put("dataType", envelopeSchema.getName());
                break;
            default:
                throw new IOException("Envelope Schema contains unsupported data type: " + envelopeSchema.getName());
        }
        try {
            dataFileWriter.append(rewardRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataFileWriter.close();
        return outputFile;
    }

    /**
     * method defined on AggregationEnclave interface that is overridden in the child enclave.
     * Used to calculate data output for a specific consumer.
     **/
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
        switch (SupportedDataTypes.valueOf(envelopeSchema.getName())) {
            case testSchema1:
                GenericRecord demandRecord = new GenericData.Record(aggregateOutputSchema);
                GenericRecord cancellationsByAgeRangeRecord = new GenericData.Record(aggregateOutputSchema.getField("cancellationsByAgeRange").schema());
                GenericRecord cancellationsByPartySizeRange = new GenericData.Record(aggregateOutputSchema.getField("cancellationsByPartySizeRange").schema());
                GenericRecord cancellationsByOnlineBookingRange = new GenericData.Record(aggregateOutputSchema.getField("cancellationsByOnlineBookingRange").schema());
                GenericRecord furnishingByAgeRange = new GenericData.Record(aggregateOutputSchema.getField("furnishingByAgeRange").schema());
                GenericRecord furnishingByPriceRange = new GenericData.Record(aggregateOutputSchema.getField("furnishingByPriceRange").schema());
                GenericRecord furnishingByPartySizeRange = new GenericData.Record(aggregateOutputSchema.getField("furnishingByPartySizeRange").schema());
                GenericRecord noOfBookingsByMonth = new GenericData.Record(aggregateOutputSchema.getField("noOfBookingsByMonth").schema());
                GenericRecord avgPriceOfBookingsByMonth = new GenericData.Record(aggregateOutputSchema.getField("avgPriceOfBookingsByMonth").schema());
                GenericRecord avgPartySizeByMonth = new GenericData.Record(aggregateOutputSchema.getField("avgPartySizeByMonth").schema());

                cancellationsByAgeRangeRecord.put("pivotId", pivot.get(0));
                cancellationsByAgeRangeRecord.put("data", groupByAgePercentage(allRecords, pivot.get(0), "cancellation"));
                cancellationsByPartySizeRange.put("pivotId", pivot.get(1));
                cancellationsByPartySizeRange.put("data", groupByPartySizePercentage(allRecords, pivot.get(1), "cancellation"));
                cancellationsByOnlineBookingRange.put("pivotId", pivot.get(2));
                cancellationsByOnlineBookingRange.put("data", groupByBookedOnlineCancellationPercentage(allRecords, pivot.get(2), "cancellation"));
                furnishingByAgeRange.put("pivotId", pivot.get(0));
                furnishingByAgeRange.put("data", groupByAgePercentage(allRecords, pivot.get(0), "furnished"));
                furnishingByPriceRange.put("pivotId", pivot.get(3));
                furnishingByPriceRange.put("data", groupByRoomPricePercentage(allRecords, pivot.get(3), "furnished"));
                furnishingByPartySizeRange.put("pivotId", pivot.get(1));
                furnishingByPartySizeRange.put("data", groupByPartySizePercentage(allRecords, pivot.get(1), "furnished"));
                noOfBookingsByMonth.put("pivotId", "month");
                noOfBookingsByMonth.put("data", groupByAndCalculateCount(allRecords, "bookingDate"));
                avgPriceOfBookingsByMonth.put("pivotId", "month");
                avgPriceOfBookingsByMonth.put("data", groupByAndCalculateAverage(allRecords, "bookingDate", "roomPrice"));
                avgPartySizeByMonth.put("pivotId", "month");
                avgPartySizeByMonth.put("data", groupByAndCalculateAverage(allRecords, "bookingDate", "partySize"));

                demandRecord.put("cancellationsByAgeRange", cancellationsByAgeRangeRecord);
                demandRecord.put("cancellationsByPartySizeRange", cancellationsByPartySizeRange);
                demandRecord.put("cancellationsByOnlineBookingRange", cancellationsByOnlineBookingRange);
                demandRecord.put("furnishingByAgeRange", furnishingByAgeRange);
                demandRecord.put("furnishingByPriceRange", furnishingByPriceRange);
                demandRecord.put("furnishingByPartySizeRange", furnishingByPartySizeRange);
                demandRecord.put("noOfBookingsByMonth", noOfBookingsByMonth);
                demandRecord.put("avgPriceOfBookingsByMonth", avgPriceOfBookingsByMonth);
                demandRecord.put("avgPartySizeByMonth", avgPartySizeByMonth);
                dataFileWriter.append(demandRecord);
                break;
            case testSchema2:
                GenericRecord demandRecord2 = new GenericData.Record(aggregateOutputSchema);
                GenericRecord cancellationsByAgeRangeRecord2 = new GenericData.Record(aggregateOutputSchema.getField("cancellationsByAgeRange").schema());
                GenericRecord cancellationsByPartySizeRange2 = new GenericData.Record(aggregateOutputSchema.getField("cancellationsByPartySizeRange").schema());
                GenericRecord cancellationsByOnlineBookingRange2 = new GenericData.Record(aggregateOutputSchema.getField("cancellationsByOnlineBookingRange").schema());
                GenericRecord furnishingByAgeRange2 = new GenericData.Record(aggregateOutputSchema.getField("furnishingByAgeRange").schema());
                GenericRecord furnishingByPriceRange2 = new GenericData.Record(aggregateOutputSchema.getField("furnishingByPriceRange").schema());
                GenericRecord furnishingByPartySizeRange2 = new GenericData.Record(aggregateOutputSchema.getField("furnishingByPartySizeRange").schema());
                GenericRecord noOfBookingsByMonth2 = new GenericData.Record(aggregateOutputSchema.getField("noOfBookingsByMonth").schema());
                GenericRecord avgPriceOfBookingsByMonth2 = new GenericData.Record(aggregateOutputSchema.getField("avgPriceOfBookingsByMonth").schema());
                GenericRecord avgPartySizeByMonth2 = new GenericData.Record(aggregateOutputSchema.getField("avgPartySizeByMonth").schema());

                cancellationsByAgeRangeRecord2.put("pivotId", pivot.get(0));
                cancellationsByAgeRangeRecord2.put("data", groupByAgePercentage(allRecords, pivot.get(0), "cancellation"));
                cancellationsByPartySizeRange2.put("pivotId", pivot.get(1));
                cancellationsByPartySizeRange2.put("data", groupByPartySizePercentage(allRecords, pivot.get(1), "cancellation"));
                cancellationsByOnlineBookingRange2.put("pivotId", pivot.get(2));
                cancellationsByOnlineBookingRange2.put("data", groupByBookedOnlineCancellationPercentage(allRecords, pivot.get(2), "cancellation"));
                furnishingByAgeRange2.put("pivotId", pivot.get(0));
                furnishingByAgeRange2.put("data", groupByAgePercentage(allRecords, pivot.get(0), "furnished"));
                furnishingByPriceRange2.put("pivotId", pivot.get(3));
                furnishingByPriceRange2.put("data", groupByRoomPricePercentage(allRecords, pivot.get(3), "furnished"));
                furnishingByPartySizeRange2.put("pivotId", pivot.get(1));
                furnishingByPartySizeRange2.put("data", groupByPartySizePercentage(allRecords, pivot.get(1), "furnished"));
                noOfBookingsByMonth2.put("pivotId", "month");
                noOfBookingsByMonth2.put("data", groupByAndCalculateCount(allRecords, "bookingDate"));
                avgPriceOfBookingsByMonth2.put("pivotId", "month");
                avgPriceOfBookingsByMonth2.put("data", groupByAndCalculateAverage(allRecords, "bookingDate", "roomPrice"));
                avgPartySizeByMonth2.put("pivotId", "month");
                avgPartySizeByMonth2.put("data", groupByAndCalculateAverage(allRecords, "bookingDate", "partySize"));

                demandRecord2.put("cancellationsByAgeRange", cancellationsByAgeRangeRecord2);
                demandRecord2.put("cancellationsByPartySizeRange", cancellationsByPartySizeRange2);
                demandRecord2.put("cancellationsByOnlineBookingRange", cancellationsByOnlineBookingRange2);
                demandRecord2.put("furnishingByAgeRange", furnishingByAgeRange2);
                demandRecord2.put("furnishingByPriceRange", furnishingByPriceRange2);
                demandRecord2.put("furnishingByPartySizeRange", furnishingByPartySizeRange2);
                demandRecord2.put("noOfBookingsByMonth", noOfBookingsByMonth2);
                demandRecord2.put("avgPriceOfBookingsByMonth", avgPriceOfBookingsByMonth2);
                demandRecord2.put("avgPartySizeByMonth", avgPartySizeByMonth2);
                dataFileWriter.append(demandRecord2);
                break;
            default:
                throw new IOException("Envelope Schema contains unsupported data type: " + envelopeSchema.getName());
        }


        dataFileWriter.close();
        return outputFile;
    }

    private float getRandomNumber(int minLimit, int maxLimit, double decimalPlace) {
        return (float) (Math.round(((minLimit + random.nextFloat() * (maxLimit - minLimit)) * decimalPlace)) / decimalPlace);
    }

    public HashMap<String, Double> groupByAndCalculateAverage(ArrayList<GenericRecord> allRecords, String groupByField, String field) {
        HashMap<String, Double> data = new HashMap<>();
        allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.summarizingDouble(genericRecord -> (int) genericRecord.get(field))
                )).forEach((k, v) -> {
                    data.put(k.toString(), v.getSum() / v.getCount());
                });

        return data;
    }

    public Map<String, Integer> groupByAndCalculateCount(ArrayList<GenericRecord> allRecords, String groupByField) {
        HashMap<String, Integer> data = new HashMap<>();
        allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.counting()
                )).forEach((k, v) -> {
                    data.put(k.toString(), v.intValue());
                });

        return data;
    }

    public HashMap<String, Double> groupByAgePercentage(ArrayList<GenericRecord> allRecords, String groupByField, String field) {
        HashMap<String, Double> data = new HashMap<>();
        Map<Object, Map<Object, Long>> groupedRecords = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.groupingBy(
                                genericRecord -> genericRecord.get(field),
                                Collectors.counting())
                ));

        long total18to30 = 0, true18to30Total = 0;
        long total31to45 = 0, true31to45Total = 0;
        long total46to60 = 0, true46to60Total = 0;
        long totalAbove60 = 0, trueAbove60Total = 0;

        for (Map.Entry<Object, Map<Object, Long>> entry : groupedRecords.entrySet()) {
            Object key = entry.getKey();
            Map<Object, Long> value = entry.getValue();
            int age = Integer.parseInt(key.toString());
            if (age >= 18 && age <= 30) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true18to30Total = true18to30Total + valueRecord.getValue();
                    }
                    total18to30 = total18to30 + valueRecord.getValue();
                }
            } else if (age >= 31 && age <= 45) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true31to45Total = true31to45Total + valueRecord.getValue();
                    }
                    total31to45 = total31to45 + valueRecord.getValue();
                }
            } else if (age >= 46 && age <= 60) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true46to60Total = true46to60Total + valueRecord.getValue();
                    }
                    total46to60 = total46to60 + valueRecord.getValue();
                }
            } else {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        trueAbove60Total = trueAbove60Total + valueRecord.getValue();
                    }
                    totalAbove60 = totalAbove60 + valueRecord.getValue();
                }
            }
        }


        data.put("18-30", total18to30 > 0 ? (double) ((true18to30Total * 100) / total18to30) : 0);
        data.put("31-45", total31to45 > 0 ? (double) ((true31to45Total * 100) / total31to45) : 0);
        data.put("46-60", total46to60 > 0 ? (double) ((true46to60Total * 100) / total46to60) : 0);
        data.put("60+", totalAbove60 > 0 ? (double) ((trueAbove60Total * 100) / totalAbove60) : 0);
        return data;
    }

    public HashMap<String, Double> groupByPartySizePercentage(ArrayList<GenericRecord> allRecords, String groupByField, String field) {
        HashMap<String, Double> data = new HashMap<>();
        Map<Object, Map<Object, Long>> groupedRecords = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.groupingBy(
                                genericRecord -> genericRecord.get(field),
                                Collectors.counting())
                ));

        long total1to3 = 0, true1to3Total = 0;
        long total4to6 = 0, true4to6Total = 0;
        long total7to10 = 0, true7to10Total = 0;
        long totalAbove10 = 0, trueAbove10Total = 0;

        for (Map.Entry<Object, Map<Object, Long>> entry : groupedRecords.entrySet()) {
            Object key = entry.getKey();
            Map<Object, Long> value = entry.getValue();
            int partySize = Integer.parseInt(key.toString());
            if (partySize >= 1 && partySize <= 3) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true1to3Total = true1to3Total + valueRecord.getValue();
                    }
                    total1to3 = total1to3 + valueRecord.getValue();
                }
            } else if (partySize >= 4 && partySize <= 6) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true4to6Total = true4to6Total + valueRecord.getValue();
                    }
                    total4to6 = total4to6 + valueRecord.getValue();
                }
            } else if (partySize >= 7 && partySize <= 10) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true7to10Total = true7to10Total + valueRecord.getValue();
                    }
                    total7to10 = total7to10 + valueRecord.getValue();
                }
            } else {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        trueAbove10Total = trueAbove10Total + valueRecord.getValue();
                    }
                    totalAbove10 = totalAbove10 + valueRecord.getValue();
                }
            }
        }

        data.put("1-3", total1to3 > 0 ? (double) ((true1to3Total * 100) / total1to3) : 0);
        data.put("4-6", total4to6 > 0 ? (double) ((true4to6Total * 100) / total4to6) : 0);
        data.put("7-10", total7to10 > 0 ? (double) ((true7to10Total * 100) / total7to10) : 0);
        data.put("10+", totalAbove10 > 0 ? (double) ((trueAbove10Total * 100) / totalAbove10) : 0);
        return data;
    }

    public HashMap<String, Double> groupByRoomPricePercentage(ArrayList<GenericRecord> allRecords, String groupByField, String field) {
        HashMap<String, Double> data = new HashMap<>();
        Map<Object, Map<Object, Long>> groupedRecords = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.groupingBy(
                                genericRecord -> genericRecord.get(field),
                                Collectors.counting())
                ));

        long total15to30 = 0, true15to30Total = 0;
        long total31to45 = 0, true31to45Total = 0;
        long total46to60 = 0, true46to60Total = 0;
        long total61to90 = 0, true61to90Total = 0;
        long totalAbove90 = 0, trueAbove90Total = 0;

        for (Map.Entry<Object, Map<Object, Long>> entry : groupedRecords.entrySet()) {
            Object key = entry.getKey();
            Map<Object, Long> value = entry.getValue();
            int price = Integer.parseInt(key.toString());
            if (price >= 15 && price <= 30) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true15to30Total = true15to30Total + valueRecord.getValue();
                    }
                    total15to30 = total15to30 + valueRecord.getValue();
                }
            } else if (price >= 31 && price <= 45) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true31to45Total = true31to45Total + valueRecord.getValue();
                    }
                    total31to45 = total31to45 + valueRecord.getValue();
                }
            } else if (price >= 46 && price <= 60) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true46to60Total = true46to60Total + valueRecord.getValue();
                    }
                    total46to60 = total46to60 + valueRecord.getValue();
                }
            } else if (price >= 61 && price <= 90) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        true61to90Total = true61to90Total + valueRecord.getValue();
                    }
                    total61to90 = total61to90 + valueRecord.getValue();
                }
            } else {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        trueAbove90Total = trueAbove90Total + valueRecord.getValue();
                    }
                    totalAbove90 = totalAbove90 + valueRecord.getValue();
                }
            }
        }

        data.put("15-30", total15to30 > 0 ? (double) ((true15to30Total * 100) / total15to30) : 0);
        data.put("31-45", total31to45 > 0 ? (double) ((true31to45Total * 100) / total31to45) : 0);
        data.put("46-60", total46to60 > 0 ? (double) ((true46to60Total * 100) / total46to60) : 0);
        data.put("61-90", total61to90 > 0 ? (double) ((true61to90Total * 100) / total61to90) : 0);
        data.put("90+", totalAbove90 > 0 ? (double) ((trueAbove90Total * 100) / totalAbove90) : 0);
        return data;
    }

    public HashMap<String, Double> groupByBookedOnlineCancellationPercentage(ArrayList<GenericRecord> allRecords, String groupByField, String field) {
        HashMap<String, Double> data = new HashMap<>();
        Map<Object, Map<Object, Long>> groupedRecords = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.groupingBy(
                                genericRecord -> genericRecord.get("cancellation"),
                                Collectors.counting())
                ));

        long bookedOnline = 0, cancellationTrueBookedOnlineTotal = 0;
        long bookedOffline = 0, cancellationTrueBookedOfflineTotal = 0;

        for (Map.Entry<Object, Map<Object, Long>> entry : groupedRecords.entrySet()) {
            Object key = entry.getKey();
            Map<Object, Long> value = entry.getValue();
            String booking = key.toString();
            if (Objects.equals(booking, "true")) {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        cancellationTrueBookedOnlineTotal = cancellationTrueBookedOnlineTotal + valueRecord.getValue();
                    }
                    bookedOnline = bookedOnline + valueRecord.getValue();
                }
            } else {
                for (Map.Entry<Object, Long> valueRecord : value.entrySet()) {
                    if (Objects.equals(valueRecord.getKey().toString(), "true")) {
                        cancellationTrueBookedOfflineTotal = cancellationTrueBookedOfflineTotal + valueRecord.getValue();
                    }
                    bookedOffline = bookedOffline + valueRecord.getValue();
                }
            }
        }

        data.put("online", bookedOnline > 0 ? (double) ((cancellationTrueBookedOnlineTotal * 100) / bookedOnline) : 0);
        data.put("offline", bookedOffline > 0 ? (double) ((cancellationTrueBookedOfflineTotal * 100) / bookedOffline) : 0);
        return data;
    }
}