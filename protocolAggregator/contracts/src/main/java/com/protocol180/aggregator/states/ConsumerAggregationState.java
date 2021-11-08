package com.protocol180.aggregator.states;

import com.google.common.collect.ImmutableList;
import com.protocol180.commons.SchemaType;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StaticPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConsumerAggregationState implements ContractState {

    public final AnonymousParty consumer;
    public final Party host;
    public final String failedReason;
    private final StaticPointer<ProviderAggregationState> pointedToProviderAggregationState;
    private final StaticPointer<DataOutputState> pointedToDataOutputState;
//    public final SchemaType
    public final SchemaType dataType;

    @ConstructorForDeserialization
    public ConsumerAggregationState(AnonymousParty consumer, Party host, String failedReason, StaticPointer<ProviderAggregationState> pointedToProviderAggregationState, StaticPointer<DataOutputState> pointedToDataOutputState, SchemaType dataType) {
        this.consumer = consumer;
        this.host = host;
        this.failedReason = failedReason;
        this.pointedToProviderAggregationState = pointedToProviderAggregationState;
        this.pointedToDataOutputState = pointedToDataOutputState;
        this.dataType = dataType;
    }


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(consumer, host);
    }

    public String getFailedReason() {
        return failedReason;
    }

    public SchemaType getDataType() {
        return dataType;
    }

    public StaticPointer<ProviderAggregationState> getPointedToProviderAggregationState() {
        return pointedToProviderAggregationState;
    }

    public StaticPointer<DataOutputState> getPointedToDataOutputState() {
        return pointedToDataOutputState;
    }
}
