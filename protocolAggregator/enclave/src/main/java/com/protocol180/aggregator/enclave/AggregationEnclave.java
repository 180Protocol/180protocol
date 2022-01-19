package com.protocol180.aggregator.enclave;

import com.protocol180.aggregator.clientEnclave.ExampleAggregationEnclave;
import com.protocol180.commons.MailType;
import com.r3.conclave.enclave.Enclave;
import com.r3.conclave.mail.EnclaveMail;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.util.*;

;

/**
 * AggregationEnclave is an interface that is the basis for based 180Protocol Coalition applications  to compute data aggregations and rewards
 * Aggregation Enclave must be extended by application developers to suit their coalition specific data aggregation needs. AggregationEnclave works with
 * 180Protocol Broker Flows - which can be Consumer or Provider led. AggregationEnclave based Enclaves are spun up by a coalition host acting on behalf
 * of the 180Protocol coalition network. When a consumer or provider led flow is kicked off, a supported coalition data type is requested to be aggregated
 * by either a consumer or provider.
 * AggregationEnclave relies on Apache Avro to serialize data in and out of the Enclave. Aggregation Enclave is data structure agnostic and provides compute
 * workflow where any data can be aggregated and rewarded for. For this the host provides to the enclave an Apache Avro schema file called the 'envelopeSchema',
 * containing the necessary aggregate input, output and rewards sub-schemas.The schemas act as an instruction set for
 * the enclave to perform the data output computation and rewards calculation for the consumers and providers respectively.
 */
public abstract class AggregationEnclave extends Enclave {

    //Enclave local store
    protected HashMap<PublicKey, byte[]> clientToEncryptedDataMap;
    protected HashMap<PublicKey, ArrayList<GenericRecord>> clientToRawDataMap;
    protected Schema envelopeSchema;
    protected Schema aggregateInputSchema;
    protected Schema aggregateOutputSchema;
    protected Schema rewardsOutputSchema;
    Schema identitySchema;

    String clientTypeForCurrRequest = null;

    @Override
    final protected byte[] receiveFromUntrustedHost(byte[] schemaBytes) {
        // This is used for host->enclave calls so we don't have to think about authentication.

        envelopeSchema = new Schema.Parser().parse(new String(schemaBytes));
        aggregateInputSchema = envelopeSchema.getField("aggregateInput").schema();
        aggregateOutputSchema = envelopeSchema.getField("aggregateOutput").schema();
        rewardsOutputSchema = envelopeSchema.getField("rewardsOutput").schema();
        identitySchema = envelopeSchema.getField("identity").schema();

        return "Schema Initialized".getBytes();
    }


