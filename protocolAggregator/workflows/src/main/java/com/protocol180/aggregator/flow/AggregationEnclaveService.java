package com.protocol180.aggregator.flow;

import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import org.jetbrains.annotations.NotNull;

/**
 * This just loads the specific enclave we want. We can have as many of these as RAM permits.
 */
@CordaService
public class AggregationEnclaveService extends EnclaveHostService {
    public AggregationEnclaveService(@NotNull AppServiceHub serviceHub) {
        super("com.protocol180.aggregator.clientEnclave.ExtendedAggregationEnclave");
        System.out.println("inside Aggregation Enclave service");
    }
}
