package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.components.ThDatePicker
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.theme.TwoHeartsTokens
import com.twohearts.app.services.validation.Validator

/**
 * RelationshipSetupScreen — who the space is shared with, and since when.
 *
 * ## What the migrated version was
 *
 * The field was labelled **"Partner's Name"** with the placeholder *"Enter
 * partner's name"*, and the date field was a text input with the placeholder
 * `yyyy-mm-dd`.
 *
 * Both of those are direct conflicts with the authority the phase runs on.
 * The Master Directive §46 is explicit: use the product's relationship-neutral
 * terminology — *You*, *Your Special Someone* — and do not hardcode partner
 * framing into the product UI. "Partner" is corporate-adjacent and quietly
 * prescriptive about the shape of the relationship; the whole point of the
 * app's language is that it does not assume one.
 *
 * ## What changed
 *
 *  - **"Your Special Someone"** replaces "Partner's Name" everywhere on this
 *    step, including the placeholder and the validation copy. This is the
 *    single most visible copy correction of the phase.
 *  - **The start date is a picker, not a text field.** The `yyyy-mm-dd`
 *    placeholder and its associated validator message ("start date must be in
 *    yyyy-mm-dd format") are gone; the app now shows the date in the form a
 *    person would say it aloud.
 *  - **The step explains why it asks.** A quiet line under the date says what
 *    the date is *for* (the day counter and anniversary), which turns an
 *    arbitrary required field into a reason.
 *
 * The start date remains required: it is the couple record's only anchor and
 * the anniversary reminder worker depends on it. The hint makes that
 * consequence visible rather than leaving the field looking optional.
 */
@Composable
fun RelationshipSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit,
) {
    var partnerName by remember { mutableStateOf(data.partnerName) }
    var startDate by remember { mutableStateOf(data.startDate) }
    var partnerNameError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }

    OnboardingLayout(
        stage = OnboardingStage.RELATIONSHIP,
        eyebrow = "The two of you",
        title = "Who are you sharing this with?",
        subtitle = "TwoHearts is a space for two. Add their name and the day " +
            "you count from.",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = {
            val trimmed = partnerName.trim()
            if (trimmed.isEmpty()) {
                partnerNameError = "Add a name (or a nickname) for your special someone."
                return@OnboardingLayout
            }
            if (!Validator.length(trimmed, "name", min = 1, max = 50).ok) {
                partnerNameError = "That name is a little long — 50 characters is the most we can keep."
                return@OnboardingLayout
            }
            if (startDate.isBlank()) {
                startDateError = "Choose the day you'd like TwoHearts to count from."
                return@OnboardingLayout
            }
            onNext(
                data.copy(
                    partnerName = trimmed,
                    startDate = startDate,
                )
            )
        },
        primaryEnabled = partnerName.isNotBlank(),
    ) {
        ThInput(
            value = partnerName,
            onValueChange = {
                partnerName = it
                partnerNameError = null
            },
            label = "Your special someone",
            placeholder = "Their name, or what you call them",
            error = partnerNameError,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))

        ThDatePicker(
            value = startDate.takeIf { it.isNotBlank() },
            onValueChange = {
                startDate = it
                startDateError = null
            },
            label = "The day you count from",
            placeholder = "Choose a date",
            support = "This becomes your shared day count and your anniversary reminder.",
            error = startDateError,
            // The relationship cannot have started in the future, and a future
            // start date would make the day counter negative. Capping the
            // picker prevents the state rather than validating against it.
            maxYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
            clearable = false,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

        OnboardingHint(
            text = "Not sure of the exact day? Pick the one that feels closest — " +
                "you can change it later in Settings.",
        )
    }
}