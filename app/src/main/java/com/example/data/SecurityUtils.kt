package com.example.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val PIN_HASH_ALGORITHM = "PBKDF2WithHmacSHA256"
private const val PIN_HASH_ITERATIONS = 120_000
private const val PIN_KEY_LENGTH_BITS = 256
private const val PIN_SALT_BYTES = 16
private const val PIN_HASH_PREFIX = "pbkdf2-sha256"

object SecurityUtils {
    private val secureRandom = SecureRandom()

    fun hashPin(pin: String): String {
        require(isValidFourDigitPin(pin)) { "PIN must be exactly 4 digits" }
        val salt = ByteArray(PIN_SALT_BYTES).also(secureRandom::nextBytes)
        val hash = derivePinHash(pin, salt)
        return listOf(
            PIN_HASH_PREFIX,
            PIN_HASH_ITERATIONS.toString(),
            Base64.encodeToString(salt, Base64.NO_WRAP),
            Base64.encodeToString(hash, Base64.NO_WRAP)
        ).joinToString("\$")
    }

    fun verifyPin(pin: String, encodedHash: String?): Boolean {
        if (!isValidFourDigitPin(pin) || encodedHash.isNullOrBlank()) return false

        val parts = encodedHash.split("\$")
        if (parts.size != 4 || parts[0] != PIN_HASH_PREFIX) return false

        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { Base64.decode(parts[2], Base64.NO_WRAP) }.getOrNull() ?: return false
        val expectedHash = runCatching { Base64.decode(parts[3], Base64.NO_WRAP) }.getOrNull() ?: return false
        val candidateHash = derivePinHash(pin, salt, iterations)

        return MessageDigest.isEqual(candidateHash, expectedHash)
    }

    fun isValidFourDigitPin(pin: String): Boolean = pin.length == 4 && pin.all(Char::isDigit)

    private fun derivePinHash(
        pin: String,
        salt: ByteArray,
        iterations: Int = PIN_HASH_ITERATIONS
    ): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, PIN_KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(PIN_HASH_ALGORITHM).generateSecret(spec).encoded
    }
}
