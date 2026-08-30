package com.twohearts.app.services.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PinHash — PBKDF2-HMAC-SHA-256 PIN hashing.
 *
 * Matches legacy pinHash.ts exactly:
 * - PBKDF2-HMAC-SHA-256
 * - 120,000 iterations
 * - 128-bit random salt
 * - Base64 encoding
 * - Constant-time comparison
 */
object PinHash {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val SALT_SIZE_BYTES = 16 // 128 bits
    private const val KEY_SIZE_BITS = 256

    /**
     * Generate a random salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Hash a PIN with a salt.
     * Returns Base64-encoded hash.
     */
    fun hash(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            ITERATIONS,
            KEY_SIZE_BITS
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Verify a PIN against a stored hash.
     * Uses constant-time comparison to prevent timing attacks.
     */
    fun verify(pin: String, salt: ByteArray, storedHash: String): Boolean {
        val computedHash = hash(pin, salt)
        return constantTimeEquals(computedHash, storedHash)
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    /**
     * Parse Base64-encoded salt.
     */
    fun parseSalt(saltBase64: String): ByteArray {
        return Base64.decode(saltBase64, Base64.NO_WRAP)
    }

    /**
     * Encode salt to Base64.
     */
    fun encodeSalt(salt: ByteArray): String {
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }
}
