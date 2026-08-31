package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.ui.components.Button
import com.twohearts.app.ui.theme.Burgundy

/**
 * PeriodSettingsScreen — cycle/period length configuration.
 *
 * Matches legacy PeriodSettingsScreen with:
 * - Cycle length setting (default 28 days)
 * - Period length setting (default 5 days)
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSettingsScreen(
    onBack: () -> Unit
) {
    var cycleLength by remember { mutableStateOf("28") }
    var periodLength by remember { mutableStateOf("5") }

    Scaffold(
        topBar = {
            Header(
                title = "Period Settings",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cycle Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Configure your typical cycle and period lengths. This helps predict future periods.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Cycle length
            Input(
                value = cycleLength,
                onValueChange = { cycleLength = it },
                label = "Cycle Length (days)",
                placeholder = "28",
                modifier = Modifier.fillMaxWidth()
            )

            // Period length
            Input(
                value = periodLength,
                onValueChange = { periodLength = it },
                label = "Period Length (days)",
                placeholder = "5",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    // TODO: Save settings via PeriodSettingsRepository
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                text = "Save Settings"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How predictions work",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "TwoHearts uses your cycle length to predict when your next period might start. The more data you log, the more accurate predictions become.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
