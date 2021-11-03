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

public class RewardsState implements ContractState, LinearState {

    public final AnonymousParty provider;
    public final Party host;
    public final byte[] rewards;
    public final Date dateCreated;
    public final UniqueIdentifier linearId;
    public final ProviderAggregationState stateAndRef;



    public RewardsState(AnonymousParty provider, Party host, byte[] rewards, Date dateCreated, UniqueIdentifier linearId, ProviderAggregationState stateAndRef) {
        this.provider = provider;
        this.host = host;
        this.rewards = rewards;
        this.dateCreated = dateCreated;
        this.linearId = linearId;
        this.stateAndRef = stateAndRef;
    }

    public byte[] getRewards() {
        return rewards;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public ProviderAggregationState getStateAndRef() {
        return stateAndRef;
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
}
