package com.protocol180.aggregator.host;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.host.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class demonstrates how to load an enclave and exchange byte arrays with it.
 */
public class ProtocolHost {
    public static void main(String[] args) throws EnclaveLoadException, IOException {

        try {
            EnclaveHost.checkPlatformSupportsEnclaves(true);
            System.out.println("This platform supports enclaves in simulation, debug and release mode.");
        } catch (MockOnlySupportedException e) {
            System.out.println("This platform only supports mock enclaves: " + e.getMessage());
            System.exit(1);
        } catch (EnclaveLoadException e) {
            System.out.println("This platform does not support hardware enclaves: " + e.getMessage());
        }

        //C1 - schema provider
        int port1 = 9999;
        System.out.println("Listening on port " + port1 + ". Use the client app to send data for aggregation.");
        ServerSocket acceptor1 = new ServerSocket(port1);
        Socket connection1 = acceptor1.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output1 = new DataOutputStream(connection1.getOutputStream());

        //C2 - identities provider
        int port2 = 9998;
        System.out.println("Listening on port " + port2 + ". Use the client app to send data for aggregation.");
        ServerSocket acceptor2 = new ServerSocket(port2);
        Socket connection2 = acceptor2.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output2 = new DataOutputStream(connection2.getOutputStream());

        //C3 - client(provider, consumer, provenance)
        int port3 = 9997;
        System.out.println("Listening on port " + port3 + ". Use the client app to send data for aggregation.");
        ServerSocket acceptor3 = new ServerSocket(port3);
        Socket connection3 = acceptor3.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output3 = new DataOutputStream(connection3.getOutputStream());

        String className = "com.protocol180.aggregator.enclave.AggregationEnclave";
        EnclaveHost enclave = EnclaveHost.load(className);

        ArrayList<Long> acknowledgedMailIds = new ArrayList<>();
        AtomicReference<byte[]> mailToSend = new AtomicReference<>();
        enclave.start(new AttestationParameters.DCAP(), (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    System.out.println("Post Mail Command");
                    String routingHint = ((MailCommand.PostMail) command).getRoutingHint();
                    System.out.println("Routing Hint: " + routingHint);
                    if (routingHint.equals("client")) {
                        System.out.println("Sending Reply Mail to Consumer or Provenance");
                        mailToSend.set(((MailCommand.PostMail) command).getEncryptedBytes());
                    }
                } else if (command instanceof MailCommand.AcknowledgeMail) {
                    System.out.println("Ack Mail Command");
                    //acknowledge mail and store locally? wait for all clients to process and aggregate
                    acknowledgedMailIds.add(((MailCommand.AcknowledgeMail) command).getMailID());
                }
            }
        });

        final EnclaveInstanceInfo attestation = enclave.getEnclaveInstanceInfo();
        final byte[] attestationBytes = attestation.serialize();
        // The attestation data must be provided to the client of the enclave, via whatever mechanism you like.

        // It has a useful toString method.
        System.out.println(EnclaveInstanceInfo.deserialize(attestationBytes));
        System.out.println();

        // C1 - schema , C2 - identities , C3 - client(Provider, Consumer, Provenance)
        DataInputStream input1 = new DataInputStream(connection1.getInputStream());
        byte[] mailBytes1 = new byte[input1.readInt()];
        input1.readFully(mailBytes1);

        DataInputStream input2 = new DataInputStream(connection2.getInputStream());
        byte[] mailBytes2 = new byte[input2.readInt()];
        input2.readFully(mailBytes2);

        DataInputStream input3 = new DataInputStream(connection3.getInputStream());
        byte[] mailBytes3 = new byte[input3.readInt()];
        input3.readFully(mailBytes3);

        // Deliver it. The enclave will give us the encrypted reply in the callback we provided above, which
        // will then send the reply to the client.

        //host is responsible for managing the state of the aggregation by delivering mail with the right contents and routing hint
        System.out.println("Sending Mail 1 to Enclave");
        enclave.deliverMail(0, mailBytes1, "schema");

        System.out.println("Sending Mail 2 to Enclave");
        enclave.deliverMail(1, mailBytes2, "identity");

        System.out.println("Sending Mail 3 to Enclave");
        enclave.deliverMail(2, mailBytes3, "client");
        byte[] toSend = mailToSend.getAndSet(null);
        sendArray(output3, toSend);


        // Closing the output stream closes the connection. Different clients will block each other but this
        // is just a hello world sample.
        System.out.println("Closing streams");
        output1.close();
        output2.close();
        output3.close();

    }

    private static void sendArray(DataOutputStream stream, byte[] bytes) throws IOException {
        stream.writeInt(bytes.length);
        stream.write(bytes);
        stream.flush();
    }
}
