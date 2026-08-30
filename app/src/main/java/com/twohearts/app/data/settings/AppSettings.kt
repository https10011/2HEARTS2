package com.twohearts.app.data.settings

/**
 * AppSettings — matches legacy localStorage settings schema (v3).
 *
 * This data class represents all app settings that were previously stored
 * in localStorage via the legacy SettingsStorage abstraction.
 */
data class AppSettings(
    // Text size scaling
    val textSize: String = "default", // "small", "default", "large", "extra-large"

    // Theme mode
    val themeMode: String = "system", // "light", "dark", "system"

    // Onboarding state
    val onboarded: Boolean = false,
    val onboardingStage: String = "fresh", // "fresh", "owner", "relationship", "personalization", "complete"

    // App lock
    val appLockEnabled: Boolean = false,
    val lockTimeoutSeconds: Int = 60,

    // Timestamps
    val firstLaunchAt: String? = null,

    // Notifications
    val notificationsEnabled: Boolean = true,
    val remindersEnabled: Boolean = true,

    // Accessibility
    val reduceMotion: Boolean = false
)

/**
 * Companion object with key constants for DataStore.
 */
object AppSettingsKeys {
    const val TEXT_SIZE = "text_size"
    const val THEME_MODE = "theme_mode"
    const val ONBOARDED = "onboarded"
    const val ONBOARDING_STAGE = "onboarding_stage"
    const val APP_LOCK_ENABLED = "app_lock_enabled"
    const val LOCK_TIMEOUT_SECONDS = "lock_timeout_seconds"
    const val FIRST_LAUNCH_AT = "first_launch_at"
    const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val REMINDERS_ENABLED = "reminders_enabled"
    const val REDUCE_MOTION = "reduce_motion"
}
