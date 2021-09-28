package com.protocol180.aggregator.cordapp.host;

import com.google.common.collect.ImmutableList;
import com.protocol180.commons.MailType;
import com.protocol180.commons.ClientType;
import com.r3.conclave.mail.Curve25519PrivateKey;
import net.corda.core.concurrent.CordaFuture;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.protocol180.aggregator.cordapp.sample.host.AggregationFlowResponder;
import com.protocol180.aggregator.cordapp.sample.host.AggregationFlow;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregatorEnclaveTest {
    private static MockNetwork network;
    private static StartedMockNode client;
    private static StartedMockNode client2;
    private static StartedMockNode host;

    static Map<Curve25519PrivateKey, String> randomClients = new HashMap<>();
    static Curve25519PrivateKey provider1 = Curve25519PrivateKey.random();
    static Curve25519PrivateKey provider2 = Curve25519PrivateKey.random();
    static Curve25519PrivateKey consumer = Curve25519PrivateKey.random();
    static Curve25519PrivateKey provenance = Curve25519PrivateKey.random();

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
        client = network.createPartyNode(null);
        client2 = network.createPartyNode(null);
        host = network.createPartyNode(null);
        host.registerInitiatedFlow(AggregationFlowResponder.class);
//        network.runNetwork();
        System.out.println("public key of client is: "+client.getInfo().getLegalIdentities().get(0).getOwningKey().getEncoded());
        randomClients.put(provider1, ClientType.TYPE_PROVIDER);
        randomClients.put(provider2, ClientType.TYPE_PROVIDER);
        randomClients.put(consumer, ClientType.TYPE_CONSUMER);
        randomClients.put(provenance, ClientType.TYPE_PROVENANCE);
    }

    @AfterAll
    static void tearDown() {
        if (network != null) {
            network.stopNodes();
            network = null;
        }
        client = null;
        client2=null;
        host = null;
    }


    @Test
    public void testSchemaMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema=MockClientUtil.initializeSchema().toString().getBytes();
//        System.out.println("envelope schema with type is:"+new String(envelopeSchemaWithType));
        CordaFuture<String> flow = client.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(),true));

        network.runNetwork();
        assertEquals(new String(envelopeSchema), flow.get());
    }

    @Test
    public void testIdentitiesMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema=MockClientUtil.initializeSchema().toString().getBytes();

        CordaFuture<String> schemaFlow = client.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(),true));
        network.runNetwork();


        byte[] identitiesList= MockClientUtil.createMailForIdentities(randomClients);

        CordaFuture<String> identitiesFlow= client2.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), identitiesList, getConstraint(),true));
        network.runNetwork();

        assertEquals(new String(envelopeSchema), schemaFlow.get());
        assertEquals(new String(identitiesList), identitiesFlow.get());
    }




    private String getConstraint() {
        String mode = System.getProperty("enclaveMode");
        if (mode == null || !mode.toLowerCase().equals("mock"))
            return constraint;
        return mock_constraint;
    }
}
