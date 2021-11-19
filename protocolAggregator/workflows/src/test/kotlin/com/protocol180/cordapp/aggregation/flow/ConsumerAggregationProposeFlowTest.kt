package com.protocol180.cordapp.aggregation.flow

import com.protocol180.aggregator.contracts.ConsumerAggregationContract
import com.protocol180.aggregator.states.ConsumerAggregationState
import com.protocol180.commons.SchemaType
import groovy.util.GroovyTestCase.assertEquals
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.finance.*
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.*
import org.junit.*
import kotlin.test.assertFailsWith

/**
 * Practical exercise instructions Flows part 1.
 * Uncomment the unit tests and use the hints + unit test body to complete the Flows such that the unit tests pass.
 * Note! These tests rely on Quasar to be loaded, set your run configuration to "-ea -javaagent:lib/quasar.jar"
 * Run configuration can be edited in IntelliJ under Run -> Edit Configurations -> VM options
 * On some machines/configurations you may have to provide a full path to the quasar.jar file.
 * On some machines/configurations you may have to use the "JAR manifest" option for shortening the command line.
 */
class ConsumerAggregationProposeFlowTest {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
            TestCordapp.findCordapp("com.protocol180.aggregator.contracts"),
            TestCordapp.findCordapp("com.protocol180.cordapp.aggregation.flow")
        ), networkParameters = testNetworkParameters(minimumPlatformVersion = 4)))
        a = network.createPartyNode()
        b = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    /**
     * Task 1.
     * Build out the [ConsumerAggregationProposeFlow]!
     * TODO: Implement the [ConsumerAggregationProposeFlow] flow which builds and returns a partially [SignedTransaction].
     * Hint:
     * - There's a lot to do to get this unit test to pass!
     * - Create a [TransactionBuilder] and pass it a notary reference.
     * -- A notary [Party] object can be obtained from [FlowLogic.serviceHub.networkMapCache].
     * -- In this training project there is only one notary
     * - Create an [ConsumerAggregationContract.Commands.Propose] inside a new [Command].
     * -- The required signers will be the same as the state's participants
     * -- Add the [Command] to the transaction builder [addCommand].
     * - Use the flow's [ConsumerAggregationProposeFlow] parameter as the output state with [addOutputState]
     * - Extra credit: use [TransactionBuilder.withItems] to create the transaction instead
     * - Sign the transaction and convert it to a [SignedTransaction] using the [serviceHub.signInitialTransaction] method.
     * - Return the [SignedTransaction].
     */
    @Test
    fun flowReturnsCorrectlyFormedPartiallySignedTransaction() {
        val consumer = a.info.chooseIdentityAndCert().party
        val host = b.info.chooseIdentityAndCert().party
        val consumerAggState = ConsumerAggregationState(consumer.anonymise(), host, null, null, null, SchemaType.TYPE_SCHEMA1)
        val flow = ConsumerAggregationProposeFlow(consumerAggState)
        val future = a.startFlow(flow)
        network.runNetwork()
        // Return the unsigned(!) SignedTransaction object from the ConsumerAggregationFlow.
        val ptx: SignedTransaction = future.getOrThrow()
        // Print the transaction for debugging purposes.
        println("ptx available is "+ptx.tx)
        // Check the transaction is well formed...
        // No outputs, one input ConsumerAggregationState and a command with the right properties.
        assert(ptx.tx.inputs.isEmpty())
        assert(ptx.tx.outputs.single().data is ConsumerAggregationState)
        val command = ptx.tx.commands.single()
        assert(command.value is ConsumerAggregationContract.Commands.Propose)
        assert(command.signers.toSet() == consumerAggState.participants.map { it.owningKey }.toSet())
        ptx.verifySignaturesExcept(
                host.owningKey,
                network.defaultNotaryNode.info.legalIdentitiesAndCerts.first().owningKey
        )
    }


}
