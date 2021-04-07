package com.protocol180.aggregator.enclave

import java.security.PrivateKey
import java.security.PublicKey

data class PostOfficeMapKey(
    val senderPrivateKey: PrivateKey,
    val recipientPublicKey: PublicKey,
    val topic: String)