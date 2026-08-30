package com.twohearts.app.services.security

import com.twohearts.app.data.settings.SecureStorage
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.lifecycle.LifecycleListener
import com.twohearts.app.services.lifecycle.LifecycleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.concurrent.CopyOnWriteArrayList

/**
 * AppLockService — app lock management.
 *
 * Matches legacy AppLockService exactly:
 * - Enable/disable app lock
 * - Create/verify/change PIN
 * - Lock/unlock state
 * - Re-lock on foreground after timeout
 * - Lock state is MEMORY-ONLY (cold start always locks)
 */
class AppLockService(
    private val secureStorage: SecureStorage,
    private val settingsStorage: SettingsStorage,
    private val lifecycleService: LifecycleService
) {

    private val listeners = CopyOnWriteArrayList<AppLockListener>()

    private val _lockState = MutableStateFlow<AppLockState>(AppLockState.DISABLED)
    val lockState: StateFlow<AppLockState> = _lockState.asStateFlow()

    private var isUnlocked = false

    /**
     * Initialize app lock service.
     * Cold start always locks when enabled.
     */
    suspend fun initialize() {
        val settings = settingsStorage.settings.first()
        if (settings.appLockEnabled) {
            _lockState.value = AppLockState.LOCKED
            isUnlocked = false
        } else {
            _lockState.value = AppLockState.DISABLED
        }

        // Listen for foreground events to re-lock
        val lockTimeout = settings.first().lockTimeoutSeconds
        lifecycleService.addListener(object : LifecycleListener {
            override fun onForeground() {
                if (_lockState.value == AppLockState.UNLOCKED) {
                    if (lifecycleService.hasExceededTimeout(lockTimeout)) {
                        lock()
                    }
                }
            }
        })
    }

    /**
     * Check if app lock is enabled.
     */
    fun isEnabled(): Boolean {
        return _lockState.value != AppLockState.DISABLED
    }

    /**
     * Check if app is locked.
     */
    fun isLocked(): Boolean {
        return _lockState.value == AppLockState.LOCKED
    }

    /**
     * Check if PIN is set.
     */
    fun hasPin(): Boolean {
        return secureStorage.hasPin()
    }

    /**
     * Create a new PIN.
     */
    suspend fun createPin(pin: String) {
        val salt = PinHash.generateSalt()
        val hash = PinHash.hash(pin, salt)
        secureStorage.storePinMaterial(PinHash.encodeSalt(salt), hash)
        settingsStorage.setAppLockEnabled(true)
        _lockState.value = AppLockState.LOCKED
        notifyEnabled()
    }

    /**
     * Verify a PIN.
     */
    fun verifyPin(pin: String): Boolean {
        val saltBase64 = secureStorage.getPinSalt() ?: return false
        val storedHash = secureStorage.getPinVerifier() ?: return false
        val salt = PinHash.parseSalt(saltBase64)
        return PinHash.verify(pin, salt, storedHash)
    }

    /**
     * Unlock the app with a PIN.
     */
    fun unlock(pin: String): Boolean {
        if (verifyPin(pin)) {
            isUnlocked = true
            _lockState.value = AppLockState.UNLOCKED
            notifyUnlocked()
            return true
        }
        return false
    }

    /**
     * Lock the app.
     */
    fun lock() {
        isUnlocked = false
        _lockState.value = AppLockState.LOCKED
        notifyLocked()
    }

    /**
     * Disable app lock.
     */
    suspend fun disable() {
        secureStorage.clearPin()
        settingsStorage.setAppLockEnabled(false)
        _lockState.value = AppLockState.DISABLED
        isUnlocked = false
        notifyDisabled()
    }

    /**
     * Change PIN (requires old PIN verification).
     */
    suspend fun changePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) {
            return false
        }
        createPin(newPin)
        return true
    }

    /**
     * Add lock state listener.
     */
    fun addListener(listener: AppLockListener) {
        listeners.add(listener)
    }

    /**
     * Remove lock state listener.
     */
    fun removeListener(listener: AppLockListener) {
        listeners.remove(listener)
    }

    private fun notifyEnabled() { listeners.forEach { it.onEnabled() } }
    private fun notifyDisabled() { listeners.forEach { it.onDisabled() } }
    private fun notifyLocked() { listeners.forEach { it.onLocked() } }
    private fun notifyUnlocked() { listeners.forEach { it.onUnlocked() } }
}

/**
 * App lock state enum.
 */
enum class AppLockState {
    DISABLED,
    LOCKED,
    UNLOCKED
}

/**
 * App lock listener interface.
 */
interface AppLockListener {
    fun onEnabled() {}
    fun onDisabled() {}
    fun onLocked() {}
    fun onUnlocked() {}
}
