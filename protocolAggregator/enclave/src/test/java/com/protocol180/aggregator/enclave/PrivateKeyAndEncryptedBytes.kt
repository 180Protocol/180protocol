package com.protocol180.aggregator.enclave

import java.security.PrivateKey

data class PrivateKeyAndEncryptedBytes(val privateKey: PrivateKey, val encryptedBytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateKeyAndEncryptedBytes

        if (privateKey != other.privateKey) return false
        if (!encryptedBytes.contentEquals(other.encryptedBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = privateKey.hashCode()
        result = 31 * result + encryptedBytes.contentHashCode()
        return result
    }
}
