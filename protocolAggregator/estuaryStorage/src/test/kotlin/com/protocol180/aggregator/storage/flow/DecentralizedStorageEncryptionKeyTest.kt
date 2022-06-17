package com.protocol180.aggregator.storage.flow

import com.protocol180.aggregator.flow.NetworkParticipantService
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
                                    "ESTXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_TENANT_ID,
                                    "XXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_ID,
                                    "XXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_CLIENT_SECRET,
                                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
                                ),
                                Pair(
                                    NetworkParticipantService.AZURE_KEY_IDENTIFIER,
                                    "keyvaulttestXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
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