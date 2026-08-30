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
 * RelationshipSetupScreen — relationship setup screen.
 *
 * Matches legacy RelationshipSetupScreen with:
 * - Partner name input (required, 1-50 chars)
 * - Start date input (required, yyyy-mm-dd)
 * - Validation
 */
@Composable
fun RelationshipSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit
) {
    var partnerName by remember { mutableStateOf(data.partnerName) }
    var startDate by remember { mutableStateOf(data.startDate) }
    var partnerNameError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }

    OnboardingLayout(
        currentStage = OnboardingStage.RELATIONSHIP,
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
                text = "Your Relationship",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us about your partner",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Partner name input
            ThInput(
                value = partnerName,
                onValueChange = {
                    partnerName = it
                    partnerNameError = null
                },
                label = "Partner's Name",
                placeholder = "Enter partner's name",
                error = partnerNameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Start date input
            ThInput(
                value = startDate,
                onValueChange = {
                    startDate = it
                    startDateError = null
                },
                label = "Relationship Start Date",
                placeholder = "yyyy-mm-dd",
                error = startDateError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Next button
            ThButton(
                onClick = {
                    // Validate
                    val partnerNameValidation = Validator.length(partnerName, "partner name", min = 1, max = 50)
                    val startDateValidation = Validator.date(startDate, "start date")

                    if (!partnerNameValidation.ok) {
                        partnerNameError = partnerNameValidation.errors.firstOrNull()?.message
                        return@ThButton
                    }

                    if (!startDateValidation.ok) {
                        startDateError = startDateValidation.errors.firstOrNull()?.message
                        return@ThButton
                    }

                    onNext(
                        data.copy(
                            partnerName = partnerName.trim(),
                            startDate = startDate.trim()
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
