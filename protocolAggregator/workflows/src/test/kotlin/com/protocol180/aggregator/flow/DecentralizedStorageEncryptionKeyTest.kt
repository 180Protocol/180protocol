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
    fun decentralizedStorageEncryptionKeyTest() {
        val kek = AESUtil.convertSecretKeyToString(AESUtil.generateKey(256));
        val flow = DecentralizedStorageEncryptionKeyUpdateFlow(kek);
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