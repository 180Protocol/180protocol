package com.protocol180.aggregator.enclave;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.host.MailCommand;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.PostOffice;
import com.r3.conclave.testing.MockHost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.util.concurrent.atomic.AtomicReference;

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

        System.out.println("Aggregate Schema in Enclave: " + aggregationEnclave.aggregateSchema);

        assertNotNull(aggregationEnclave.aggregateSchema);
        assertEquals(5, aggregationEnclave.aggregateSchema.getFields().size());
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
        AtomicReference<byte []> encryptedReply = new AtomicReference<>();
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Post Mail Command");
                        routingHintReply.set(((MailCommand.PostMail) command).getRoutingHint());
                        System.out.println("Consumer Routing Hint: " + routingHintReply.toString());
                        if(routingHintReply.toString().equals("consumer")){
                            System.out.println("Sending Reply Mail to Consumer Client");
                            encryptedReply.set(((MailCommand.PostMail) command).getEncryptedBytes());
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

        System.out.println("Mails to Process: " + aggregationEnclave.clientToEncryptedDataMap);

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(3, aggregationEnclave.clientToEncryptedDataMap.size());
        assertEquals(routingHintReply.toString(), "consumer");

        System.out.println("Post office Map: " + MockClientUtil.postOfficeMap);

        PostOffice postOffice3 = MockClientUtil.postOfficeMap.get(
                new PostOfficeMapKey(privateKeyAndEncryptedBytes3.getPrivateKey(), mockHost.getEnclaveInstanceInfo().getDataSigningKey(), MockClientUtil.topic)
        );

        EnclaveMail reply = postOffice3.decryptMail(encryptedReply.get());
        System.out.println("Aggregated data from the Enclave : '" + new String(reply.getBodyAsBytes()) + "'");
    }


    /*


    @Test
    void testProvenanceMail() throws EnclaveLoadException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        AtomicReference<String> routingHintReply = new AtomicReference<>("");
        AtomicReference<byte[]> provenanceBytes = new AtomicReference<>();
        mockHost.start(null, (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Post Mail Command");
                        routingHintReply.set(((MailCommand.PostMail) command).getRoutingHint());
                        System.out.println("Consumer Routing Hint: " + routingHintReply.toString());
                        if(routingHintReply.toString().equals("consumer")){
                            System.out.println("Sending Reply Mail to Consumer Client");
                        }
                        else if(routingHintReply.toString().equals("provenance")){
                            System.out.println("Sending Reply Mail to Provenance Client");
                            System.out.println("Provenance Mail Bytes" + ((MailCommand.PostMail) command).getEncryptedBytes());
                            provenanceBytes.set(((MailCommand.PostMail) command).getEncryptedBytes());
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

        byte[] mailBytes1 = createEncryptedClientMail(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail 1: " + mailBytes1);
        mockHost.deliverMail(1, mailBytes1, "self");

        byte[] mailBytes2 = createEncryptedClientMail(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail 2: " + mailBytes2);
        mockHost.deliverMail(2, mailBytes2, "consumer");
        assertEquals(routingHintReply.toString(), "consumer");

        assertNotNull(aggregationEnclave.clientToEncryptedDataMap);
        assertEquals(2, aggregationEnclave.clientToEncryptedDataMap.size());

        byte[] mailBytes3 = createEncryptedClientMail(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail 3: " + mailBytes3);
        mockHost.deliverMail(3, mailBytes3, "provenance");
        assertEquals(routingHintReply.toString(), "provenance");

        assertNull(aggregationEnclave.clientToEncryptedDataMap);
    }


     */

    byte[] createEncryptedClientMail(EnclaveInstanceInfo attestation){
        String toReverse = "Complicated String";
        PrivateKey myKey = Curve25519PrivateKey.random();
        PostOffice postOffice = attestation.createPostOffice(myKey, "aggregate");
        return postOffice.encryptMail(toReverse.getBytes(StandardCharsets.UTF_8));
    }
}
