package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * PeriodHome — period tracker home screen.
 *
 * matches legacy PeriodHome with:
 * - Cycle status card
 * - Log entry
 * - Calendar view link
 * - History link
 * - Settings link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodHome(
    latestEntry: PeriodEntry?,
    onLogPeriod: () -> Unit,
    onViewCalendar: () -> Unit,
    onViewHistory: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Header(
                title = "Period Tracker",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onLogPeriod,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log Period"
                )
            }
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
            // Cycle status card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cycle Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (latestEntry != null) {
                        Text(
                            text = "Last period started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DateTimeHelper.formatDisplay(latestEntry.startDate),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (latestEntry.endDate != null) {
                            Text(
                                text = "Duration: ${DateTimeHelper.daysBetween(latestEntry.startDate, latestEntry.endDate)} days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = "No period logged yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Quick actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                onClick = onViewCalendar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "View Calendar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                onClick = onViewHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cycle History",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
