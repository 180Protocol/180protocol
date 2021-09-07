package com.r3.conclave.cordapp.sample.host;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.conclave.cordapp.sample.client.EnclaveClientHelper;
import com.r3.conclave.cordapp.sample.client.EnclaveFlowResponder;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;

@InitiatedBy(AggregationFlow.class)
public class AggregationFlowResponder extends FlowLogic<Void> {
    private final FlowSession counterpartySession;

    public AggregationFlowResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        EnclaveFlowResponder session =
                EnclaveClientHelper.initiateResponderFlow(this, counterpartySession, AggregationEnclaveService.class);

        session.relayMessageToFromEnclave();

        return null;
    }
}
