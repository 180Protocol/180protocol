package com.protocol180.aggregator.flow

import com.protocol180.aggregator.states.CoalitionConfigurationState
import com.protocol180.aggregator.states.CoalitionDataType
import com.protocol180.aggregator.states.RoleType
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.internal.readFully
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CoalitionConfigurationTest {
    lateinit var network: MockNetwork
    lateinit var consumer: StartedMockNode
    lateinit var host: StartedMockNode
    lateinit var provider1: StartedMockNode
    lateinit var provider2: StartedMockNode
    lateinit var provider3: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.protocol180.aggregator.contracts"),
                    TestCordapp.findCordapp("com.protocol180.aggregator.flow")
                )
            )
        )
        consumer = prepareNodeForRole(RoleType.DATA_CONSUMER)
        host = prepareNodeForRole(RoleType.COALITION_HOST)
        provider1 = prepareNodeForRole(RoleType.DATA_PROVIDER)
        provider2 = prepareNodeForRole(RoleType.DATA_PROVIDER)
        provider3 = prepareNodeForRole(RoleType.DATA_PROVIDER)
        network.runNetwork()
    }

    private fun prepareNodeForRole(role: RoleType) : StartedMockNode {
        return network.createNode(
                parameters = MockNodeParameters(
                    additionalCordapps = listOf(
                        TestCordapp.findCordapp("com.protocol180.aggregator.flow")
                            .withConfig(mapOf(Pair(NetworkParticipantService.PARTICIPANT_ROLE_CONFIG_KEY, role.name)))
                )
            )
        )
    }


    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun createCoalitionConfigurationStateAndTestUpdate() {
        var coalitionPartyToRole = mapOf(RoleType.COALITION_HOST to setOf(host.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_CONSUMER to setOf(consumer.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_PROVIDER to setOf(provider1.info.chooseIdentityAndCert().party.name,
                provider2.info.chooseIdentityAndCert().party.name))

        val dataTypes = listOf(CoalitionDataType("testDataType1", "Test Data Type 1",
                ClassLoader.getSystemClassLoader().getResourceAsStream("testSchema1.avsc").readFully(), "com.protocol180.aggregator.sample.ExampleAggregationEnclave"),
            CoalitionDataType("testDataType2", "Test Data Type 2",
                ClassLoader.getSystemClassLoader().getResourceAsStream("testSchema2.avsc").readFully(), "com.protocol180.aggregator.sample.ExampleAggregationEnclave"))

        val flow1 = CoalitionConfigurationUpdateFlow(coalitionPartyToRole, dataTypes)
        val future1 = host.startFlow(flow1)
        network.runNetwork()
        val signedTransaction = future1.get()
        assertEquals(1, signedTransaction.tx.outputStates.size)
        val output1 = signedTransaction.tx.getOutput(0) as CoalitionConfigurationState

        /*assertEquals(coalitionPartyToRole.values, output1.coalitionPartyToRole.mapValues {
                entry -> entry.value.map{it.name}.toSet() }.values)*/
        assertEquals(coalitionPartyToRole.values.fold(listOf<CordaX500Name>()){ acc, e -> acc + e }, output1.participants.map{it.nameOrNull()})
        assertEquals(dataTypes, output1.supportedCoalitionDataTypes)

        //add new provider node to network
        coalitionPartyToRole = mapOf(RoleType.COALITION_HOST to setOf(host.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_CONSUMER to setOf(consumer.info.chooseIdentityAndCert().party.name),
            RoleType.DATA_PROVIDER to setOf(provider1.info.chooseIdentityAndCert().party.name,
                provider2.info.chooseIdentityAndCert().party.name, provider3.info.chooseIdentityAndCert().party.name))

        val flow2 = CoalitionConfigurationUpdateFlow(coalitionPartyToRole, dataTypes)
        val future2 = host.startFlow(flow2)
        network.runNetwork()

        val signedTransaction2 = future2.get()
        assertEquals(1, signedTransaction2.tx.outputStates.size)
        val output2 = signedTransaction2.tx.getOutput(0) as CoalitionConfigurationState

        assertEquals(coalitionPartyToRole.values.fold(listOf<CordaX500Name>()){ acc, e -> acc + e }, output2.participants.map{it.nameOrNull()})
        assertEquals(dataTypes, output2.supportedCoalitionDataTypes)
        assertEquals(output1.linearId, output2.linearId)

        host.transaction {
            val coalitionConfigurationStateAndRefs : List<StateAndRef<CoalitionConfigurationState>> = host.services.vaultService.
            queryBy<CoalitionConfigurationState>(QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.UNCONSUMED)).
            states
            //only one unconsumed coalition configuration state after update
            assertEquals(coalitionConfigurationStateAndRefs.size, 1)
        }

    }

}