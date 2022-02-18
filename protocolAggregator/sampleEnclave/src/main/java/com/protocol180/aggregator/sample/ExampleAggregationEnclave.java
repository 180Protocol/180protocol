package com.protocol180.aggregator.sample;

import com.protocol180.aggregator.enclave.AggregationEnclave;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

/**
 * The below enclave provides an example of how to write 180Protocol Broker Flow compatible Enclaves.
 * Example enclaves must handle data output and rewards calculations for coalition supported data types. These data
 * types and their associated schemas must be indicated as an Enum inside the Enclave.
 * The 'createRewardsDataOutput' and 'createAggregateDataOutput' methods must be designed to handle computations for each
 * of the supported data types (and their corresponding schemas).
 * **/
public class ExampleAggregationEnclave extends AggregationEnclave {

    enum SupportedDataTypes {
        testSchema1,
        testSchema2
    }

    Random random = new Random();

    /**
     * method defined on AggregationEnclave interface that is overridden in the child enclave.
     * Used to calculate rewards for a specific provider.
     * Accepts the key of the provider for which the Rewards computation is done. Future implementations will support
     * calling a Rewards engine that calculates rewards factors automatically and based on regression.
     * **/
    protected File createRewardsDataOutput(PublicKey providerKey) throws IOException, UnsupportedDataTypeException {

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
            default: throw new UnsupportedDataTypeException("Envelope Schema contains unsupported data type: " + envelopeSchema.getName());
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
     * **/
    @Override
    protected File createAggregateDataOutput() throws IOException, UnsupportedDataTypeException {
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
                allRecords.forEach(genericRecord -> {
                    try {
                        dataFileWriter.append(genericRecord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case testSchema2:
                allRecords.forEach(genericRecord -> {
                    try {
                        dataFileWriter.append(genericRecord);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            default: throw new UnsupportedDataTypeException("Envelope Schema contains unsupported data type: " + envelopeSchema.getName());
        }


        dataFileWriter.close();
        return outputFile;
    }

    private float getRandomNumber(int minLimit, int maxLimit, double decimalPlace) {
        return (float) (Math.round(((minLimit + random.nextFloat() * (maxLimit - minLimit)) * decimalPlace)) / decimalPlace);
    }


}
