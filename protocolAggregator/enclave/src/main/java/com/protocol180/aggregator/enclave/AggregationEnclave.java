package com.protocol180.aggregator.enclave;

import com.protocol180.commons.MailType;
import com.r3.conclave.enclave.Enclave;
import com.r3.conclave.mail.EnclaveMail;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

;

/**
 * Simply reverses the bytes that are passed in.
 */
public abstract class AggregationEnclave extends Enclave {

    //Local store
    protected HashMap<PublicKey, byte[]> clientToEncryptedDataMap;
    protected HashMap<PublicKey, ArrayList<GenericRecord>> clientToRawDataMap;
    HashMap<String, String> clientIdentityStore;
    protected Schema aggregateInputSchema;
    protected Schema aggregateOutputSchema;
    protected Schema rewardsOutputSchema;
    Schema identitySchema;

    String clientTypeForCurrRequest = null;

    @Override
    final protected byte[] receiveFromUntrustedHost(byte[] schemaBytes) {
        // This is used for host->enclave calls so we don't have to think about authentication.

        Schema envelopSchema = new Schema.Parser().parse(new String(schemaBytes));
        aggregateInputSchema = envelopSchema.getField("aggregateInput").schema();
        aggregateOutputSchema = envelopSchema.getField("aggregateOutput").schema();
        rewardsOutputSchema = envelopSchema.getField("rewardsOutput").schema();
        identitySchema = envelopSchema.getField("identity").schema();

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

    protected abstract File createRewardsDataOutput(PublicKey providerKey) throws IOException;

    protected abstract File createAggregateDataOutput() throws IOException;

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
