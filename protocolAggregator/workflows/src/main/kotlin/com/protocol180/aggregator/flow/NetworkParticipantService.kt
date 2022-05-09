package com.protocol180.aggregator.flow

import com.protocol180.aggregator.states.RoleType
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor

/**
 * A simple service used to retrieve the node's role based on its identity. Role is configured on the node CordApp configuration
 */
@CordaService
class NetworkParticipantService(private val hub: AppServiceHub) : SingletonSerializeAsToken() {

    companion object {
        const val PARTICIPANT_ROLE_CONFIG_KEY = "participantRole"
        const val ESTUARY_STORAGE_TOKEN = "estuaryToken"
        const val AZURE_TENANT_ID = "azureTenantID"
        const val AZURE_CLIENT_ID = "azureClientID"
        const val AZURE_CLIENT_SECRET = "azureClientSecret"
        const val AZURE_KEY_IDENTIFIER = "azureKeyIdentifier"
        private val log = loggerFor<NetworkParticipantService>()
    }

    private val _role: RoleType?
    private val _token: String
    private val _tenantId: String
    private val _clientId: String
    private val _clientSecret: String
    private val _keyIdentifier: String

    val role: RoleType
        get() = _role!!

    val token: String
        get() = _token

    val tenantId: String
        get() = _tenantId
    val clientId: String
        get() = _clientId
    val clientSecret: String
        get() = _clientSecret
    val keyIdentifier: String
        get() = _keyIdentifier

    init {
        val config = hub.getAppContext().config

        _role = if (config.exists(PARTICIPANT_ROLE_CONFIG_KEY)) {
            RoleType.valueOf(config.getString(PARTICIPANT_ROLE_CONFIG_KEY))
        } else {
            log.warn("Participant's role is not set, error will be raised if accessed in a flow")
            null
        }

        _token = if (config.exists(ESTUARY_STORAGE_TOKEN)) {
            config.getString(ESTUARY_STORAGE_TOKEN)
        } else {
            log.warn("Storage token is not set, error will be raised if accessed in a flow")
            null.toString()
        }

        _tenantId = if (config.exists(AZURE_TENANT_ID)) {
            config.getString(AZURE_TENANT_ID)
        } else {
            log.warn("Tenant ID is not set, error will be raised if accessed in a flow")
            null.toString()
        }

        _clientId = if (config.exists(AZURE_CLIENT_ID)) {
            config.getString(AZURE_CLIENT_ID)
        } else {
            log.warn("Client ID is not set, error will be raised if accessed in a flow")
            null.toString()
        }

        _clientSecret = if (config.exists(AZURE_CLIENT_SECRET)) {
            config.getString(AZURE_CLIENT_SECRET)
        } else {
            log.warn("Client secret is not set, error will be raised if accessed in a flow")
            null.toString()
        }

        _keyIdentifier = if (config.exists(AZURE_KEY_IDENTIFIER)) {
            config.getString(AZURE_KEY_IDENTIFIER)
        } else {
            log.warn("Key identifier is not set, error will be raised if accessed in a flow")
            null.toString()
        }
    }
}
