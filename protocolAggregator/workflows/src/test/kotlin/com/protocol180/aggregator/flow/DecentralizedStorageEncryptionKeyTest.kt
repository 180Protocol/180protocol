package com.protocol180.aggregator.flow

import com.protocol180.aggregator.states.RoleType
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class DecentralizedStorageEncryptionKeyTest {
    lateinit var network: MockNetwork
    lateinit var consumer: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.protocol180.aggregator.contracts"),
                    TestCordapp.findCordapp("com.protocol180.aggregator.flow"),
                    TestCordapp.findCordapp("com.protocol180.aggregator.storage")
                )
            )
        )
        consumer = prepareNodeForRole(RoleType.DATA_CONSUMER)
        network.runNetwork()
    }

    private fun prepareNodeForRole(role: RoleType): StartedMockNode {
        return network.createNode(
            parameters = MockNodeParameters(
                additionalCordapps = listOf(
                    TestCordapp.findCordapp("com.protocol180.aggregator.flow")
                        .withConfig(
                            mapOf(
                                Pair(NetworkParticipantService.PARTICIPANT_ROLE_CONFIG_KEY, role.name),
                                Pair(
                                    NetworkParticipantService.ESTUARY_STORAGE_TOKEN,
                                    "EST5b9e4a3a-4978-46a0-8ac7-703711dbee4fARY"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_TENANT_ID,
                                    "c7e48871-70ff-48d5-8934-a8e594bc040b"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_ID,
                                    "dd115d03-42ee-43db-98e1-8b6a475275f8"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_SECRET,
                                    "KEZmQT-~mvUlNS61E1ZBj75W~DrSTZtYKO"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_KEY_IDENTIFIER,
                                    "keyvaulttest180p.vault.azure.net/keys/180PTest/06ff554fc7af4be685eec9f841ce2e60"
                                )
                            )
                        )
                )
            )
        )
    }


    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun decentralizedStorageEncryptionKeyTest() {
        val updateFlow = DecentralizedStorageEncryptionKeyUpdateFlow();
        consumer.startFlow(updateFlow)
        network.runNetwork()

        val decentralizedStorageEncryptionKeyRetrievalFlow = DecentralizedStorageEncryptionKeyRetrievalFlow();
        val decentralizedStorageEncryptionKeyFuture = consumer.startFlow(decentralizedStorageEncryptionKeyRetrievalFlow)
        val decentralizedStorageEncryptionKeyRecord = decentralizedStorageEncryptionKeyFuture.get()
        assertNotNull(decentralizedStorageEncryptionKeyRecord)
        println("Record : $decentralizedStorageEncryptionKeyRecord")
        network.runNetwork()
    }
}