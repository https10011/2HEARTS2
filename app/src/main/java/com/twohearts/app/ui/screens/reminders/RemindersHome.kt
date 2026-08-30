package com.twohearts.app.ui.screens.reminders

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
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * RemindersHome — reminders list screen.
 *
 * Matches legacy RemindersHome with:
 * - Filtered list with "next up" hero
 * - Pending/completed tabs
 * - Empty state
 * - Add reminder FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersHome(
    reminders: List<Reminder>,
    onReminderClick: (String) -> Unit,
    onAddReminder: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Completed")

    val filteredReminders = remember(reminders, selectedTab) {
        when (selectedTab) {
            0 -> reminders.filter { it.status == "pending" }
            1 -> reminders.filter { it.status == "completed" }
            else -> reminders
        }
    }

    Scaffold(
        topBar = {
            Header(
                title = "Reminders",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (filteredReminders.isEmpty()) {
                EmptyState(
                    title = "No Reminders",
                    message = if (selectedTab == 0) "All caught up!" else "No completed reminders yet"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onClick = { onReminderClick(reminder.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * ReminderCard — reminder card for list display.
 */
@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (reminder.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date and time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeHelper.formatShortDisplay(reminder.scheduledDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (reminder.scheduledTime != null) {
                    Text(
                        text = " at ${reminder.scheduledTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if (reminder.recurrence != "none") {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = reminder.recurrence.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
