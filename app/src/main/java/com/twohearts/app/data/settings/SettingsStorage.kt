package com.twohearts.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * SettingsStorage — DataStore-based implementation matching legacy localStorage settings.
 *
 * This replaces the legacy SettingsStorage abstraction that used localStorage.
 * All settings are persisted using Jetpack DataStore (Preferences).
 */
class SettingsStorage(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "twohearts_settings"
    )

    /**
     * Observe all settings as a Flow.
     */
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            textSize = preferences[TEXT_SIZE] ?: "default",
            themeMode = preferences[THEME_MODE] ?: "system",
            onboarded = preferences[ONBOARDED] ?: false,
            onboardingStage = preferences[ONBOARDING_STAGE] ?: "fresh",
            appLockEnabled = preferences[APP_LOCK_ENABLED] ?: false,
            lockTimeoutSeconds = preferences[LOCK_TIMEOUT_SECONDS] ?: 60,
            firstLaunchAt = preferences[FIRST_LAUNCH_AT],
            notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
            remindersEnabled = preferences[REMINDERS_ENABLED] ?: true,
            reduceMotion = preferences[REDUCE_MOTION] ?: false
        )
    }

    /**
     * Update a single setting.
     */
    suspend fun updateTextSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE] = size
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDED] = onboarded
        }
    }

    suspend fun updateOnboardingStage(stage: String) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_STAGE] = stage
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun updateLockTimeout(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[LOCK_TIMEOUT_SECONDS] = seconds
        }
    }

    suspend fun setFirstLaunchAt(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_AT] = timestamp
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REDUCE_MOTION] = reduce
        }
    }

    /**
     * Reset all settings to defaults.
     */
    suspend fun reset() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val TEXT_SIZE = stringPreferencesKey(AppSettingsKeys.TEXT_SIZE)
        private val THEME_MODE = stringPreferencesKey(AppSettingsKeys.THEME_MODE)
        private val ONBOARDED = booleanPreferencesKey(AppSettingsKeys.ONBOARDED)
        private val ONBOARDING_STAGE = stringPreferencesKey(AppSettingsKeys.ONBOARDING_STAGE)
        private val APP_LOCK_ENABLED = booleanPreferencesKey(AppSettingsKeys.APP_LOCK_ENABLED)
        private val LOCK_TIMEOUT_SECONDS = intPreferencesKey(AppSettingsKeys.LOCK_TIMEOUT_SECONDS)
        private val FIRST_LAUNCH_AT = stringPreferencesKey(AppSettingsKeys.FIRST_LAUNCH_AT)
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey(AppSettingsKeys.NOTIFICATIONS_ENABLED)
        private val REMINDERS_ENABLED = booleanPreferencesKey(AppSettingsKeys.REMINDERS_ENABLED)
        private val REDUCE_MOTION = booleanPreferencesKey(AppSettingsKeys.REDUCE_MOTION)
    }
}
