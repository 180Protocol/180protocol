package com.protocol180.aggregator.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class DataOutputState implements ContractState, LinearState {

    public final AnonymousParty consumer;
    public final Party host;
    public final byte[] dataOutput;
    public final Date dateCreate;

    public byte[] getDataOutput() {
        return dataOutput;
    }

    public Date getDateCreate() {
        return dateCreate;
    }

    public ConsumerAggregationState getStateAndRef() {
        return stateAndRef;
    }

    public final ConsumerAggregationState stateAndRef;

    public final UniqueIdentifier linearId;

    public DataOutputState(AnonymousParty consumer, Party host, byte[] dataOutput, Date dateCreate, ConsumerAggregationState stateAndRef, UniqueIdentifier linearId) {
        this.consumer = consumer;
        this.host = host;
        this.dataOutput = dataOutput;
        this.dateCreate = dateCreate;
        this.stateAndRef = stateAndRef;
        this.linearId = linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(consumer, host);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }
}
