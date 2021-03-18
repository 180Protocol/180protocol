package com.r3.conclave.sample.enclave;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.host.MailCommand;
import com.r3.conclave.mail.Curve25519PrivateKey;
import com.r3.conclave.mail.PostOffice;
import com.r3.conclave.testing.MockHost;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the enclave fully in-memory in a mock environment.
 */
public class AggregationEnclaveTest {
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
    void testSelfMail() throws EnclaveLoadException {
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

        byte[] mailBytes1 = createEncryptedClientMail(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail: " + mailBytes1);
        mockHost.deliverMail(1, mailBytes1, "self");

        System.out.println("Mails to Process: " + aggregationEnclave.mailsToProcess);

        assertNotNull(aggregationEnclave.mailsToProcess);
        assertEquals(1, aggregationEnclave.mailsToProcess.size());
    }

    @Test
    void testConsumerMail() throws EnclaveLoadException {
        MockHost<AggregationEnclave> mockHost = MockHost.loadMock(AggregationEnclave.class);
        AtomicReference<String> routingHintReply = new AtomicReference<>("");
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

        System.out.println("Mails to Process: " + aggregationEnclave.mailsToProcess);
        System.out.println("Provenance Result: " + aggregationEnclave.mailsToProcess);

        assertNotNull(aggregationEnclave.mailsToProcess);
        assertEquals(2, aggregationEnclave.mailsToProcess.size());
        assertNotNull(aggregationEnclave.provenanceResult);
        assertEquals(2, aggregationEnclave.provenanceResult.size());
        assertEquals(routingHintReply.toString(), "consumer");
    }

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

        assertNotNull(aggregationEnclave.mailsToProcess);
        assertEquals(2, aggregationEnclave.mailsToProcess.size());
        assertNotNull(aggregationEnclave.provenanceResult);
        assertEquals(2, aggregationEnclave.provenanceResult.size());

        byte[] mailBytes3 = createEncryptedClientMail(mockHost.getEnclaveInstanceInfo());
        System.out.println("Encrypted client mail 3: " + mailBytes3);
        mockHost.deliverMail(3, mailBytes3, "provenance");
        assertEquals(routingHintReply.toString(), "provenance");

        assertNull(aggregationEnclave.mailsToProcess);
        assertNull(aggregationEnclave.provenanceResult);
    }

    byte[] createEncryptedClientMail(EnclaveInstanceInfo attestation){
        String toReverse = "Complicated String";
        PrivateKey myKey = Curve25519PrivateKey.random();
        PostOffice postOffice = attestation.createPostOffice(myKey, "reverse");
        return postOffice.encryptMail(toReverse.getBytes(StandardCharsets.UTF_8));
    }
}
