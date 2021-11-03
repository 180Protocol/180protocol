package com.protocol180.aggregator.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProviderAggregationState implements ContractState, LinearState {
    public final AnonymousParty provider;
    public final Party host;
    public final String failedReason;
    public final byte[] dataInput;
    public final ConsumerAggregationState consumerAggregationState;
    public final RewardsState rewardsState;

    public final UniqueIdentifier linearId;

    public ProviderAggregationState(AnonymousParty provider, Party host, String failedReason, byte[] dataInput, ConsumerAggregationState consumerAggregationState, RewardsState rewardsState, UniqueIdentifier linearId) {
        this.provider = provider;
        this.host = host;
        this.failedReason = failedReason;
        this.dataInput = dataInput;
        this.consumerAggregationState = consumerAggregationState;
        this.rewardsState = rewardsState;
        this.linearId = linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(provider, host);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public byte[] getDataInput() {
        return dataInput;
    }

    public ConsumerAggregationState getConsumerAggregationState() {
        return consumerAggregationState;
    }

    public RewardsState getRewardsState() {
        return rewardsState;
    }
}