    final protected void convertEncryptedClientDataToRawData() {
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(aggregateInputSchema);
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

    /**
     * createRewardsDataOutput - calculates rewards for the data provider(s). Reward calculation must be provider specific.
     * In the future a Rewards Engine will be inserted for developers to utilize for reward factor calculations.
     * This method must be overridden by the coalition application developer to contain a reward calculation algorithm that
     * accepts a public key for the data provider for which the rewards are being calculated.
     * The output must be an avro serialized file containing Avro GenericRecord.
     * The GenericRecord must follow the sub-schema defined as 'rewardOutput' under the 'envelopeSchema'
     * @see org.apache.avro.generic.GenericRecord
     **/
    protected abstract File createRewardsDataOutput(PublicKey providerKey) throws IOException, ExampleAggregationEnclave.UnsupportedDataTypeException;

    /**
     * createAggregateDataOutput - calculates a data output for the data consumer(s). Currently, the same data output is
     * computed for all data consumers. This method must be overridden by the coalition application developer to contain a
     * data transformation algorithm that transforms data from the specified 'aggregateInput' sub-schema to the 'aggregateOutput'
     * sub-schema. The output must be an avro serialized file containing Avro GenericRecord.
     * The GenericRecord must follow the sub-schema defined as 'aggregateOutput' under the 'envelopeSchema'
     * @see org.apache.avro.generic.GenericRecord
     **/
    protected abstract File createAggregateDataOutput() throws IOException, ExampleAggregationEnclave.UnsupportedDataTypeException;

    void initializeLocalStore() {
        clientToEncryptedDataMap = new HashMap();
        clientToRawDataMap = new HashMap();
    }

    void clearLocalStore() {
        clientToEncryptedDataMap = null;
        clientToRawDataMap = null;
    }

    private void putUnencryptedMailToClient(PublicKey sender, byte[] mailBytes) {
        clientToEncryptedDataMap.put(sender, mailBytes);
    }

    /**
     * receiveMail function defines the entry point for requests coming into the enclave from the host. The host conveys encrypted
     * mail bytes from clients along ith a routing hint. See R3 Conclave documentation for more details around
     * @see com.r3.conclave.mail.EnclaveMail API
     * The receiveMail function executes a data aggregation and reward compute workflow. To determine the right step in the workflow
     * the Enclave determines the MailType from the encrypted mail message from the host. Based on this the enclave
     * executes the necessary workflow action -
     * 1. Storing data input from all providers
     * 2. Computing Data Outputs for all consumers
     * 3. Computing Rewards for all providers
     * @see com.protocol180.commons.MailType
     **/
    @Override
    protected final void receiveMail(EnclaveMail mail, String routingHint) {
        final byte[] unencryptedMail = mail.getBodyAsBytes();


        MailType mailType = getMailType(new String(unencryptedMail));
        System.out.println("Type of mail for current request is:" + mailType);

        String senderEncodedPublicKey = Base64.getEncoder().encodeToString(mail.getAuthenticatedSender().getEncoded());
        System.out.println("Clients randomly generated public key is: " + senderEncodedPublicKey);

        try {
            if (mailType.equals(MailType.TYPE_PROVIDER)) {
                clientTypeForCurrRequest = MailType.TYPE_PROVIDER.type;
                //store mail contents for aggregation
                System.out.println("Ack Mail for provider");
                if (clientToEncryptedDataMap == null && clientToRawDataMap == null) {
                    initializeLocalStore();
                }
                putUnencryptedMailToClient(mail.getAuthenticatedSender(), unencryptedMail);
                System.out.println(clientToEncryptedDataMap.size());

                final byte[] responseBytes = postOffice(mail).encryptMail(String.valueOf(clientToEncryptedDataMap.size()).getBytes());
                postMail(responseBytes, routingHint);

            } else if (mailType.equals(MailType.TYPE_CONSUMER)) {
                clientTypeForCurrRequest = MailType.TYPE_CONSUMER.type;
                //send aggregation output to consumer
                System.out.println("Aggregate Data request Mail from consumer");
                //create aggregate output
                File aggregateOutput = createAggregateDataOutput();
                System.out.println(new String(Files.readAllBytes(aggregateOutput.toPath())));
                final byte[] responseBytes = postOffice(mail).encryptMail(Files.readAllBytes(aggregateOutput.toPath()));
                postMail(responseBytes, routingHint);

            } else if (mailType.equals(MailType.TYPE_REWARDS)) {
                clientTypeForCurrRequest = MailType.TYPE_REWARDS.type;
                //send rewards result to party aka provider
                System.out.println("Rewards Mail");
                //create rewards output
                File rewardsOutput = createRewardsDataOutput(mail.getAuthenticatedSender());
                final byte[] responseBytes = postOffice(mail).encryptMail(Files.readAllBytes(rewardsOutput.toPath()));
                postMail(responseBytes, routingHint);

            } else {
                clientTypeForCurrRequest = null;
                System.out.println("Unauthenticated client request");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MailType getMailType(String unencryptedMail) {
        if (unencryptedMail.contains("AggregateInput"))
            return MailType.TYPE_PROVIDER;
        else if (unencryptedMail.contains("AggregateOutput"))
            return MailType.TYPE_CONSUMER;
        else if (unencryptedMail.contains("Rewards"))
            return MailType.TYPE_REWARDS;
        return null;
    }


}
