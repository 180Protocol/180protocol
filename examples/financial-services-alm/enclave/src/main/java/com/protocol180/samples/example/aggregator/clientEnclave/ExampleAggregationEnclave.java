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
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
        alm
    }

    Random random = new Random();

    ArrayList<String> pivot = new ArrayList<String>() {
        {
            add("month");
            add("brand");
            add("type");
            add("model");
        }
    };

    public static class RiskData {
        public Double governmentRisk;
        public Double totalRisk;

        RiskData(Double governmentRisk, Double totalRisk){
            this.governmentRisk = governmentRisk;
            this.totalRisk = totalRisk;
        }
    }

    static final HashMap<Integer, RiskData> riskDataHashMap = new HashMap<Integer, RiskData>(){{
        put(1, new RiskData(1.0,1.25)); put(2, new RiskData(1.5,2.0)); put(3, new RiskData(1.536666667,2.12)); put(4, new RiskData(1.573333333,2.24));
        put(5, new RiskData(1.61,2.36)); put(6, new RiskData(1.66,2.46)); put(7, new RiskData(1.71,2.56)); put(8, new RiskData(1.76,2.66));
        put(9, new RiskData(1.81,2.76)); put(10, new RiskData(1.86,2.86)); put(11, new RiskData(1.867,2.8795)); put(12, new RiskData(1.874,2.899));
        put(13, new RiskData(1.881,2.9185)); put(14, new RiskData(1.888,2.938)); put(15, new RiskData(1.895,2.9575)); put(16, new RiskData(1.902,2.977));
        put(17, new RiskData(1.909,2.9965)); put(18, new RiskData(1.916,3.016)); put(19, new RiskData(1.923,3.0355)); put(20, new RiskData(1.93,3.055));
        put(21, new RiskData(1.937,3.0745)); put(22, new RiskData(1.944,3.094)); put(23, new RiskData(1.951,3.1135)); put(24, new RiskData(1.958,3.133));
        put(25, new RiskData(1.965,3.1525)); put(26, new RiskData(1.972,3.172)); put(27, new RiskData(1.979,3.1915)); put(28, new RiskData(1.986,3.211));
        put(29, new RiskData(1.993,3.2305)); put(30, new RiskData(2.0,3.25)); put(31, new RiskData(2.0,3.25)); put(32, new RiskData(2.0,3.25));
        put(33, new RiskData(2.0,3.25)); put(34, new RiskData(2.0,3.25)); put(35, new RiskData(2.0,3.25)); put(36, new RiskData(2.0,3.25));
        put(37, new RiskData(2.0,3.25)); put(38, new RiskData(2.0,3.25)); put(39, new RiskData(2.0,3.25)); put(40, new RiskData(2.0,3.25));
        put(41, new RiskData(2.0,3.25)); put(42, new RiskData(2.0,3.25)); put(43, new RiskData(2.0,3.25)); put(44, new RiskData(2.0,3.25));
        put(45, new RiskData(2.0,3.25)); put(46, new RiskData(2.0,3.25)); put(47, new RiskData(2.0,3.25)); put(48, new RiskData(2.0,3.25));
        put(49, new RiskData(2.0,3.25)); put(50, new RiskData(2.0,3.25));
    }};


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

        //TODO: insert logic for factors
        /*float amountProvided = (float) clientRecords.size() / (float) allRecords.size();
        float completeness = (float) groupByModelCountryAndCalculateCount(clientRecords, "model", "country") / (float) groupByModelCountryAndCalculateCount(allRecords, "model", "country");
        float uniqueness = (float) groupByAndCalculateCount(clientRecords, pivot.get(2)) / (float) groupByAndCalculateCount(allRecords, pivot.get(2));
        float updateFrequency = (float) groupByDateAndCalculateCount(clientRecords, "date") / (float) groupByDateAndCalculateCount(allRecords, "date");*/

        float amountProvided = (float) clientRecords.size() / (float) allRecords.size();
        float completeness = (float) 0.674;
        float uniqueness = (float) 0.56;
        float updateFrequency = (float) 0.88;

        float qualityScore = (amountProvided + completeness + uniqueness + updateFrequency) / 4;
        float rewards = qualityScore * 100;

        switch (SupportedDataTypes.valueOf(envelopeSchema.getName())) {
            case alm:
                rewardRecord.put("amountProvided", amountProvided);
                rewardRecord.put("completeness", completeness);
                rewardRecord.put("uniqueness", uniqueness);
                rewardRecord.put("updateFrequency", updateFrequency);
                rewardRecord.put("qualityScore", qualityScore);
                rewardRecord.put("rewards", rewards);
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
            case alm:
                HashMap<Integer, ArrayList<GenericRecord>> allRecordsByYear = mapRecordsByYear(allRecords);
                AtomicReference<Double> averageSurplusOrDeficitDiscounted = new AtomicReference<>(0.0);
                allRecordsByYear.keySet().forEach(year -> {
                        GenericRecord netAlmRecord = new GenericData.Record(aggregateOutputSchema);
                        //populate output fields
                        netAlmRecord.put("year", year.intValue());
                        netAlmRecord.put("netAlmCashBasis", calculateNetAlmCashBasisForYear(allRecordsByYear.get(year), year));
                        Double netAlmDiscountedBasis = calculateNetAlmDiscountedBasisForYear(allRecordsByYear.get(year), year);
                        averageSurplusOrDeficitDiscounted.set(averageSurplusOrDeficitDiscounted.get() + netAlmDiscountedBasis);
                        netAlmRecord.put("netAlmDiscountedBasis", netAlmDiscountedBasis);
                        netAlmRecord.put("averageSurplusOrDeficitDiscounted", averageSurplusOrDeficitDiscounted.get());
                        //write record to output
                        try {
                            dataFileWriter.append(netAlmRecord);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                );
                break;
            default:
                throw new IOException("Envelope Schema contains unsupported data type: " + envelopeSchema.getName());
        }


        dataFileWriter.close();
        return outputFile;
    }

    private double calculateNetAlmCashBasisForYear(ArrayList<GenericRecord> allRecordsForYear, Integer year){
        Double allLiabilitiesForYear = allRecordsForYear.stream().mapToDouble(it ->
                        it.hasField("liabilities") ? (Double)it.get("liabilities") : 0.0 ).sum();
        Double allAssetsForYear = allRecordsForYear.stream().mapToDouble(it ->
                it.hasField("assets") ? (Double)it.get("assets") : 0.0 ).sum();
        return allAssetsForYear - allLiabilitiesForYear;
    }

    private double calculateNetAlmDiscountedBasisForYear(ArrayList<GenericRecord> allRecordsForYear, Integer year){
        RiskData riskDataForYear = riskDataHashMap.get(year);
        Double allDiscountedLiabilitiesForYear = allRecordsForYear.stream().mapToDouble(it ->
                it.hasField("liabilities") ?
                        (Double)it.get("liabilities")/Math.pow((1 + (riskDataForYear.governmentRisk/100)), year.doubleValue()) : 0.0 ).sum();
                //map(it -> it/Math.pow((1 + (riskDataForYear.governmentRisk/100)), year.doubleValue())).sum();
        Double allDiscountedAssetsForYear = allRecordsForYear.stream().mapToDouble(it ->
                        it.hasField("assets") ?
                                (Double)it.get("assets")/Math.pow((1 + (riskDataForYear.totalRisk/100)), year.doubleValue()) : 0.0 ).sum();
                //map(it -> it/Math.pow((1 + (riskDataForYear.totalRisk/100)), year.doubleValue())).sum();
        return allDiscountedAssetsForYear - allDiscountedLiabilitiesForYear;
    }

    private HashMap<Integer, ArrayList<GenericRecord>> mapRecordsByYear(ArrayList<GenericRecord> allRecords){
        HashMap<Integer, ArrayList<GenericRecord>> allRecordsByYear = new HashMap();
        allRecords.forEach(genericRecord -> {
            if(genericRecord.hasField("year") && !allRecordsByYear.containsKey((Integer)genericRecord.get("year"))){
                ArrayList<GenericRecord> recordsByYear = new ArrayList();
                recordsByYear.add(genericRecord);
                allRecordsByYear.put((Integer)genericRecord.get("year"), recordsByYear);
            } else if(genericRecord.hasField("year") && allRecordsByYear.containsKey((Integer)genericRecord.get("year"))){
                allRecordsByYear.get((Integer)genericRecord.get("year")).add(genericRecord);
            }
        });
        return allRecordsByYear;
    }

    private int groupByAndCalculateCount(ArrayList<GenericRecord> allRecords, String groupByField) {
        Map<Object, Long> counted = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(groupByField),
                        Collectors.counting()
                ));

        return counted.size();
    }

    private int groupByModelCountryAndCalculateCount(ArrayList<GenericRecord> allRecords, String model, String country) {
        Map<Object, Map<Object, Long>> groupedRecords = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(model),
                        Collectors.groupingBy(
                                genericRecord -> genericRecord.get(country),
                                Collectors.counting())
                ));

        int count = 0;
        for (Map.Entry<Object, Map<Object, Long>> entry : groupedRecords.entrySet()) {
            Object key = entry.getKey();
            Map<Object, Long> value = entry.getValue();
            count += value.size();
        }

        return count;
    }

    private int groupByDateAndCalculateCount(ArrayList<GenericRecord> allRecords, String date) {
        Map<Object, Long> groupByDateRecords = allRecords.stream()
                .collect(Collectors.groupingBy(
                        genericRecord -> genericRecord.get(date),
                        Collectors.counting()
                ));

        LocalDate currentDate = LocalDate.now().minusMonths(3);
        long total = 0;
        for (Map.Entry<Object, Long> entry : groupByDateRecords.entrySet()) {
            Object k = entry.getKey();
            Long v = entry.getValue();
            if (LocalDate.parse(k.toString().substring(1, k.toString().length() - 1)).isAfter(currentDate)) {
                total += v;
            }
        }

        return (int) total;
    }
}