package com.protocol180.aggregator.host;

import com.r3.conclave.common.EnclaveInstanceInfo;
import com.r3.conclave.host.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class demonstrates how to load an enclave and exchange byte arrays with it.
 */
public class ProtocolHost {
    private static final String ENCLAVE_CLASS_NAME =
            "com.protocol180.aggregator.enclave.AggregationEnclave";
    private EnclaveHost enclaveHost;
    private Map<String, Socket> clientMap = new HashMap<>();

    public static void main(String[] args) throws EnclaveLoadException, IOException, InterruptedException {
        ProtocolHost host = new ProtocolHost();
        host.verifyPlatformSupport();
        host.initializeEnclave();
        host.startServer();


//        ArrayList<Long> acknowledgedMailIds = new ArrayList<>();
//        AtomicReference<byte[]> mailToSend = new AtomicReference<>();
//        enclave.start(new AttestationParameters.DCAP(), (commands) -> {
//            for (MailCommand command : commands) {
//                if (command instanceof MailCommand.PostMail) {
//                    System.out.println("Post Mail Command");
//                    String routingHint = ((MailCommand.PostMail) command).getRoutingHint();
//                    System.out.println("Routing Hint: " + routingHint);
//                    if (routingHint.equals("consumer") || routingHint.equals("provenance")) {
//                        System.out.println("Sending Reply Mail to Consumer or Provenance");
//                        mailToSend.set(((MailCommand.PostMail) command).getEncryptedBytes());
//                        System.out.println("hiiii "+mailToSend);
//                    }
//                } else if (command instanceof MailCommand.AcknowledgeMail) {
//                    System.out.println("Ack Mail Command");
//                    //acknowledge mail and store locally? wait for all clients to process and aggregate
//                    acknowledgedMailIds.add(((MailCommand.AcknowledgeMail) command).getMailID());
//                }
//            }
//        });

//        final EnclaveInstanceInfo attestation = enclave.getEnclaveInstanceInfo();
//        final byte[] attestationBytes = attestation.serialize();
//        // The attestation data must be provided to the client of the enclave, via whatever mechanism you like.
//
//        // It has a useful toString method.
//        System.out.println(EnclaveInstanceInfo.deserialize(attestationBytes));
//        System.out.println();
//
//        //C1 - schema provider
//        int port1 = 9999;
//        System.out.println("Listening on port " + port1 + ". Use the client app to send the schema.");
//        ServerSocket acceptor1 = new ServerSocket(port1);
//        Socket connection1 = acceptor1.accept();
//
//        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
//        DataOutputStream output1 = new DataOutputStream(connection1.getOutputStream());
//
//        sendArray(output1,attestationBytes);
//
//        DataInputStream input1 = new DataInputStream(connection1.getInputStream());
//        byte[] mailBytes1 = new byte[input1.readInt()];
//        input1.readFully(mailBytes1);
//
//        System.out.println("Sending Mail 1 to Enclave");
//        enclave.deliverMail(0, mailBytes1, "schema");
//
//
//        //C2 - identities provider
//        int port2 = 9998;
//        System.out.println("Listening on port " + port2 + ". Use the client app to send List of Identities");
//        ServerSocket acceptor2 = new ServerSocket(port2);
//        Socket connection2 = acceptor2.accept();
//
//        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
//        DataOutputStream output2 = new DataOutputStream(connection2.getOutputStream());
//
//        sendArray(output2,attestationBytes);
//
//        DataInputStream input2 = new DataInputStream(connection2.getInputStream());
//        byte[] mailBytes2 = new byte[input2.readInt()];
//        input2.readFully(mailBytes2);
//
//        System.out.println("Sending Mail 2 to Enclave");
//        enclave.deliverMail(1, mailBytes2, "identity");
//
//
//        //C3 - client(provider)
//        int port3 = 9997;
//        System.out.println("Listening on port " + port3 + ". Use the client app to provide data for aggregation.");
//        ServerSocket acceptor3 = new ServerSocket(port3);
//        Socket connection3 = acceptor3.accept();
//
//        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
//        DataOutputStream output3 = new DataOutputStream(connection3.getOutputStream());
//
//        sendArray(output3,attestationBytes);
//
//        DataInputStream input3 = new DataInputStream(connection3.getInputStream());
//        byte[] mailBytes3 = new byte[input3.readInt()];
//        input3.readFully(mailBytes3);
//        Thread.sleep(3000);
//        System.out.println("Sending Mail 3 to Enclave");
//        enclave.deliverMail(2, mailBytes3, "client");
//        Thread.sleep(3000);
//
//
//        //C4 - client(consumer)
//        int port4 = 9996;
//        System.out.println("Listening on port " + port4 + ". Use the client app to receive data from aggregation.");
//        ServerSocket acceptor4 = new ServerSocket(port4);
//        Socket connection4 = acceptor4.accept();
//
//        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
//        DataOutputStream output4 = new DataOutputStream(connection4.getOutputStream());
//
//        sendArray(output4,attestationBytes);
//        System.out.println("attestation bytes sent to client.");
//        DataInputStream input4 = new DataInputStream(connection4.getInputStream());
//        byte[] mailBytes4 = new byte[input4.readInt()];
//        input4.readFully(mailBytes4);
//
//        System.out.println("Sending Mail 4 to Enclave");
//        enclave.deliverMail(3, mailBytes4, "client");
//        Thread.sleep(3000);
//        byte[] toSendConsumer = mailToSend.getAndSet(null);
//        System.out.println(toSendConsumer);
//        Thread.sleep(3000);
//        sendArray(output4, toSendConsumer);
//
//
//        //C4 - client(provenance)
//        int port5 = 9995;
//        System.out.println("Listening on port " + port5 + ". Use the client app to receive data for provenance.");
//        ServerSocket acceptor5 = new ServerSocket(port5);
//        Socket connection5 = acceptor5.accept();
//
//        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
//        DataOutputStream output5 = new DataOutputStream(connection3.getOutputStream());
//
//        sendArray(output5,attestationBytes);
//
//        DataInputStream input5 = new DataInputStream(connection5.getInputStream());
//        byte[] mailBytes5 = new byte[input5.readInt()];
//        input5.readFully(mailBytes5);
//
//        System.out.println("Sending Mail 5 to Enclave");
//        enclave.deliverMail(4, mailBytes5, "client");
//        byte[] toSendProvenance = mailToSend.getAndSet(null);
//        sendArray(output5, toSendProvenance);
//
//
//
//
//
//
//
//        // C1 - schema , C2 - identities , C3 - client(Provider, Consumer, Provenance)
//
//
//
//
//
//        // Deliver it. The enclave will give us the encrypted reply in the callback we provided above, which
//        // will then send the reply to the client.
//
//        //host is responsible for managing the state of the aggregation by delivering mail with the right contents and routing hint
//
//
//
//
//
//
//        // Closing the output stream closes the connection. Different clients will block each other but this
//        // is just a hello world sample.
//        System.out.println("Closing streams");
//        output1.close();
//        output2.close();
//        output3.close();
//        output4.close();
//        output5.close();

    }

