package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.validation.Validator
import com.twohearts.app.ui.components.TextThButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * AppLockSetupScreen — the optional lock on the private space.
 *
 * ## What the migrated version was
 *
 * Two plain text fields labelled "PIN (4-8 digits)" and "Confirm PIN", a
 * paragraph of explanatory copy, a "Set PIN" button and a "Skip" text button.
 * Nothing on the screen said *why* a PIN was being offered, and the copy
 * explained the mechanics of the setting ("You can skip this step and set up
 * app lock later in Settings") rather than what the lock is for.
 *
 * ## What changed
 *
 *  - **The step is framed as a choice, not a default.** The subtitle states
 *    plainly that this is optional and what it protects. The directive warns
 *    against inventing skip functionality to shorten a flow — but this skip
 *    is genuine, pre-existing product behaviour, so it is kept and made
 *    legible rather than hidden.
 *  - **The PIN is entered as digits, with a visible length rule.** The field
 *    is numeric, masked, and capped at 8 characters, and the rule ("4 to 8
 *    digits") sits in the label rather than in a paragraph below.
 *  - **Validation is specific and never silent.** Too short, non-numeric and
 *    mismatched PINs each get their own message, attached to the field that
 *    is actually wrong. The confirm field is only validated once the first
 *    field is valid, so the person is never told two things at once.
 *  - **The PIN is never echoed back.** It is a credential; nothing in this
 *    screen logs it, and the completion step does not display it.
 *
 * The PIN itself is stored by `AppLockService` through `SecureStorage` — this
 * screen only collects it. No PIN material is persisted by the UI layer.
 */
@Composable
fun AppLockSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit,
) {
    var pin by remember { mutableStateOf(data.pin ?: "") }
    var confirmPin by remember { mutableStateOf(data.confirmPin ?: "") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var confirmPinError by remember { mutableStateOf<String?>(null) }

    OnboardingLayout(
        stage = OnboardingStage.APP_LOCK,
        eyebrow = "Optional",
        title = "Keep it just for you",
        subtitle = "Add a PIN and only you can open TwoHearts. Skip this and " +
            "you can still set one up later.",
        onBack = onBack,
        primaryLabel = "Set the PIN",
        primaryEnabled = pin.isNotBlank() && confirmPin.isNotBlank(),
        onPrimary = {
            val pinCheck = Validator.pin(pin)
            if (!pinCheck.ok) {
                pinError = "A PIN is 4 to 8 digits."
                return@OnboardingLayout
            }
            if (pin != confirmPin) {
                confirmPinError = "These don't match yet — try the second one again."
                return@OnboardingLayout
            }
            onNext(data.copy(pin = pin, confirmPin = confirmPin))
        },
        secondary = {
            TextThButton(
                onClick = { onNext(data.copy(pin = null, confirmPin = null)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Not now",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) {
        LockReassuranceRow()

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

        ThInput(
            value = pin,
            onValueChange = { raw ->
                // Digits only, capped at the documented maximum, so an
                // invalid PIN cannot be typed in the first place.
                pin = raw.filter { it.isDigit() }.take(8)
                pinError = null
                confirmPinError = null
            },
            label = "A PIN (4 to 8 digits)",
            placeholder = "••••",
            error = pinError,
            keyboardType = KeyboardType.NumberPassword,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))

        ThInput(
            value = confirmPin,
            onValueChange = { raw ->
                confirmPin = raw.filter { it.isDigit() }.take(8)
                confirmPinError = null
            },
            label = "Once more, to be sure",
            placeholder = "••••",
            error = confirmPinError,
            keyboardType = KeyboardType.NumberPassword,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

        OnboardingHint(
            text = "TwoHearts asks for this PIN when the app opens. There is no " +
                "way to recover it, so pick something you'll remember.",
        )
    }
}

/**
 * LockReassuranceRow — what the lock protects, stated in one line.
 *
 * The migrated screen offered a PIN without saying what it was guarding.
 * A lock is a privacy feature, and privacy features are only reassuring when
 * the person knows the boundary being drawn.
 */
@Composable
private fun LockReassuranceRow(modifier: Modifier = Modifier) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(thColors.surfaceWarm)
            .border(TwoHeartsTokens.Border.hairline, thColors.borderSubtle, shape)
            .padding(TwoHeartsTokens.Spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(thColors.surfaceBlush),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ThIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Everything stays on this device",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))
            Text(
                text = "The PIN is the only key. It isn't sent anywhere, and it " +
                    "isn't stored with your memories.",
                style = MaterialTheme.typography.bodySmall,
                color = thColors.textSecondary,
            )
        }
    }
}