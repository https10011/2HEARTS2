package com.twohearts.app.services.lifecycle

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.CopyOnWriteArrayList

/**
 * LifecycleService — app lifecycle event handling.
 *
 * Matches legacy AppLifecycle with:
 * - Foreground/background detection
 * - Back button handling
 * - Lifecycle event listeners
 * - Background time tracking
 */
class LifecycleService(application: Application) {

    private val listeners = CopyOnWriteArrayList<LifecycleListener>()
    private var lastBackgroundTime: Long = 0L
    private var isInBackground = false

    /**
     * Initialize lifecycle observation.
     */
    fun start() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // App came to foreground
                isInBackground = false
                notifyForeground()
            }

            override fun onStop(owner: LifecycleOwner) {
                // App went to background
                isInBackground = true
                lastBackgroundTime = System.currentTimeMillis()
                notifyBackground()
            }
        })
    }

    /**
     * Check if app is currently in background.
     */
    fun isInBackground(): Boolean = isInBackground

    /**
     * Get time spent in background (milliseconds).
     */
    fun getBackgroundDuration(): Long {
        if (!isInBackground) return 0
        return System.currentTimeMillis() - lastBackgroundTime
    }

    /**
     * Check if background time exceeds timeout.
     */
    fun hasExceededTimeout(timeoutSeconds: Int): Boolean {
        val backgroundMs = getBackgroundDuration()
        val timeoutMs = timeoutSeconds * 1000L
        return backgroundMs > timeoutMs
    }

    /**
     * Add lifecycle listener.
     */
    fun addListener(listener: LifecycleListener) {
        listeners.add(listener)
    }

    /**
     * Remove lifecycle listener.
     */
    fun removeListener(listener: LifecycleListener) {
        listeners.remove(listener)
    }

    /**
     * Notify listeners of foreground event.
     */
    private fun notifyForeground() {
        listeners.forEach { it.onForeground() }
    }

    /**
     * Notify listeners of background event.
     */
    private fun notifyBackground() {
        listeners.forEach { it.onBackground() }
    }

    /**
     * Handle back button press.
     * Returns true if handled, false if should use default behavior.
     */
    fun handleBackButton(): Boolean {
        // Default: let the system handle it
        // Feature-specific handling can be added here
        return false
    }
}

/**
 * Lifecycle listener interface.
 */
interface LifecycleListener {
    fun onForeground() {}
    fun onBackground() {}
}
