package com.r3.conclave.sample.enclave;

import com.r3.conclave.enclave.Enclave;
import com.r3.conclave.mail.EnclaveMail;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.HashMap;

/**
 * Simply reverses the bytes that are passed in.
 */
public class AggregationEnclave extends Enclave {
    // We store the previous result to showcase that the enclave internals can be examined in a mock test.
    byte[] previousResult;

    //not needed - used for demonstration of comms with host
    @Override
    protected byte[] receiveFromUntrustedHost(byte[] bytes) {
        // This is used for host->enclave calls so we don't have to think about authentication.
        final String input = new String(bytes);
        byte[] result = reverse(input).getBytes();
        previousResult = result;
        return result;
    }

    private static String reverse(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = input.length() - 1; i >= 0; i--)
            builder.append(input.charAt(i));
        return builder.toString();
    }

    protected String aggregate() {
        StringBuilder aggregatedString = new StringBuilder();
        System.out.println("Mails to Process" + mailsToProcess.toString());
        mailsToProcess.values().forEach( s -> aggregatedString.append(s).append(" "));
        mailsToProcess.forEach((publicKey, s) -> provenanceResult.put(publicKey, (double) (s.length()/aggregatedString.length())));
        return aggregatedString.toString();
    }

    protected void initializeLocalStore(){
        mailsToProcess = new HashMap();
        provenanceResult = new HashMap();
    }

    protected void clearLocalStore(){
        mailsToProcess = null;
        provenanceResult = null;
    }

    protected void putMailInMailToProcess(PublicKey sender, String stringToMap){
        mailsToProcess.put(sender, stringToMap);
    }

    @Override
    protected void receiveMail(long id, EnclaveMail mail, String routingHint) {
        // This is used when the host delivers a message from the client.
        // First, decode mail body as a String.
        final String stringToReverse = new String(mail.getBodyAsBytes());
        final PublicKey sender = mail.getAuthenticatedSender();
        if (sender == null)
            throw new IllegalArgumentException("Mail sent to this enclave must be authenticated so we can reply.");
        //enclave acknowledges mail or routes to host based on routingHint from host
        try {
            if (routingHint.equals("self")) {
                //store mail contents for aggregation
                System.out.println("Ack Mail");
                if(mailsToProcess == null && provenanceResult == null){
                    initializeLocalStore();
                }
                putMailInMailToProcess(sender, stringToReverse);
                acknowledgeMail(id);
            } else if (routingHint.equals("consumer")) {
                //send aggregation output to consumer
                System.out.println("Aggregate Mail");
                //retrieve all processedMails in this chain and aggregate
                putMailInMailToProcess(sender, stringToReverse);
                String aggregateOutput = aggregate();
                // Reverse it and re-encode to UTF-8 to send back.
                final byte[] reversedEncodedString = reverse(aggregateOutput).getBytes();
                // Check the client that sent the mail set things up so we can reply.
                // Get the post office object for responding back to this mail and use it to encrypt our response.
                final byte[] responseBytes = postOffice(mail).encryptMail(reversedEncodedString);
                postMail(responseBytes, routingHint);
            } else if (routingHint.equals("provenance")) {
                //send provenance result to required party
                // Convert Map to byte array
                System.out.println("Provenance Mail");
                /*ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteOut);
                out.writeObject(provenanceResult);
                byte[] byteOut = provenanceResult.toString().getBytes();
                */
                byte[] byteOut = SerializationUtils.serialize(provenanceResult);
                postMail(byteOut, routingHint);
                clearLocalStore();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    HashMap<PublicKey, Double> provenanceResult;
    HashMap<PublicKey, String> mailsToProcess;
}
