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
import java.util.ArrayList;
import java.util.Random;

public class ExampleAggregationEnclave extends AggregationEnclave {

    final String TEST_SCHEMA_1 = "testSchema1";
    final String TEST_SCHEMA_2 = "testSchema2";

    Random random = new Random();

    protected File createRewardsDataOutput(PublicKey providerKey) throws IOException {

        //populate rewards output file here based on raw client data
        File outputFile = new File("rewardsOutput.avro");
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(rewardsOutputSchema);

        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(rewardsOutputSchema, outputFile);

        GenericRecord rewardRecord = new GenericData.Record(rewardsOutputSchema);

        int generalMinLimit = 1;
        int generalMaxLimit = 10;
        int finalRewardMinLimit = 1;
        int finalRewardMaxLimit = 100;

        switch (envelopeSchema.getName()) {
            case TEST_SCHEMA_1:
                rewardRecord.put("amountProvided", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("completeness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("uniqueness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("updateFrequency", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("qualityScore", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("rewards", getRandomNumber(finalRewardMinLimit, finalRewardMaxLimit, 1));
                rewardRecord.put("dataType", TEST_SCHEMA_1);
                break;
            case TEST_SCHEMA_2:
                rewardRecord.put("amountProvided", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("completeness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("uniqueness", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("updateFrequency", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("qualityScore", getRandomNumber(generalMinLimit, generalMaxLimit, 10));
                rewardRecord.put("rewards", getRandomNumber(finalRewardMinLimit, finalRewardMaxLimit, 1));
                rewardRecord.put("dataType", TEST_SCHEMA_2);
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

    private float getRandomNumber(int minLimit, int maxLimit, int decimalPlace) {
        return (minLimit + random.nextInt() * (minLimit - maxLimit)) / decimalPlace;
    }

}
