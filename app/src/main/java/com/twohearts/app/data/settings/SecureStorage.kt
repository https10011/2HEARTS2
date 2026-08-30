package com.twohearts.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * SecureStorage — EncryptedSharedPreferences-based implementation matching legacy SecureStore.
 *
 * This replaces the legacy @aparajita/capacitor-secure-storage implementation.
 * PIN material (salt + verifier) is stored using Android Keystore-backed
 * EncryptedSharedPreferences.
 *
 * Security properties preserved:
 * - PIN material never enters settings, UI state, or logs
 * - PBKDF2-HMAC-SHA-256 hashing (120k iterations, 128-bit salt)
 * - Constant-time comparison
 */
class SecureStorage(private val context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "twohearts_secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Store PIN hash material (salt + verifier).
     */
    fun storePinMaterial(salt: String, verifier: String) {
        sharedPreferences.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_VERIFIER, verifier)
            .apply()
    }

    /**
     * Retrieve PIN salt.
     */
    fun getPinSalt(): String? {
        return sharedPreferences.getString(KEY_PIN_SALT, null)
    }

    /**
     * Retrieve PIN verifier.
     */
    fun getPinVerifier(): String? {
        return sharedPreferences.getString(KEY_PIN_VERIFIER, null)
    }

    /**
     * Check if PIN is set.
     */
    fun hasPin(): Boolean {
        return sharedPreferences.contains(KEY_PIN_SALT) &&
                sharedPreferences.contains(KEY_PIN_VERIFIER)
    }

    /**
     * Clear PIN material.
     */
    fun clearPin() {
        sharedPreferences.edit()
            .remove(KEY_PIN_SALT)
            .remove(KEY_PIN_VERIFIER)
            .apply()
    }

    /**
     * Clear all secure storage.
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_VERIFIER = "pin_verifier"
    }
}
