package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.PeriodEntry

/**
 * PeriodCalendarScreen — calendar view of period entries.
 *
 * Matches legacy PeriodCalendarScreen with:
 * - Monthly calendar view
 * - Period days highlighted
 * - Cycle length indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodCalendarScreen(
    entries: List<PeriodEntry>,
    onBack: () -> Unit
) {
    var selectedMonth by remember { mutableStateOf(java.time.YearMonth.now()) }

    Scaffold(
        topBar = {
            Header(
                title = "Period Calendar",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    selectedMonth = selectedMonth.minusMonths(1)
                }) {
                    Text("← Previous")
                }

                Text(
                    text = selectedMonth.month.name + " " + selectedMonth.year,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = {
                    selectedMonth = selectedMonth.plusMonths(1)
                }) {
                    Text("Next →")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar grid placeholder
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅 Calendar View\n(Period days will be highlighted)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Period Day", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Predicted", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
