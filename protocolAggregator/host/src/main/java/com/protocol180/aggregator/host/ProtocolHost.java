package com.protocol180.aggregator.host;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.host.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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

        //C2 - provider 1 - data consumer 1
        int port2 = 9998;
        System.out.println("Listening on port " + port2 + ". Use the client app to send data for aggregation.");
        ServerSocket acceptor2 = new ServerSocket(port2);
        Socket connection2 = acceptor2.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output2 = new DataOutputStream(connection2.getOutputStream());

        //C3 - provider 2 - data consumer 2
        int port3 = 9997;
        System.out.println("Listening on port " + port3 + ". Use the client app to send data for aggregation.");
        ServerSocket acceptor3 = new ServerSocket(port3);
        Socket connection3 = acceptor3.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output3 = new DataOutputStream(connection3.getOutputStream());

        //C4 - consumer - provenance consumer - provenance schema provider
        int port4 = 9996;
        System.out.println("Listening on port " + port4 + ". Use the client app to send data for aggregation.");
        ServerSocket acceptor4 = new ServerSocket(port4);
        Socket connection4 = acceptor4.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output4 = new DataOutputStream(connection4.getOutputStream());


        String className = "com.protocol180.aggregator.enclave.AggregationEnclave";
        EnclaveHost enclave = EnclaveHost.load(className);

        ArrayList<Long> acknowledgedMailIds = new ArrayList<>();
        int totalAggregationClients = 2;
        enclave.start(new AttestationParameters.DCAP(), (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Post Mail Command");
                        String routingHint = ((MailCommand.PostMail) command).getRoutingHint();
                        System.out.println("Routing Hint: " + routingHint);
                        if(routingHint.equals("consumer")){
                            System.out.println("Sending Reply Mail to Data Consumer Client");
                            sendArray(output2, ((MailCommand.PostMail) command).getEncryptedBytes());
                            sendArray(output3, ((MailCommand.PostMail) command).getEncryptedBytes());
                        }
                        else if(routingHint.equals("provenance")){
                            System.out.println("Sending Reply Mail to Provenance Consumer Client");
                            sendArray(output4, ((MailCommand.PostMail) command).getEncryptedBytes());
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to send reply to client.");
                        e.printStackTrace();
                    }
                }
                else if(command instanceof MailCommand.AcknowledgeMail){
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

        sendArray(output1, attestationBytes);
        sendArray(output2, attestationBytes);
        sendArray(output3, attestationBytes);
        sendArray(output4, attestationBytes);

        // C1 - schema , C2 - provider 1, C3 - provider 2, C4 - provenance consumer
        DataInputStream input1 = new DataInputStream(connection1.getInputStream());
        byte[] mailBytes1 = new byte[input1.readInt()];
        input1.readFully(mailBytes1);

        DataInputStream input2 = new DataInputStream(connection2.getInputStream());
        byte[] mailBytes2 = new byte[input2.readInt()];
        input2.readFully(mailBytes2);

        DataInputStream input3 = new DataInputStream(connection3.getInputStream());
        byte[] mailBytes3 = new byte[input3.readInt()];
        input3.readFully(mailBytes3);

        DataInputStream input4 = new DataInputStream(connection4.getInputStream());
        byte[] mailBytes4 = new byte[input4.readInt()];
        input4.readFully(mailBytes4);

        // Deliver it. The enclave will give us the encrypted reply in the callback we provided above, which
        // will then send the reply to the client.

        //host is responsible for managing the state of the aggregation by delivering mail with the right contents and routing hint
        System.out.println("Sending Mail 1 to Enclave");
        enclave.deliverMail(0, mailBytes1, "schema");
        System.out.println("Sending Mail 2 to Enclave");
        enclave.deliverMail(1, mailBytes2, "self");
        System.out.println("Sending Mail 3 to Enclave");
        enclave.deliverMail(2, mailBytes3, "consumer");

        //once all clients are acknowledged ask enclave for provenance mail
        System.out.println("Acknowledged mails: " + acknowledgedMailIds.toString());
        if(acknowledgedMailIds.size()  == totalAggregationClients){
            System.out.println("Sending Provenance Mail to Enclave");
            enclave.deliverMail(3, mailBytes4, "provenance");
        }
        // Closing the output stream closes the connection. Different clients will block each other but this
        // is just a hello world sample.
        System.out.println("Closing streams");
        output1.close();
        output2.close();
        output3.close();
        output4.close();

    }

    private static void sendArray(DataOutputStream stream, byte[] bytes) throws IOException {
        stream.writeInt(bytes.length);
        stream.write(bytes);
        stream.flush();
    }
}