    private static void sendArray(DataOutputStream stream, byte[] bytes) throws IOException {
        stream.writeInt(bytes.length);
        stream.write(bytes);
        stream.flush();
    }

    private void startServer() throws IOException {
        final EnclaveInstanceInfo attestation = enclaveHost.getEnclaveInstanceInfo();
        final byte[] attestationBytes = attestation.serialize();

        int port = 9999;
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Listening on port " + port + ". Use the client app to send the schema.");
        Socket clientSocket = serverSocket.accept();
        DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
        sendMessageToClient(clientSocket, attestationBytes);
//        byte[] clientMail, serverMessage;
        int mailCounter = 1;
        while (mailCounter > 6) {
            recieveMailFromClientAndDeliverToEnclave(clientSocket, routingHint);
            clientMessage = inStream.readUTF();
            System.out.println("From Client-" + clientNo + ": Number is :" + clientMessage);
            squre = Integer.parseInt(clientMessage) * Integer.parseInt(clientMessage);
            serverMessage = "From Server to Client-" + clientNo + " Square of " + clientMessage + " is " + squre;
            outStream.writeUTF(serverMessage);
            outStream.flush();
        }
        inStream.close();
        outStream.close();
        clientSocket.close();
//        while (true) {
//            Socket clientSocket = null;
//            try {
//                assert serverSocket != null;
//                clientSocket = serverSocket.accept();
//
//                String routingHint = UUID.randomUUID().toString();
//                clientMap.put(routingHint, clientSocket);
//
//
//                sendMessageToClient(routingHint, attestationBytes);
//                recieveMailFromClientAndDeliverToEnclave(clientSocket, routingHint);
//            } catch (IOException e) {
//                System.out.println("I/O error: " + e);
//                throw e;
//            }
    }

}

    private void initializeEnclave() throws EnclaveLoadException {
        enclaveHost = EnclaveHost.load(ENCLAVE_CLASS_NAME);
        enclaveHost.start(
                new AttestationParameters.DCAP(), mailCommands -> {
                    for (MailCommand command : mailCommands) {
                        if (command instanceof MailCommand.PostMail) {
                            String routingHint = ((MailCommand.PostMail) command).getRoutingHint();
                            byte[] content = ((MailCommand.PostMail) command).getEncryptedBytes();
                            sendMessageToClient(routingHint, content);
                        } else if (command instanceof MailCommand.AcknowledgeMail) {
                            System.out.println("Ack Mail Command");
                        }
                    }
                }
        );
    }

    private void sendMessageToClient(Socket clientSocket, byte[] content) {
        try {
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.writeInt(content.length);
            outputStream.write(content);
            outputStream.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void recieveMailFromClientAndDeliverToEnclave(Socket clientSocket, String routingHint) {
        try {
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            byte[] mailBytes = new byte[input.readInt()];
            input.readFully(mailBytes);

            enclaveHost.deliverMail(1, mailBytes, routingHint);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void verifyPlatformSupport() {
        try {
            EnclaveHost.checkPlatformSupportsEnclaves(true);
            System.out.println("This platform supports enclaves in simulation, debug and release mode.");
        } catch (EnclaveLoadException e) {
            System.out.println("This platform does not support hardware enclaves: " + e.getMessage());
        }
    }
}
