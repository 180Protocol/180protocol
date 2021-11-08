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

import java.util.Date;
import java.util.List;

public class RewardsState implements ContractState{

    public final AnonymousParty provider;
    public final Party host;
    public final byte[] rewards;
    public final Date dateCreated;
    private final StaticPointer<ProviderAggregationState> pointedToState;



public RewardsState(AnonymousParty provider, Party host, byte[] rewards, Date dateCreated,  StaticPointer<ProviderAggregationState> pointedToState) {
        this.provider = provider;
        this.host = host;
        this.rewards = rewards;
        this.dateCreated = dateCreated;
        this.pointedToState = pointedToState;
    }

    public byte[] getRewards() {
        return rewards;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(provider, host);
    }

    public StaticPointer<ProviderAggregationState> getPointedToState() {
        return pointedToState;
    }
}
