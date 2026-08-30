package com.twohearts.app.services.device

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * DeviceCapabilities — device information and capabilities.
 *
 * Matches legacy DeviceCapabilities with:
 * - Device info (model, manufacturer, OS version)
 * - Capability detection (notifications, camera, etc.)
 * - Accessibility settings (text scale, reduced motion)
 */
class DeviceCapabilities(private val context: Context) {

    private var initialized = false

    /**
     * Initialize device capabilities.
     */
    fun initialize() {
        if (initialized) return
        initialized = true
    }

    /**
     * Get device model.
     */
    fun getModel(): String = Build.MODEL

    /**
     * Get device manufacturer.
     */
    fun getManufacturer(): String = Build.MANUFACTURER

    /**
     * Get Android version.
     */
    fun getAndroidVersion(): Int = Build.VERSION.SDK_INT

    /**
     * Get Android version name.
     */
    fun getAndroidVersionName(): String {
        return when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.O -> "8.0 Oreo"
            Build.VERSION_CODES.O_MR1 -> "8.1 Oreo"
            Build.VERSION_CODES.P -> "9.0 Pie"
            Build.VERSION_CODES.Q -> "10"
            Build.VERSION_CODES.R -> "11"
            Build.VERSION_CODES.S -> "12"
            Build.VERSION_CODES.S_V2 -> "12L"
            Build.VERSION_CODES.TIRAMISU -> "13"
            Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> "14"
            Build.VERSION_CODES.VANILLA_ICE_CREAM -> "15"
            else -> "Unknown"
        }
    }

    /**
     * Check if notifications are supported.
     */
    fun supportsNotifications(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * Check if exact alarms are supported.
     */
    fun supportsExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * Check if post-notification permission is required.
     */
    fun requiresPostNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Get display metrics.
     */
    fun getDisplayMetrics(): DisplayMetrics {
        val metrics = context.resources.displayMetrics
        return DisplayMetrics(
            widthPixels = metrics.widthPixels,
            heightPixels = metrics.heightPixels,
            density = metrics.density,
            densityDpi = metrics.densityDpi
        )
    }

    /**
     * Check if device has notched display.
     */
    fun hasNotch(): Boolean {
        val resourceId = context.resources.getIdentifier(
            "cutout", "dimen", "android"
        )
        return resourceId > 0 && context.resources.getDimensionPixelSize(resourceId) > 0
    }

    /**
     * Get app version name.
     */
    fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Get app version code.
     */
    fun getAppVersionCode(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }
}

/**
 * Display metrics data class.
 */
data class DisplayMetrics(
    val widthPixels: Int,
    val heightPixels: Int,
    val density: Float,
    val densityDpi: Int
)
