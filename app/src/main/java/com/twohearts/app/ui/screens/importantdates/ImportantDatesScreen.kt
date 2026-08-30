package com.twohearts.app.ui.screens.importantdates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * ImportantDatesScreen — important dates list screen.
 *
 * matches legacy ImportantDatesScreen with:
 * - List of important dates
 * - Recurring dates
 * - Empty state
 * - Add date FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantDatesScreen(
    dates: List<ImportantDate>,
    onAddDate: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Header(
                title = "Important Dates",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Date"
                )
            }
        }
    ) { paddingValues ->
        if (dates.isEmpty()) {
            EmptyState(
                title = "No Important Dates",
                message = "Add anniversaries, birthdays, and more",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dates) { date ->
                    ImportantDateCard(date = date)
                }
            }
        }
    }
}

/**
 * ImportantDateCard — important date card.
 */
@Composable
fun ImportantDateCard(
    date: ImportantDate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date icon
            Text(
                text = "📅",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Date info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateTimeHelper.formatDisplay(date.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (date.recurrence != "none") {
                    Text(
                        text = "Repeats: ${date.recurrence.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
