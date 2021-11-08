package com.protocol180.aggregator.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.StaticPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProviderAggregationState implements ContractState {
    public final AnonymousParty provider;
    public final Party host;
    public final String failedReason;
    public final byte[] dataInput;
    private final StaticPointer<ConsumerAggregationState> pointedTolAggregationState;
    private final StaticPointer<RewardsState> pointedToRewardState;


    public ProviderAggregationState(AnonymousParty provider, Party host, String failedReason, byte[] dataInput, StaticPointer<ConsumerAggregationState> pointedTolAggregationState, StaticPointer<RewardsState> pointedToRewardState) {
        this.provider = provider;
        this.host = host;
        this.failedReason = failedReason;
        this.dataInput = dataInput;
        this.pointedTolAggregationState = pointedTolAggregationState;
        this.pointedToRewardState = pointedToRewardState;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(provider, host);
    }

    public String getFailedReason() {
        return failedReason;
    }

    public byte[] getDataInput() {
        return dataInput;
    }

    public StaticPointer<ConsumerAggregationState> getPointedTolAggregationState() {
        return pointedTolAggregationState;
    }

    public StaticPointer<RewardsState> getPointedToRewardState() {
        return pointedToRewardState;
    }
}
