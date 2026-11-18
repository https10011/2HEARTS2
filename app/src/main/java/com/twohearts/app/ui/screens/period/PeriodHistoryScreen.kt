package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * PeriodHistoryScreen — period history list.
 *
 * Matches legacy PeriodHistoryScreen with:
 * - Past cycles list
 * - Duration calculations
 * - Empty state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodHistoryScreen(
    entries: List<PeriodEntry>,
    onBack: () -> Unit,
    onEdit: ((PeriodEntry) -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            Header(
                title = "Period History",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
            EmptyState(
                title = "No Period Entries",
                message = "Start logging your period to see your history here.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    PeriodHistoryItem(entry = entry, onEdit = onEdit?.let { { it(entry) } })
                }
            }
        }
    }
}

@Composable
private fun PeriodHistoryItem(entry: PeriodEntry, onEdit: (() -> Unit)?) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = DateTimeHelper.formatDisplay(entry.startDate),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (entry.endDate != null) {
                Text(
                    text = "Ended ${DateTimeHelper.formatDisplay(entry.endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                Text("End date missing", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Flow: ${entry.flowLevel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (entry.note != null && entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            if (onEdit != null) TextButton(onClick = onEdit) { Text("Edit entry") }
        }
    }
}
