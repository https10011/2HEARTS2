package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ProfileAvatar
import com.twohearts.app.ui.components.ThDatePicker
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import com.twohearts.app.services.validation.Validator

/**
 * ProfileSetupScreen — who is using TwoHearts.
 *
 * ## What the migrated version was
 *
 * Two fields: a required "Your Name" and an optional "Birthday (optional)"
 * whose placeholder was the literal string `yyyy-mm-dd`. Validation ran only
 * on tap of Continue, and its message came from the generic validator
 * ("name must be at least 1 characters").
 *
 * ## What changed
 *
 *  - **The date is no longer typed.** The birthday uses the app's branded
 *    [ThDatePicker], so there is no machine format to learn and no way to
 *    express a date that does not exist. This was the single largest piece
 *    of friction in first launch.
 *  - **There is a live preview.** The avatar beside the name field updates as
 *    the person types, so the step shows what it is building rather than
 *    asking for input blind.
 *  - **Validation is specific and recoverable.** The message names the actual
 *    problem in the product's voice ("Please add a name so TwoHearts knows
 *    what to call you.") and clears the moment the field is edited.
 *  - **Copy is relationship-neutral.** No gendered term appears anywhere; the
 *    other person is referred to only as "your special someone".
 *
 * The step is deliberately short. The directive asks for a flow that is
 * comfortable to complete, and a name plus an optional birthday is genuinely
 * all this screen needs.
 */
@Composable
fun ProfileSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit,
) {
    var name by remember { mutableStateOf(data.ownerName) }
    var birthday by remember { mutableStateOf(data.ownerBirthday ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    OnboardingLayout(
        stage = OnboardingStage.OWNER,
        eyebrow = "About you",
        title = "What should we call you?",
        subtitle = "This is how TwoHearts greets you. Only your name is needed — " +
            "the rest is yours to add whenever you like.",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                nameError = "Please add a name so TwoHearts knows what to call you."
                return@OnboardingLayout
            }
            val lengthCheck = Validator.length(trimmed, "name", min = 1, max = 50)
            if (!lengthCheck.ok) {
                nameError = "That name is a little long — 50 characters is the most we can keep."
                return@OnboardingLayout
            }
            onNext(
                data.copy(
                    ownerName = trimmed,
                    ownerBirthday = birthday.takeIf { it.isNotBlank() },
                )
            )
        },
        primaryEnabled = name.isNotBlank(),
    ) {
        // Live preview: the avatar reflects the typed name immediately, so
        // the step visibly builds something instead of just collecting input.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                name = name,
                photoUrl = null,
                size = 72,
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space4))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))
                Text(
                    text = if (name.isBlank()) {
                        "Your first name is enough."
                    } else {
                        "Nice to meet you, ${name.trim()}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalTwoHeartsColors.current.textSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

        ThInput(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            label = "Your name",
            placeholder = "First name or nickname",
            error = nameError,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))

        ThDatePicker(
            value = birthday.takeIf { it.isNotBlank() },
            onValueChange = { birthday = it },
            label = "Your birthday (optional)",
            placeholder = "Add your birthday",
            support = "We'll remind you both when the day comes around.",
            clearable = true,
            maxYear = currentYearForPicker(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * The latest selectable birth year.
 *
 * A birthday cannot be in the future, so the picker is capped at the current
 * year. Kept here rather than in the shared picker because "today" is a
 * screen-level policy, not a property of a date control.
 */
private fun currentYearForPicker(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

/**
 * ThFieldGroup — vertical stack of related fields with consistent rhythm.
 *
 * Extracted so the setup steps stop each picking their own inter-field
 * spacing, which is what made the migrated forms look assembled rather than
 * designed.
 */
@Composable
fun ThFieldGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space5),
    ) {
        content()
    }
}

/**
 * OnboardingHint — a quiet supporting line under a field group.
 *
 * Uses the tertiary text tone rather than an informational banner: hints
 * here are reassurance, not alerts, and the directive asks that emotional
 * language not be forced onto every element.
 */
@Composable
fun OnboardingHint(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TwoHeartsTokens.Spacing.space3),
        style = MaterialTheme.typography.bodySmall,
        color = LocalTwoHeartsColors.current.textTertiary,
    )
}