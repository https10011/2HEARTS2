package com.twohearts.app.ui.onboarding

/**
 * OnboardingState — onboarding flow state management.
 *
 * Matches legacy onboarding stages exactly:
 * - fresh: First launch, show welcome
 * - owner: Profile setup (owner name + birthday)
 * - relationship: Relationship setup (partner name + start date)
 * - personalization: Theme + text size selection
 * - app-lock: Optional PIN setup
 * - complete: Setup complete, navigate to app
 */
enum class OnboardingStage(val order: Int) {
    FRESH(0),
    OWNER(1),
    RELATIONSHIP(2),
    PERSONALIZATION(3),
    APP_LOCK(4),
    COMPLETE(5);

    /**
     * Get the next stage in the flow.
     */
    fun next(): OnboardingStage? {
        return entries.find { it.order == this.order + 1 }
    }

    /**
     * Get the previous stage in the flow.
     */
    fun previous(): OnboardingStage? {
        return entries.find { it.order == this.order - 1 }
    }

    /**
     * Check if this is the first stage.
     */
    fun isFirst(): Boolean = this == FRESH

    /**
     * Check if this is the last stage.
     */
    fun isLast(): Boolean = this == COMPLETE

    companion object {
        /**
         * The order value of the last step that collects input.
         *
         * The welcome screen (`FRESH`) is the front door and the completion
         * screen (`COMPLETE`) is the exit, so neither is a numbered "step" —
         * the progress indicator counts only the four setup steps between
         * them. Deriving this from the enum means adding a stage cannot
         * silently desync the indicator's denominator.
         */
        val LAST_SETUP_ORDER: Int = COMPLETE.order - 1

        /** Number of numbered setup steps, used by the progress indicator. */
        val SETUP_STEP_COUNT: Int = LAST_SETUP_ORDER

        /**
         * Resolves the persisted stage string.
         *
         * The storage keys are the legacy strings that `SettingsStorage`
         * already writes ("app-lock", not "APP_LOCK"), so a person who was
         * mid-setup before this phase keeps their position. The mapping used
         * to be duplicated as two matching `when` blocks in the flow — one
         * forward, one backward — which is exactly how the two drift.
         * Unrecognised values, including a cleared store, fall back to FRESH.
         */
        fun fromStorageKey(key: String?): OnboardingStage = entries.firstOrNull {
            it.storageKey == key
        } ?: FRESH
    }

    /** The string persisted in settings for this stage. */
    val storageKey: String
        get() = when (this) {
            FRESH -> "fresh"
            OWNER -> "owner"
            RELATIONSHIP -> "relationship"
            PERSONALIZATION -> "personalization"
            APP_LOCK -> "app-lock"
            COMPLETE -> "complete"
        }
}

/**
 * Decides whether the shell should replace onboarding.
 *
 * Completed setup is persisted the instant the domain commit returns, so a
 * crash cannot undo it. But the completion screen is the moment the person is
 * told what was created, and it must still be shown *after* that write — if
 * the gate swapped in the shell the moment `stage == "complete"`, the
 * completion screen would be unreachable in the real app.
 *
 * [acknowledged] therefore records that the person has actually seen the
 * completion screen and tapped through it. It is deliberately in-memory: a
 * cold start of an already-complete install must land in the app, never on a
 * second celebration, so the caller seeds it from the persisted state and
 * only clears it when setup is genuinely incomplete again.
 *
 * Extracted as a pure function so the rule can be tested without a composed
 * tree.
 */
fun shouldShowApp(
    onboarded: Boolean,
    stageKey: String?,
    acknowledged: Boolean,
): Boolean = onboarded &&
    OnboardingStage.fromStorageKey(stageKey) == OnboardingStage.COMPLETE &&
    acknowledged

/**
 * OnboardingData — data collected during onboarding.
 */
data class OnboardingData(
    // Owner profile
    val ownerName: String = "",
    val ownerBirthday: String? = null,

    // Partner profile
    val partnerName: String = "",
    val partnerBirthday: String? = null,

    // Relationship
    val startDate: String = "",

    // Personalization
    val themeMode: String = "system",
    val textSize: String = "default",

    // App lock
    val pin: String? = null,
    val confirmPin: String? = null
) {
    /**
     * Check if owner profile is valid.
     */
    fun isOwnerValid(): Boolean {
        return ownerName.isNotBlank() && ownerName.length in 1..50
    }

    /**
     * Check if relationship data is valid.
     */
    fun isRelationshipValid(): Boolean {
        return partnerName.isNotBlank() &&
                partnerName.length in 1..50 &&
                startDate.isNotBlank()
    }

    /**
     * Check if PIN is valid.
     */
    fun isPinValid(): Boolean {
        return pin != null && pin.length in 4..8 && pin == confirmPin
    }
}
