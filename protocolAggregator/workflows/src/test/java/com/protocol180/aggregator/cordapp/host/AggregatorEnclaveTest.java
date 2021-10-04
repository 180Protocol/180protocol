package com.protocol180.aggregator.cordapp.host;

import com.google.common.collect.ImmutableList;
import com.protocol180.aggregator.cordapp.sample.host.AggregationFlow;
import com.protocol180.aggregator.cordapp.sample.host.AggregationFlowResponder;
import com.protocol180.commons.ClientType;
import net.corda.core.concurrent.CordaFuture;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
        randomClients.put(getPublicKey(provider1), ClientType.TYPE_PROVIDER);
        randomClients.put(getPublicKey(provider2), ClientType.TYPE_PROVIDER);
        randomClients.put(getPublicKey(consumer), ClientType.TYPE_CONSUMER);
        randomClients.put(getPublicKey(provenance), ClientType.TYPE_PROVENANCE);

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


    @Test
    public void testSchemaMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema = MockClientUtil.envelopeSchema.toString().getBytes();
        CordaFuture<byte[]> flow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(), false));

        network.runNetwork();
        assertEquals(MockClientUtil.aggregationInputSchema.toString(), new String(flow.get()));
    }

    @Test
    public void testIdentitiesMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema = MockClientUtil.envelopeSchema.toString().getBytes();

        CordaFuture<byte[]> schemaFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(), false));
        network.runNetwork();


        byte[] identitiesList = mockClientUtil.createMailForIdentities(randomClients);

        CordaFuture<byte[]> identitiesFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), identitiesList, getConstraint(), false));
        network.runNetwork();

        assertEquals(MockClientUtil.aggregationInputSchema.toString(), new String(schemaFlow.get()));
        assertEquals("4", new String(identitiesFlow.get()));

    }

    @Test
    public void testProviderMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema = MockClientUtil.envelopeSchema.toString().getBytes();

        CordaFuture<byte[]> schemaFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(), false));
        network.runNetwork();


        byte[] identitiesList = mockClientUtil.createMailForIdentities(randomClients);

        CordaFuture<byte[]> identitiesFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), identitiesList, getConstraint(), false));
        network.runNetwork();

        byte[] provider1Data = mockClientUtil.createProviderMailForAggregationData();

        CordaFuture<byte[]> provider1Flow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provider1Data, getConstraint(), false));
        network.runNetwork();

        byte[] provider2Data = mockClientUtil.createProviderMailForAggregationData();

        CordaFuture<byte[]> provider2Flow = provider2.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provider2Data, getConstraint(), false));
        network.runNetwork();

        assertEquals(MockClientUtil.aggregationInputSchema.toString(), new String(schemaFlow.get()));
        assertEquals("4", new String(identitiesFlow.get()));
        assertEquals("2", new String(provider2Flow.get()));

    }


    @Test
    public void testConsumerMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema = MockClientUtil.envelopeSchema.toString().getBytes();

        CordaFuture<byte[]> schemaFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(), false));
        network.runNetwork();


        byte[] identitiesList = mockClientUtil.createMailForIdentities(randomClients);

        CordaFuture<byte[]> identitiesFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), identitiesList, getConstraint(), false));
        network.runNetwork();

        byte[] provider1Data = mockClientUtil.createProviderMailForAggregationData();

        CordaFuture<byte[]> provider1flow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provider1Data, getConstraint(), false));
        network.runNetwork();

        byte[] provider2Data = mockClientUtil.createProviderMailForAggregationData();

        CordaFuture<byte[]> provider2flow = provider2.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provider2Data, getConstraint(), false));
        network.runNetwork();

        byte[] consumerSchema = MockClientUtil.aggregationOutputSchema.toString().getBytes();

        CordaFuture<byte[]> consumerflow = consumer.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), consumerSchema, getConstraint(), false));
        network.runNetwork();

        ArrayList<GenericRecord> aggregationOutputDataRecords = mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(consumerflow.get(), "aggregate");


        assertEquals(MockClientUtil.aggregationInputSchema.toString(), new String(schemaFlow.get()));
        assertEquals("4", new String(identitiesFlow.get()));
        assertEquals("2", new String(provider2flow.get()));
        assertTrue(aggregationOutputDataRecords.size() > 0);
    }

    @Test
    public void testProvenanceMail() throws ExecutionException, InterruptedException, IOException {
        byte[] envelopeSchema = MockClientUtil.envelopeSchema.toString().getBytes();

        CordaFuture<byte[]> schemaFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), envelopeSchema,
                getConstraint(), false));
        network.runNetwork();


        byte[] identitiesList = mockClientUtil.createMailForIdentities(randomClients);

        CordaFuture<byte[]> identitiesFlow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), identitiesList, getConstraint(), false));
        network.runNetwork();

        byte[] provider1Data = mockClientUtil.createProviderMailForAggregationData();

        CordaFuture<byte[]> provider1flow = provider1.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provider1Data, getConstraint(), false));
        network.runNetwork();

        byte[] provider2Data = mockClientUtil.createProviderMailForAggregationData();

        CordaFuture<byte[]> provider2flow = provider2.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provider2Data, getConstraint(), false));
        network.runNetwork();

        byte[] consumerSchema = MockClientUtil.aggregationOutputSchema.toString().getBytes();

        CordaFuture<byte[]> consumerflow = consumer.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), consumerSchema, getConstraint(), false));
        network.runNetwork();

        ArrayList<GenericRecord> aggregationOutputDataRecords = mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(consumerflow.get(), "aggregate");

        byte[] provenanceSchema = MockClientUtil.provenanceOutputSchema.toString().getBytes();

        CordaFuture<byte[]> provenanceflow = provenance.startFlow(new AggregationFlow(host.getInfo().getLegalIdentities().get(0), provenanceSchema, getConstraint(), false));
        network.runNetwork();

        ArrayList<GenericRecord> provenanceOutputDataRecords = mockClientUtil.readGenericRecordsFromOutputBytesAndSchema(provenanceflow.get(), "provenance");


        //testing aggregation schema has been delivered to enclave
        assertEquals(MockClientUtil.aggregationInputSchema.toString(), new String(schemaFlow.get()));
        //testing number of identities provided to enclave for client validation
        assertEquals("4", new String(identitiesFlow.get()));
        // testing number of Data provider inside enclave
        assertEquals("2", new String(provider2flow.get()));
        //testing aggregation output records provided from client
        assertTrue(aggregationOutputDataRecords.size() > 0);
        //testing provenance output records provided from client
        assertTrue(provenanceOutputDataRecords.size() > 0);
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
