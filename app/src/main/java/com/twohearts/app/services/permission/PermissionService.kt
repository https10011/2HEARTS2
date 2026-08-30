package com.twohearts.app.services.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * PermissionService — runtime permission handling.
 *
 * Matches legacy PermissionService with:
 * - Permission state tracking (granted, denied, prompt, unavailable)
 * - Notification permission handling
 * - Permission request coordination
 */
class PermissionService(private val context: Context) {

    /**
     * Get notification permission state.
     */
    fun getNotificationPermissionState(): PermissionState {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Pre-Android 13: notifications don't require runtime permission
            PermissionState.GRANTED
        } else {
            when (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )) {
                PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
                PackageManager.PERMISSION_DENIED -> PermissionState.DENIED
                else -> PermissionState.PROMPT
            }
        }
    }

    /**
     * Check if notification permission is granted.
     */
    fun hasNotificationPermission(): Boolean {
        return getNotificationPermissionState() == PermissionState.GRANTED
    }

    /**
     * Check if exact alarm permission is granted.
     */
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Get all required permissions.
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions
    }

    /**
     * Check if all required permissions are granted.
     */
    fun hasAllRequiredPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

/**
 * Permission state enum.
 */
enum class PermissionState {
    GRANTED,
    DENIED,
    PROMPT,
    UNAVAILABLE
}
