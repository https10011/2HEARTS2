package com.twohearts.app.data.settings

import org.json.JSONObject

/**
 * OnboardingDraft — the setup answers, persisted between steps.
 *
 * ## Why this is separate from [AppSettings]
 *
 * `AppSettings` is the app's *configuration*: preferences the app reads on
 * every launch. The draft is *intent that has not become domain state yet* —
 * a name and a date that will become a profile and a couple record only when
 * setup commits. Keeping them apart means no screen has to ask whether
 * `ownerName` was real or half-typed.
 *
 * ## Why the PIN is deliberately absent
 *
 * The other fields are ordinary personalisation and can live in DataStore.
 * A PIN is a credential, and DataStore Preferences is not encrypted. Persisting
 * it here would put a plaintext lock code on disk for the sake of saving one
 * re-entry if the app dies during the final step — a bad trade in a product
 * whose whole promise is that private things stay private.
 *
 * The PIN therefore exists only in the composition and is handed to
 * `AppLockService` (secure storage) directly at commit. A restart during the
 * lock step simply asks for it again, which is the correct behaviour for a
 * credential.
 */
data class OnboardingDraft(
    val ownerName: String = "",
    val ownerBirthday: String? = null,
    val partnerName: String = "",
    val partnerBirthday: String? = null,
    val startDate: String = "",
    val themeMode: String = "system",
    val textSize: String = "default",
) {
    /** Serialises to a single JSON object — one key, so writes are atomic. */
    fun toJson(): String = JSONObject().apply {
        put(KEY_OWNER_NAME, ownerName)
        put(KEY_OWNER_BIRTHDAY, ownerBirthday ?: JSONObject.NULL)
        put(KEY_PARTNER_NAME, partnerName)
        put(KEY_PARTNER_BIRTHDAY, partnerBirthday ?: JSONObject.NULL)
        put(KEY_START_DATE, startDate)
        put(KEY_THEME_MODE, themeMode)
        put(KEY_TEXT_SIZE, textSize)
    }.toString()

    companion object {
        private const val KEY_OWNER_NAME = "ownerName"
        private const val KEY_OWNER_BIRTHDAY = "ownerBirthday"
        private const val KEY_PARTNER_NAME = "partnerName"
        private const val KEY_PARTNER_BIRTHDAY = "partnerBirthday"
        private const val KEY_START_DATE = "startDate"
        private const val KEY_THEME_MODE = "themeMode"
        private const val KEY_TEXT_SIZE = "textSize"

        /**
         * Reads a stored draft, returning null when the value is absent,
         * malformed, or not an object.
         *
         * Every field read is defensive. A draft is a convenience: losing it
         * costs the person one field re-entry, while throwing here would crash
         * first launch — the one screen that must never fail.
         */
        fun fromJson(raw: String?): OnboardingDraft? {
            if (raw.isNullOrBlank()) return null
            return try {
                val obj = JSONObject(raw)
                OnboardingDraft(
                    ownerName = obj.optString(KEY_OWNER_NAME, ""),
                    ownerBirthday = obj.optNullableString(KEY_OWNER_BIRTHDAY),
                    partnerName = obj.optString(KEY_PARTNER_NAME, ""),
                    partnerBirthday = obj.optNullableString(KEY_PARTNER_BIRTHDAY),
                    startDate = obj.optString(KEY_START_DATE, ""),
                    themeMode = obj.optString(KEY_THEME_MODE, "system"),
                    textSize = obj.optString(KEY_TEXT_SIZE, "default"),
                )
            } catch (_: Exception) {
                null
            }
        }

        /**
         * `optString` returns the literal "null" for a JSON null and "" for a
         * missing key, so both are normalised back to a real Kotlin null.
         */
        private fun JSONObject.optNullableString(key: String): String? {
            if (!has(key) || isNull(key)) return null
            val value = optString(key, "")
            return value.ifBlank { null }
        }
    }
}