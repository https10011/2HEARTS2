package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.services.validation.Validator

/**
 * AppLockSetupScreen — optional PIN setup screen.
 *
 * Matches legacy AppLockSetupScreen with:
 * - PIN input (4-8 digits)
 * - Confirm PIN input
 * - Skip option
 * - Validation
 */
@Composable
fun AppLockSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit
) {
    var pin by remember { mutableStateOf(data.pin ?: "") }
    var confirmPin by remember { mutableStateOf(data.confirmPin ?: "") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var confirmPinError by remember { mutableStateOf<String?>(null) }

    OnboardingLayout(
        currentStage = OnboardingStage.APP_LOCK,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "App Lock",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Protect your private content with a PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PIN input
            ThInput(
                value = pin,
                onValueChange = {
                    pin = it
                    pinError = null
                },
                label = "PIN (4-8 digits)",
                placeholder = "Enter PIN",
                error = pinError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm PIN input
            ThInput(
                value = confirmPin,
                onValueChange = {
                    confirmPin = it
                    confirmPinError = null
                },
                label = "Confirm PIN",
                placeholder = "Confirm PIN",
                error = confirmPinError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = "You can skip this step and set up app lock later in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Continue button (with PIN)
            ThButton(
                onClick = {
                    // Validate PIN
                    val pinValidation = Validator.pin(pin)
                    if (!pinValidation.ok) {
                        pinError = pinValidation.errors.firstOrNull()?.message
                        return@ThButton
                    }

                    // Validate confirm PIN
                    if (pin != confirmPin) {
                        confirmPinError = "PINs do not match"
                        return@ThButton
                    }

                    onNext(
                        data.copy(
                            pin = pin,
                            confirmPin = confirmPin
                        )
                    )
                },
                text = "Set PIN",
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skip button
            TextThButton(
                onClick = {
                    onNext(data.copy(pin = null, confirmPin = null))
                }
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
