package com.protocol180.aggregator.enclave;

import com.protocol180.aggregator.commons.PostOfficeMapKey;
import com.protocol180.aggregator.commons.PrivateKeyAndEncryptedBytes;
import com.r3.conclave.host.EnclaveHost;
import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.host.MailCommand;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.PostOffice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.protocol180.aggregator.commons.MockClientUtil;
import com.protocol180.aggregator.commons.Utility;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class AggregationEnclaveTest {

    MockClientUtil mockClientUtil = new MockClientUtil();

    //Generating random clients for test
    Map<Curve25519PrivateKey, String> randomClients = new HashMap<>();
    Curve25519PrivateKey provider1 = Curve25519PrivateKey.random();
    Curve25519PrivateKey provider2 = Curve25519PrivateKey.random();
    Curve25519PrivateKey consumer = Curve25519PrivateKey.random();
    Curve25519PrivateKey provenance = Curve25519PrivateKey.random();

    @BeforeEach
    public void initializeMockClient() {
        mockClientUtil = new MockClientUtil();
        randomClients.put(provider1, Utility.CLIENT_PROVIDER);
        randomClients.put(provider2, Utility.CLIENT_PROVIDER);
        randomClients.put(consumer, Utility.CLIENT_CONSUMER);
        randomClients.put(provenance, Utility.CLIENT_PROVENANCE);
    }

    @Test
    void reverseNumber() throws EnclaveLoadException {
        EnclaveHost mockHost = EnclaveHost.load("com.protocol180.aggregator.enclave.AggregationEnclave");
        mockHost.start(null, null);
        AggregationEnclave aggregationEnclave = (AggregationEnclave) mockHost.getMockEnclave();

        assertNull(aggregationEnclave.previousResult);

        byte[] response = mockHost.callEnclave("1234".getBytes());
        assertNotNull(response);
        assertEquals("4321", new String(response));

        assertEquals("4321", new String(aggregationEnclave.previousResult));
    }

    @Test
    void testSchemaMail() throws EnclaveLoadException, IOException {
        EnclaveHost mockHost = EnclaveHost.load("com.protocol180.aggregator.enclave.AggregationEnclave");
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = (AggregationEnclave) mockHost.getMockEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        System.out.println("Aggregate Input Schema in Enclave: " + aggregationEnclave.aggregateInputSchema);
        System.out.println("Aggregate output Schema in Enclave: " + aggregationEnclave.aggregateInputSchema);
        assertNotNull(aggregationEnclave.aggregateInputSchema);
        assertNotNull(aggregationEnclave.aggregateOutputSchema);
        assertNotNull(aggregationEnclave.provenanceOutputSchema);
        assertEquals(5, aggregationEnclave.aggregateInputSchema.getFields().size());
        assertEquals(5, aggregationEnclave.aggregateOutputSchema.getFields().size());
        assertEquals(2, aggregationEnclave.provenanceOutputSchema.getFields().size());
    }

    @Test
    void testProviderMail() throws EnclaveLoadException, IOException {
        EnclaveHost mockHost = EnclaveHost.load("com.protocol180.aggregator.enclave.AggregationEnclave");
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = (AggregationEnclave) mockHost.getMockEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        byte[] identitiesData = mockClientUtil.createEncryptedClientMailForIdentities(mockHost.getEnclaveInstanceInfo(), provider1, randomClients);
        System.out.println("Encrypted client mail with identities data: " + identitiesData);
        mockHost.deliverMail(1, identitiesData, "identity");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes = mockClientUtil.createEncryptedProviderMailForAggregationData(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with aggregation data for provider: " + privateKeyAndEncryptedBytes.getEncryptedBytes());
        mockHost.deliverMail(2, privateKeyAndEncryptedBytes.getEncryptedBytes(), "client");

        System.out.println("Mails to Process: " + aggregationEnclave.clientToEncryptedDataMap);

        assertNotNull(aggregationEnclave.localClientStore);
        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(4, aggregationEnclave.localClientStore.size());
        assertEquals(1, aggregationEnclave.clientToEncryptedDataMap.size());
    }

    @Test
    void testConsumerMail() throws EnclaveLoadException, IOException {
        EnclaveHost mockHost = EnclaveHost.load("com.protocol180.aggregator.enclave.AggregationEnclave");
        AtomicReference<byte[]> postReply = new AtomicReference<>();
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Sending Reply Mail to Consumer Client");
                        postReply.set(((MailCommand.PostMail) command).getEncryptedBytes());
                    } catch (Exception e) {
                        System.err.println("Failed to send reply ");
                        e.printStackTrace();
                    }
                } else if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = (AggregationEnclave) mockHost.getMockEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        byte[] identitiesData = mockClientUtil.createEncryptedClientMailForIdentities(mockHost.getEnclaveInstanceInfo(), provider1, randomClients);
        System.out.println("Encrypted client mail with identities data: " + identitiesData);
        mockHost.deliverMail(1, identitiesData, "identity");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes1 = mockClientUtil.createEncryptedProviderMailForAggregationData(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with aggregation data for provider: " + privateKeyAndEncryptedBytes1.getEncryptedBytes());
        mockHost.deliverMail(2, privateKeyAndEncryptedBytes1.getEncryptedBytes(), "client");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes2 = mockClientUtil.createEncryptedProviderMailForAggregationData(mockHost.getEnclaveInstanceInfo(), provider2);
        System.out.println("Encrypted client mail with aggregation data: for provider" + privateKeyAndEncryptedBytes2.getEncryptedBytes());
        mockHost.deliverMail(3, privateKeyAndEncryptedBytes2.getEncryptedBytes(), "client");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes3 = mockClientUtil.createEncryptedConsumerMailForAggregationData(mockHost.getEnclaveInstanceInfo(), consumer);
        System.out.println("Encrypted client mail for consumer: " + privateKeyAndEncryptedBytes3.getEncryptedBytes());
        mockHost.deliverMail(4, privateKeyAndEncryptedBytes3.getEncryptedBytes(), "client");

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(2, aggregationEnclave.clientToEncryptedDataMap.size());
        assertNotNull(aggregationEnclave.clientToRawDataMap);
        assertEquals(2, aggregationEnclave.clientToRawDataMap.size());
        assertEquals(aggregationEnclave.clientTypeForCurrRequest, Utility.CLIENT_CONSUMER);

        PostOffice postOffice3 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes3.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getEncryptionKey(), MockClientUtil.topic)
        );

        EnclaveMail reply = postOffice3.decryptMail(postReply.get());
        mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), "aggregate");
        //System.out.println("Aggregated data from the Enclave : '" + mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes()).toString() + "'");


    }

    @Test
    void testProvenanceMail() throws EnclaveLoadException, IOException {

        EnclaveHost mockHost = EnclaveHost.load("com.protocol180.aggregator.enclave.AggregationEnclave");
        AtomicReference<byte[]> postReply = new AtomicReference<>();
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Sending Reply Mail to Consumer Client");
                        postReply.set(((MailCommand.PostMail) command).getEncryptedBytes());
                    } catch (Exception e) {
                        System.err.println("Failed to send reply ");
                        e.printStackTrace();
                    }
                } else if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = (AggregationEnclave) mockHost.getMockEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        byte[] identitiesData = mockClientUtil.createEncryptedClientMailForIdentities(mockHost.getEnclaveInstanceInfo(), provider1, randomClients);
        System.out.println("Encrypted client mail with identities data: " + identitiesData);
        mockHost.deliverMail(1, identitiesData, "identity");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes1 = mockClientUtil.createEncryptedProviderMailForAggregationData(mockHost.getEnclaveInstanceInfo(), provider1);
        System.out.println("Encrypted client mail with aggregation data for provider: " + privateKeyAndEncryptedBytes1.getEncryptedBytes());
        mockHost.deliverMail(2, privateKeyAndEncryptedBytes1.getEncryptedBytes(), "client");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes2 = mockClientUtil.createEncryptedProviderMailForAggregationData(mockHost.getEnclaveInstanceInfo(), provider2);
        System.out.println("Encrypted client mail with aggregation data: for provider" + privateKeyAndEncryptedBytes2.getEncryptedBytes());
        mockHost.deliverMail(3, privateKeyAndEncryptedBytes2.getEncryptedBytes(), "client");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes3 = mockClientUtil.createEncryptedConsumerMailForAggregationData(mockHost.getEnclaveInstanceInfo(), consumer);
        System.out.println("Encrypted client mail for consumer: " + privateKeyAndEncryptedBytes3.getEncryptedBytes());
        mockHost.deliverMail(4, privateKeyAndEncryptedBytes3.getEncryptedBytes(), "client");

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(2, aggregationEnclave.clientToEncryptedDataMap.size());
        assertNotNull(aggregationEnclave.clientToRawDataMap);
        assertEquals(2, aggregationEnclave.clientToRawDataMap.size());
        assertEquals(aggregationEnclave.clientTypeForCurrRequest, Utility.CLIENT_CONSUMER);

        PostOffice postOffice3 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes3.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getEncryptionKey(), MockClientUtil.topic)
        );

        EnclaveMail reply = postOffice3.decryptMail(postReply.get());
        mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), "aggregate");

        //provenance
        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes4 = mockClientUtil.createEncryptedClientMailForProvenanceData(mockHost.getEnclaveInstanceInfo(), provenance);
        System.out.println("Encrypted client mail for provenance data: " + privateKeyAndEncryptedBytes4.getEncryptedBytes());
        mockHost.deliverMail(5, privateKeyAndEncryptedBytes4.getEncryptedBytes(), "client");

        assertEquals(aggregationEnclave.clientTypeForCurrRequest, Utility.CLIENT_PROVENANCE);
        assertNull(aggregationEnclave.clientToEncryptedDataMap);

        PostOffice postOffice4 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes4.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getEncryptionKey(), MockClientUtil.topic)
        );

        reply = postOffice4.decryptMail(postReply.get());
        mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), "provenance");

    }

}
