package com.protocol180.aggregator.host;

import com.google.common.collect.ImmutableList;
import com.protocol180.aggregator.sample.host.AggregationFlowResponder;
import com.protocol180.commons.ClientType;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregatorEnclaveTest {
    private static MockNetwork network;
    private static StartedMockNode provider1;
    private static StartedMockNode provider2;
    private static StartedMockNode consumer;
    private static StartedMockNode provenance;
    private static StartedMockNode host;

    private static MockClientUtil mockClientUtil;

    static Map<PublicKey, String> randomClients;

    // DO NOT BLINDLY COPY PASTE THIS. The SEC:INSECURE part does what it sounds like: turns off security for convenience
    // when unit testing and developing.
    //
    // Obviously in a real app you'd not use SEC:INSECURE, however this makes the sample work in simulation mode.
    private final String constraint = "S:4924CA3A9C8241A3C0AA1A24A407AA86401D2B79FA9FF84932DA798A942166D4 PROD:1 SEC:INSECURE";
    private final String mock_constraint = "S:0000000000000000000000000000000000000000000000000000000000000000 PROD:1 SEC:INSECURE";

    @BeforeAll
    static void setup() {
        System.setProperty("log4j.configurationFile", "logging.xml");
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(
                ImmutableList.of(TestCordapp.findCordapp("com.protocol180.aggregator.cordapp.sample.host"))
        ));
        provider1 = network.createPartyNode(null);
        provider2 = network.createPartyNode(null);
        consumer = network.createPartyNode(null);
        provenance = network.createPartyNode(null);
        host = network.createPartyNode(null);
        host.registerInitiatedFlow(AggregationFlowResponder.class);
//        network.runNetwork();
        System.out.println("public key of client1 is: " + Base64.getEncoder().encodeToString(provider2.getInfo().getLegalIdentities().get(0).getOwningKey().getEncoded()));
        System.out.println("public key of client2 is: " + Base64.getEncoder().encodeToString(provider2.getInfo().getLegalIdentities().get(0).getOwningKey().getEncoded()));

        randomClients = new HashMap<>();
        randomClients.put(getPublicKey(provider1), ClientType.TYPE_PROVIDER.type);
        randomClients.put(getPublicKey(provider2), ClientType.TYPE_PROVIDER.type);
        randomClients.put(getPublicKey(consumer), ClientType.TYPE_CONSUMER.type);
        randomClients.put(getPublicKey(provenance), ClientType.TYPE_PROVENANCE.type);

        mockClientUtil = new MockClientUtil();
    }

    @AfterAll
    static void tearDown() {
        if (network != null) {
            network.stopNodes();
            network = null;
        }
        provider1 = null;
        provider2 = null;
        consumer = null;
        provenance = null;
        host = null;
    }


    private static PublicKey getPublicKey(StartedMockNode node) {
        return node.getInfo().getLegalIdentities().get(0).getOwningKey();
    }

    private String getConstraint() {
        String mode = System.getProperty("enclaveMode");
        if (mode == null || !mode.toLowerCase().equals("mock"))
            return constraint;
        return mock_constraint;
    }
}
