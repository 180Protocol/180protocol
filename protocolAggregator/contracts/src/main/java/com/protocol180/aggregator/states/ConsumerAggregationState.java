package com.protocol180.aggregator.states;

import com.google.common.collect.ImmutableList;
import com.protocol180.commons.SchemaType;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConsumerAggregationState implements ContractState, LinearState {

    public final AnonymousParty consumer;
    public final Party host;
    public final String failedReason;
    public final List<ProviderAggregationState> providerAggregationStatesList;
    public final DataOutputState dataOutputState;
//    public final SchemaType
    public final SchemaType dataType;

    private final UniqueIdentifier linearId;

    @ConstructorForDeserialization
    public ConsumerAggregationState(AnonymousParty consumer, Party host, String failedReason, List<ProviderAggregationState> providerAggregationStatesList, DataOutputState dataOutputState, SchemaType dataType, UniqueIdentifier linearId) {
        this.consumer = consumer;
        this.host = host;
        this.failedReason = failedReason;
        this.providerAggregationStatesList = providerAggregationStatesList;
        this.dataOutputState = dataOutputState;
        this.dataType = dataType;
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

    public String getFailedReason() {
        return failedReason;
    }

    public List<ProviderAggregationState> getProviderAggregationStatesList() {
        return providerAggregationStatesList;
    }

    public DataOutputState getDataOutputState() {
        return dataOutputState;
    }

    public SchemaType getDataType() {
        return dataType;
    }
}
