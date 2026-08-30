package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * WelcomeScreen — first-launch welcome screen.
 *
 * Matches legacy WelcomeScreen with:
 * - Brand logo
 * - Welcome message
 * - "Get Started" button
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    OnboardingLayout(
        currentStage = OnboardingStage.FRESH,
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
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome title
            Text(
                text = "Welcome to TwoHearts",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Welcome message
            Text(
                text = "A private space for you and your partner to share memories, notes, and moments together.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Get Started button
            ThButton(
                onClick = onGetStarted,
                text = "Get Started",
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
