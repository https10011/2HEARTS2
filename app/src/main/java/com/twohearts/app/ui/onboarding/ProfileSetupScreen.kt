package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.services.validation.Validator

/**
 * ProfileSetupScreen — owner profile setup screen.
 *
 * Matches legacy ProfileSetupScreen with:
 * - Name input (required, 1-50 chars)
 * - Birthday input (optional)
 * - Validation
 */
@Composable
fun ProfileSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit
) {
    var name by remember { mutableStateOf(data.ownerName) }
    var birthday by remember { mutableStateOf(data.ownerBirthday ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    OnboardingLayout(
        currentStage = OnboardingStage.OWNER,
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
                text = "About You",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us a bit about yourself",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name input
            ThInput(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = "Your Name",
                placeholder = "Enter your name",
                error = nameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Birthday input
            ThInput(
                value = birthday,
                onValueChange = { birthday = it },
                label = "Birthday (optional)",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Next button
            ThButton(
                onClick = {
                    // Validate
                    val nameValidation = Validator.length(name, "name", min = 1, max = 50)
                    if (!nameValidation.ok) {
                        nameError = nameValidation.errors.firstOrNull()?.message
                        return@Button
                    }

                    onNext(
                        data.copy(
                            ownerName = name.trim(),
                            ownerBirthday = birthday.takeIf { it.isNotBlank() }
                        )
                    )
                },
                text = "Continue",
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
