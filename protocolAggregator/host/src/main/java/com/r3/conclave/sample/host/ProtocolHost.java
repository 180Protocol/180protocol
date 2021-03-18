package com.r3.conclave.sample.host;

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
        // Report whether the platform supports hardware enclaves.
        //
        // This method will always check the hardware state regardless of whether running in Simulation,
        // Debug or Release mode. If the platform supports hardware enclaves then no exception is thrown.
        // If the platform does not support enclaves or requires enabling, an exception is thrown with the
        // details in the exception message.
        //
        // If the platform supports enabling of enclave support via software then passing true as a parameter
        // to this function will attempt to enable enclave support on the platform. Normally this process
        // will have to be run with root/admin privileges in order for it to be enabled successfully.
        try {
            EnclaveHost.checkPlatformSupportsEnclaves(true);
            System.out.println("This platform supports enclaves in simulation, debug and release mode.");
        } catch (MockOnlySupportedException e) {
            System.out.println("This platform only supports mock enclaves: " + e.getMessage());
            System.exit(1);
        } catch (EnclaveLoadException e) {
            System.out.println("This platform does not support hardware enclaves: " + e.getMessage());
        }

        // Enclaves get interesting when remote clients can talk to them.
        // Let's open a TCP socket and implement a trivial protocol that lets a remote client use it.
        // A real app would use SSL here to protect client/host communications, even though the only
        // data we're sending and receiving here is encrypted to the enclave: better safe than sorry.
        //C1
        int port1 = 9999;
        System.out.println("Listening on port " + port1 + ". Use the client app to send strings for reversal.");
        ServerSocket acceptor1 = new ServerSocket(port1);
        Socket connection1 = acceptor1.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output1 = new DataOutputStream(connection1.getOutputStream());

        //C2
        int port2 = 9998;
        System.out.println("Listening on port " + port2 + ". Use the client app to send strings for reversal.");
        ServerSocket acceptor2 = new ServerSocket(port2);
        Socket connection2 = acceptor2.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output2 = new DataOutputStream(connection2.getOutputStream());

        //C3
        int port3 = 9997;
        System.out.println("Listening on port " + port3 + ". Use the client app to send strings for reversal.");
        ServerSocket acceptor3 = new ServerSocket(port3);
        Socket connection3 = acceptor3.accept();

        // Just send the attestation straight to whoever connects. It's signed so that is MITM-safe.
        DataOutputStream output3 = new DataOutputStream(connection3.getOutputStream());


        // We start by loading the enclave using EnclaveHost, and passing the class name of the Enclave subclass
        // that we defined in our enclave module. This will start the sub-JVM and initialise the class in that, i.e.
        // the ReverseEnclave class is not instantiated in this JVM.
        //
        // We could also shut it down at the end by calling .close() but this isn't necessary if the host is about
        // to quit anyway. Don't load enclaves too often - it's like starting a sub-process and can be somewhat slow
        // because the CPU must hash the contents of the enclave binary.
        String className = "com.r3.conclave.sample.enclave.AggregationEnclave";
        EnclaveHost enclave = EnclaveHost.load(className);
        // Start it up with a callback that will deliver the response. But remember: in a real app that can handle
        // multiple clients, you shouldn't start one enclave per client. That'd be wasteful and won't fit in available
        // encrypted memory. A real app should use the routingHint parameter to select the right connection back
        // to the client, here.

        //System.out.println(enclave.getCapabilitiesDiagnostics());
        ArrayList<Long> acknowledgedMailIds = new ArrayList<>();
        int totalAggregationClients = 2;
        enclave.start(new AttestationParameters.DCAP(), (commands) -> {
            for (MailCommand command : commands) {
                if (command instanceof MailCommand.PostMail) {
                    try {
                        System.out.println("Post Mail Command");
                        //this can be modified to be sent to the actor with the routing hint
                        String routingHint = ((MailCommand.PostMail) command).getRoutingHint();
                        //check routing hint and retrieve recipient
                        System.out.println("Routing Hint: " + routingHint);
                        if(routingHint.equals("consumer")){
                            System.out.println("Sending Reply Mail to Consumer Client");
                            sendArray(output1, ((MailCommand.PostMail) command).getEncryptedBytes());
                        }
                        else if(routingHint.equals("provenance")){
                            System.out.println("Sending Reply Mail to Provenance Client");
                            sendArray(output2, ((MailCommand.PostMail) command).getEncryptedBytes());
                            sendArray(output3, ((MailCommand.PostMail) command).getEncryptedBytes());
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
        // Now let's send a local message from host to enclave, asking it to reverse a string.
        System.out.println();
        final Charset utf8 = StandardCharsets.UTF_8;
        System.out.println("Reversing Hello World!: " + new String(enclave.callEnclave("Hello World!".getBytes(utf8)), utf8));
        System.out.println();

        sendArray(output1, attestationBytes);
        sendArray(output2, attestationBytes);
        sendArray(output3, attestationBytes);

        // Now read some mail from the client.
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
        enclave.deliverMail(1, mailBytes1, "self");
        System.out.println("Sending Mail 2 to Enclave");
        enclave.deliverMail(2, mailBytes2, "consumer");

        //once all clients are acknowledged ask enclave for provenance mail
        System.out.println("Acknowledged mails: " + acknowledgedMailIds.toString());
        if(acknowledgedMailIds.size()  == totalAggregationClients - 1){
            System.out.println("Sending Provenance Mail to Enclave");
            enclave.deliverMail(3, mailBytes3, "provenance");
        }
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

    public static String callEnclave(EnclaveHost enclave, String input) {
        // TODO: Fill this out.
        return new String(enclave.callEnclave(input.getBytes()));
    }
}
