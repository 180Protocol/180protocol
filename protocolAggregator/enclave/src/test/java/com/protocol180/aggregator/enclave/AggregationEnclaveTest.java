package com.protocol180.aggregator.enclave;

import com.protocol180.aggregator.commons.PostOfficeMapKey;
import com.protocol180.aggregator.commons.PrivateKeyAndEncryptedBytes;
import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.host.MailCommand;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.PostOffice;
import com.r3.conclave.testing.MockHost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.protocol180.aggregator.commons.MockClientUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class AggregationEnclaveTest {

    MockClientUtil mockClientUtil = new MockClientUtil();

    @BeforeEach
    public void initializeMockClient(){
        mockClientUtil = new MockClientUtil();
    }

    @Test
    void reverseNumber() throws EnclaveLoadException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        mockHost.start(null, null);
        AggregationEnclave aggregationEnclave = mockHost.getEnclave();

        assertNull(aggregationEnclave.previousResult);

        byte[] response = mockHost.callEnclave("1234".getBytes());
        assertNotNull(response);
        assertEquals("4321", new String(response));

        assertEquals("4321", new String(aggregationEnclave.previousResult));
    }

    @Test
    void testSchemaMail() throws EnclaveLoadException, IOException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = mockHost.getEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo());
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
    void testAggregateDataMailToSelf() throws EnclaveLoadException, IOException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = mockHost.getEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes.getEncryptedBytes());
        mockHost.deliverMail(1, privateKeyAndEncryptedBytes.getEncryptedBytes(), "self");

        System.out.println("Mails to Process: " + aggregationEnclave.clientToEncryptedDataMap);

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(1, aggregationEnclave.clientToEncryptedDataMap.size());
    }

    @Test
    void testConsumerMail() throws EnclaveLoadException, IOException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        AtomicReference<String> routingHintReply = new AtomicReference<>("");
        AtomicReference<byte []> consumerReply = new AtomicReference<>();
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Post Mail Command");
                        routingHintReply.set(((MailCommand.PostMail) command).getRoutingHint());
                        System.out.println("Consumer Routing Hint: " + routingHintReply.toString());
                        if(routingHintReply.toString().equals("consumer")){
                            System.out.println("Sending Reply Mail to Consumer Client");
                            consumerReply.set(((MailCommand.PostMail) command).getEncryptedBytes());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to send reply to client.");
                        e.printStackTrace();
                    }
                }
                else if(command instanceof MailCommand.AcknowledgeMail){
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = mockHost.getEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes1 = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes1.getEncryptedBytes());
        mockHost.deliverMail(1, privateKeyAndEncryptedBytes1.getEncryptedBytes(), "self");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes2 = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes2.getEncryptedBytes());
        mockHost.deliverMail(2, privateKeyAndEncryptedBytes2.getEncryptedBytes(), "self");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes3 = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes3.getEncryptedBytes());
        mockHost.deliverMail(3, privateKeyAndEncryptedBytes3.getEncryptedBytes(), "consumer");

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(3, aggregationEnclave.clientToEncryptedDataMap.size());
        assertNotNull(aggregationEnclave.clientToRawDataMap);
        assertEquals(3, aggregationEnclave.clientToRawDataMap.size());
        assertEquals(routingHintReply.toString(), "consumer");

        PostOffice postOffice3 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes3.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getEncryptionKey(), MockClientUtil.topic)
        );

        EnclaveMail reply = postOffice3.decryptMail(consumerReply.get());
        mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), "aggregate");
        //System.out.println("Aggregated data from the Enclave : '" + mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes()).toString() + "'");
    }




    @Test
    void testProvenanceMail() throws EnclaveLoadException, IOException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        AtomicReference<String> routingHintReply = new AtomicReference<>("");
        AtomicReference<byte[]> provenanceReply = new AtomicReference<>();
        AtomicReference<byte []> consumerReply = new AtomicReference<>();
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Post Mail Command");
                        routingHintReply.set(((MailCommand.PostMail) command).getRoutingHint());
                        System.out.println("Consumer Routing Hint: " + routingHintReply.toString());
                        if(routingHintReply.toString().equals("consumer")){
                            System.out.println("Sending Reply Mail to Consumer Client");
                            consumerReply.set(((MailCommand.PostMail) command).getEncryptedBytes());
                        }
                        else if(routingHintReply.toString().equals("provenance")){
                            System.out.println("Sending Reply Mail to Provenance Client");
                            System.out.println("Provenance Mail Bytes" + ((MailCommand.PostMail) command).getEncryptedBytes());
                            provenanceReply.set(((MailCommand.PostMail) command).getEncryptedBytes());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to send reply to client.");
                        e.printStackTrace();
                    }
                }
                else if(command instanceof MailCommand.AcknowledgeMail){
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                }
            }
        });

        AggregationEnclave aggregationEnclave = mockHost.getEnclave();

        byte[] aggregationSchema = mockClientUtil.createEncryptedClientMailForAggregationSchema(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with schema: " + aggregationSchema);
        mockHost.deliverMail(0, aggregationSchema, "schema");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes1 = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes1.getEncryptedBytes());
        mockHost.deliverMail(1, privateKeyAndEncryptedBytes1.getEncryptedBytes(), "self");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes2 = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes2.getEncryptedBytes());
        mockHost.deliverMail(2, privateKeyAndEncryptedBytes2.getEncryptedBytes(), "self");

        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes3 = mockClientUtil.createEncryptedClientMailForAggregationData(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with aggregation data: " + privateKeyAndEncryptedBytes3.getEncryptedBytes());
        mockHost.deliverMail(3, privateKeyAndEncryptedBytes3.getEncryptedBytes(), "consumer");

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(3, aggregationEnclave.clientToEncryptedDataMap.size());
        assertNotNull(aggregationEnclave.clientToRawDataMap);
        assertEquals(3, aggregationEnclave.clientToRawDataMap.size());
        assertEquals(routingHintReply.toString(), "consumer");

        PostOffice postOffice3 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes3.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getEncryptionKey(), MockClientUtil.topic)
        );

        EnclaveMail reply = postOffice3.decryptMail(consumerReply.get());
        mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), "aggregate");

        //provenance
        PrivateKeyAndEncryptedBytes privateKeyAndEncryptedBytes4 = mockClientUtil.createEncryptedClientMailForProvenanceSchema(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail with provenance schema: " + privateKeyAndEncryptedBytes4.getEncryptedBytes());
        mockHost.deliverMail(4, privateKeyAndEncryptedBytes4.getEncryptedBytes(), "provenance");

        assertEquals(routingHintReply.toString(), "provenance");
        assertNull(aggregationEnclave.clientToEncryptedDataMap);

        PostOffice postOffice4 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes4.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getEncryptionKey(), MockClientUtil.topic)
        );

        reply = postOffice4.decryptMail(provenanceReply.get());
        mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(reply.getBodyAsBytes(), "provenance");

    }



}
