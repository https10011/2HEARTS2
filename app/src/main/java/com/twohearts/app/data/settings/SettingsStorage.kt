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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * The one DataStore for settings, scoped to the process rather than to a
 * [SettingsStorage] instance. DataStore enforces one active instance per file,
 * so this must not be a member property (see the class docs).
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "twohearts_settings"
)

/**
 * SettingsStorage — DataStore-based implementation matching legacy localStorage settings.
 *
 * This replaces the legacy SettingsStorage abstraction that used localStorage.
 * All settings are persisted using Jetpack DataStore (Preferences).
 *
 * ## Why the DataStore is a top-level delegate and the instance is shared
 *
 * DataStore allows exactly **one active instance per file per process**; a
 * second one fails with *"There are multiple DataStores active for the same
 * file"*. Two things about the original wiring made that reachable on a real
 * device:
 *
 *  1. The delegate was a *member* of this class, so every `SettingsStorage(...)`
 *     created another `DataStore` over the same file. A `by
 *     preferencesDataStore(...)` delegate is safe as a top-level (or Activity)
 *     property precisely because it is created once per process — as a member
 *     it is created once per instance.
 *  2. There were two instances: one in `MainActivity` and a second inside
 *     `BootstrapService`. `MainActivity` subscribes to `settings` for the
 *     theme, so the first DataStore was already active by the time bootstrap
 *     reached the app-lock stage and built the second.
 *
 * Moving the delegate to the top of this file plus [getInstance] makes the
 * one-instance rule structural rather than something every call site has to
 * remember. See `SettingsStorageSingletonTest`.
 */
class SettingsStorage private constructor(private val context: Context) {

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
     * Persists the in-progress onboarding draft.
     *
     * ## Why this exists (Phase 3)
     *
     * The migrated flow kept the collected names, birthday and start date in
     * transient Compose state and persisted only the *stage*. A process death
     * mid-setup therefore returned the person to the correct step with an
     * empty form — the stage survived, the input did not.
     *
     * The draft is stored here, beside the stage, rather than in the domain
     * database on purpose: the profiles and couple row are created in one
     * commit at the end of setup, so there must not be a half-written couple
     * record to reconcile if someone abandons setup. DataStore is the correct
     * home for "intent that has not become domain state yet".
     *
     * The draft is one JSON object under a single key, so a partial write
     * cannot produce a mixed old/new set of fields.
     */
    suspend fun saveOnboardingDraft(draft: OnboardingDraft) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_DRAFT] = draft.toJson()
        }
    }

    /**
     * Reads the in-progress draft, or null when setup has not started or the
     * stored value is unreadable.
     *
     * A malformed value resolves to null rather than throwing: losing draft
     * input is recoverable (the person re-enters a name), whereas crashing on
     * first launch is not.
     */
    suspend fun loadOnboardingDraft(): OnboardingDraft? {
        val raw = context.dataStore.data.map { it[ONBOARDING_DRAFT] }.first() ?: return null
        return OnboardingDraft.fromJson(raw)
    }

    /**
     * Removes the draft once setup has been committed to the domain.
     *
     * The draft holds personalisation only — names and dates. The PIN is
     * never stored here (see [OnboardingDraft]), so clearing is about not
     * leaving a half-finished setup behind, not about scrubbing a credential.
     */
    suspend fun clearOnboardingDraft() {
        context.dataStore.edit { preferences ->
            preferences.remove(ONBOARDING_DRAFT)
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
        @Volatile
        private var instance: SettingsStorage? = null

        /**
         * The process-wide [SettingsStorage].
         *
         * Every caller must go through here: a second instance would create a
         * second [DataStore] over the same file and fail at runtime with
         * *"There are multiple DataStores active for the same file"*, which is
         * what crashed the app on launch. The constructor is private so that
         * cannot be done accidentally.
         *
         * This is intentionally the same double-checked-singleton shape used by
         * [com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase].
         */
        fun getInstance(context: Context): SettingsStorage {
            return instance ?: synchronized(this) {
                instance ?: SettingsStorage(context.applicationContext).also { instance = it }
            }
        }

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
        private val ONBOARDING_DRAFT = stringPreferencesKey(AppSettingsKeys.ONBOARDING_DRAFT)
    }
}
