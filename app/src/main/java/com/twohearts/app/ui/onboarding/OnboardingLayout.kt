package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * OnboardingLayout — shared shell for onboarding screens.
 *
 * Matches legacy OnboardingLayout with:
 * - Step indicator (dots)
 * - Back navigation
 * - Content area
 * - Brand logo
 */
@Composable
fun OnboardingLayout(
    currentStage: OnboardingStage,
    onBack: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null && !currentStage.isFirst()) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Brand logo
            BrandLogo(
                modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Spacer for symmetry
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step indicator
        StepIndicator(
            currentStep = currentStage.order,
            totalSteps = OnboardingStage.entries.size - 1 // Exclude FRESH
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * StepIndicator — visual step indicator with dots.
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            Box(
                modifier = Modifier
                    .size(if (step == currentStep) 12.dp else 8.dp)
                    .background(
                        color = if (step <= currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = MaterialTheme.shapes.small
                    )
            )

            if (step < totalSteps) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
