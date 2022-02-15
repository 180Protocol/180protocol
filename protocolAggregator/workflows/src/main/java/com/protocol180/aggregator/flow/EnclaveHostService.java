package com.protocol180.aggregator.flow;

import com.r3.conclave.host.AttestationParameters;
import com.r3.conclave.host.EnclaveHost;
import com.r3.conclave.host.EnclaveLoadException;
import com.r3.conclave.host.MailCommand;
import com.r3.conclave.mail.MailDecryptionException;
import net.corda.core.flows.FlowExternalOperation;
import net.corda.core.flows.FlowLogic;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This corda service provides functionality to load an enclave into the node.
 * Call the {@link #loadEnclaveForAggregation(String, String)} method with the
 * flow Id & class name of the enclave, and then use it from flows.
 */
@CordaService
public class EnclaveHostService extends SingletonSerializeAsToken {

    // A map to store & track enclave loaded for each aggregation cycle launched from consumer node
    private Map<String, EnclaveHost> enclaveHostCollection;

    // A map of flow (state machine) IDs to futures that become complete when the enclave tries to deliver mail to them.
    private final Map<UUID, CompletableFuture<byte[]>> mailFutures = Collections.synchronizedMap(new HashMap<>());

    public EnclaveHostService(@NotNull AppServiceHub serviceHub) {
        enclaveHostCollection = new HashMap<>();
    }

    protected void loadEnclaveForAggregation(@NotNull String flowId, @NotNull String enclaveClassName) {
        try {
            EnclaveHost enclaveHost = EnclaveHost.load(enclaveClassName);
            // If you want to use pre-DCAP hardware via the older EPID protocol, you'll need to get the relevant API
            // keys from Intel and replace AttestationParameters.DCAP with AttestationParameters.EPID.
            enclaveHost.start(new AttestationParameters.DCAP(), null, null, (commands) -> {
                // The enclave is requesting that we deliver messages transactionally. In Corda there's no way to
                // do an all-or-nothing message delivery to multiple peers at once: for that you need a genuine
                // ledger transaction which is more complex and slower. So for now we'll just deliver messages
                // non-transactionally.
                for (MailCommand command : commands) {
                    if (command instanceof MailCommand.PostMail) {
                        MailCommand.PostMail post = (MailCommand.PostMail) command;
                        enclaveToFlow(post.getEncryptedBytes(), post.getRoutingHint());
                    }
                }
            });

            enclaveHostCollection.put(flowId,enclaveHost);
        } catch (EnclaveLoadException e) {
            throw new RuntimeException(e);   // Propagate and let the node abort startup, as this shouldn't happen.
        }
    }

    private void enclaveToFlow(byte[] encryptedBytes, @Nullable String routingHint) {
        // Called when the enclave is asking us to deliver an encrypted message to a peer.
        // The routing hint must be a specific flow ID. In this sample we don't let the enclave
        // trigger new flows with peers on the network, only respond to existing flows.
        //
        // TODO: To support enclaves sending messages when no flow is waiting, extend routingHint to contain both
        //       UUIDs and also party names.
        try {
            UUID flowId = UUID.fromString(routingHint.substring(routingHint.indexOf(":")+1));   // NPE here means enclave didn't provide a hint.

            mailFutures.get(flowId).complete(encryptedBytes);  // NPE here means flow is gone.
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new IllegalArgumentException("To send mail the routingHint parameter must be a valid pending flow ID, was " + routingHint, e);
        }
    }

    public void initializeAvroSchema(String flowId, byte[] schemaBytes) {
        enclaveHostCollection.get(flowId).callEnclave(schemaBytes);
    }

    public void removeEnclave(String flowId){
        enclaveHostCollection.remove(flowId);
    }


    /**
     * Returns serialised {@link com.r3.conclave.common.EnclaveInstanceInfo} object that represents the identity of the
     * loaded enclave.
     */
    public byte[] getAttestationBytes(String flowId){
        return enclaveHostCollection.get(flowId).getEnclaveInstanceInfo().serialize();
    }

    /**
     * Delivers a mail to the enclave and returns an operation that can be used to suspend a flow until the enclave
     * chooses to send a reply. This may not happen immediately. This is equivalent to calling
     * {@link #pickUpMail(FlowLogic)} on the flow, then {@link # deliverMail(byte[])}, then returning the result of
     * the receiveMail call.
     *
     * @param flow          The flow from which the mail is being received.
     * @param encryptedMail The contents of the mail.
     * @return An operation that can be passed to {@link FlowLogic#await(FlowExternalOperation)} to suspend the flow until
     * the enclave provides a mail to send.
     */
    public FlowExternalOperation<byte[]> deliverAndPickUpMail(FlowLogic<?> flow, byte[] encryptedMail) throws MailDecryptionException {
        // Prepare the object that the enclave will signal if it wants to send a response. It must be in the map
        // before we enter the enclave, as the enclave may immediately call back to request we deliver a response
        // and that will happen on the same call stack.
        FlowExternalOperation<byte[]> operation = pickUpMail(flow);
        enclaveHostCollection.get(flow.getRunId().getUuid().toString()).deliverMail(encryptedMail, flow.getRunId().getUuid().toString());
        // The operation might be completed already, but if not, the flow can sleep until the enclave decides to
        // reply (e.g. due to some other mail from some other flow) by calling await on this operation.
        return operation;
    }


    /**
     * Returns an operation that can be passed to {@link FlowLogic#await(FlowExternalOperation)} which will suspend
     * the flow until the enclave chooses to deliver a mail to it (e.g. because it received a mail from a different flow).
     *
     * @param flow The flow that the enclave may wish to deliver mail to.
     * @return The operation that can be used to suspend until the enclave is ready.
     */
    public FlowExternalOperation<byte[]> pickUpMail(FlowLogic<?> flow) {
        UUID flowID = flow.getRunId().getUuid();
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        synchronized (mailFutures) {
            assert !mailFutures.containsKey(flowID) : "Re-entrant attempt to wait for mail more than once.";
            // Create a future that the flow framework will wait on. The enclave doesn't have to immediately
            // given us mail to return to the peer!
            mailFutures.put(flowID, future);
        }
        return new ReceiveOperation(flowID);
    }

    // This inner class captures a reference to the service, which will survive checkpointing.
    private class ReceiveOperation implements FlowExternalOperation<byte[]> {
        private final UUID flowID;

        public ReceiveOperation(UUID flowID) {
            this.flowID = flowID;
        }

        @NotNull
        @Override
        public byte[] execute(@NotNull String deduplicationId) {
            // This method will run on a thread pool provided by Corda.
            // We don't use the dedupe ID at the moment, as a restart would wipe the enclave state anyway.
            CompletableFuture<byte[]> future = mailFutures.get(flowID);
            if (future == null)
                throw new RuntimeException("Unknown flow ID: " + flowID);
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                mailFutures.remove(flowID);
            }
        }
    }
}
