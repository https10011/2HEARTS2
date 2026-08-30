package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.Button

/**
 * SetupCompleteScreen — celebration and navigation to app.
 *
 * Matches legacy SetupCompleteScreen with:
 * - Celebration message
 * - Brand logo
 * - "Start Using TwoHearts" button
 */
@Composable
fun SetupCompleteScreen(
    data: OnboardingData,
    onComplete: () -> Unit
) {
    OnboardingLayout(
        currentStage = OnboardingStage.COMPLETE,
        onBack = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand logo
            BrandLogo(
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Celebration title
            Text(
                text = "You're All Set!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Celebration message
            Text(
                text = "Welcome to TwoHearts, ${data.ownerName}! Start sharing moments with ${data.partnerName}.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Start button
            Button(
                onClick = onComplete,
                text = "Start Using TwoHearts",
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
