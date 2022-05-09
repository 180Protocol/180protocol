package com.protocol180.aggregator.flow

import com.protocol180.aggregator.states.RoleType
import com.protocol180.aggregator.utils.AESUtil
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
                    TestCordapp.findCordapp("com.protocol180.aggregator.flow")
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
                                    "EST8752f2e8-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_TENANT_ID,
                                    "c7e48871-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_ID,
                                    "dd115d03-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_SECRET,
                                    "KEZmQTXXXXXXXXXXXXXXXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_KEY_IDENTIFIER,
                                    "https://XXXXXXXXX.vault.azure.net/keys/XXXXXXXXX/06ff554fc7af4be685eec9f841ce2e60"
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
        val flow = DecentralizedStorageEncryptionKeyUpdateFlow();
        consumer.startFlow(flow)
        network.runNetwork()

        val decentralizedStorageEncryptionKeyRetrievalFlow = DecentralizedStorageEncryptionKeyRetrievalFlow();
        val decentralizedStorageEncryptionKeyFuture = consumer.startFlow(decentralizedStorageEncryptionKeyRetrievalFlow)
        val decentralizedStorageEncryptionKeyRecord = decentralizedStorageEncryptionKeyFuture.get()
        assertNotNull(decentralizedStorageEncryptionKeyRecord)
        println("Record : $decentralizedStorageEncryptionKeyRecord")
        network.runNetwork()
    }
}