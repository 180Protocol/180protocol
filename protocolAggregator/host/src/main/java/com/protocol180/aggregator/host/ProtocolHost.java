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
    private Socket clientSocket;

    public static void main(String[] args) throws EnclaveLoadException, IOException, InterruptedException {
        ProtocolHost host = new ProtocolHost();
        host.verifyPlatformSupport();
        host.initializeEnclave();
        host.startServer();
    }

    private void startServer() throws IOException {

        final EnclaveInstanceInfo attestation = enclaveHost.getEnclaveInstanceInfo();
        final byte[] attestationBytes = attestation.serialize();

        int port = 9999;
        ServerSocket serverSocket = new ServerSocket(port);

        int mailCounter = 0;

        while (mailCounter < 5) {
            System.out.println("Listening on port " + port + ". Use the client app to send Data.");

            assert serverSocket != null;
            clientSocket = serverSocket.accept();

            sendMessageToClient(clientSocket, attestationBytes);
            recieveMailFromClientAndDeliverToEnclave(clientSocket, mailCounter);
            mailCounter++;
        }
        clientSocket.close();
    }


    private void initializeEnclave() throws EnclaveLoadException {
        enclaveHost = EnclaveHost.load(ENCLAVE_CLASS_NAME);
        enclaveHost.start(
                new AttestationParameters.DCAP(), mailCommands -> {
                    for (MailCommand command : mailCommands) {
                        if (command instanceof MailCommand.PostMail) {
                            String routingHint = ((MailCommand.PostMail) command).getRoutingHint();
                            byte[] content = ((MailCommand.PostMail) command).getEncryptedBytes();
                            sendMessageToClient(clientSocket, content);
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

    private void recieveMailFromClientAndDeliverToEnclave(Socket clientSocket, int mailCounter) {
        try {
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            byte[] mailBytes = new byte[input.readInt()];
            input.readFully(mailBytes);
            System.out.println("Mail No is: " + mailCounter);
            if (mailCounter == 0)
                enclaveHost.deliverMail(mailCounter, mailBytes, "schema");
            else if (mailCounter == 1)
                enclaveHost.deliverMail(mailCounter, mailBytes, "identity");
            else
                enclaveHost.deliverMail(mailCounter, mailBytes, "client");
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
