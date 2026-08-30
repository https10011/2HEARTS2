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
}

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
