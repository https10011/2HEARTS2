package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ThSectionTabs
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.datetime.scheduledReminderDate
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate

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
    onBack: () -> Unit,
    onToggleCompleted: (Reminder) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Completed")

    val filteredReminders = remember(reminders, selectedTab) {
        when (selectedTab) {
            0 -> reminders.filter { it.status == "pending" }.sortedBy { scheduledReminderDate(it) }
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
            ExtendedFloatingActionButton(
                onClick = onAddReminder,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                text = { Text("Add reminder") },
                icon = { Icon(ThIcons.Add, contentDescription = null) },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ThSectionTabs(labels = tabs, selectedIndex = selectedTab, onSelect = { selectedTab = it })

            if (filteredReminders.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(if (selectedTab == 0) ThIcons.Alarm else ThIcons.CheckCircle, null, tint = LocalTwoHeartsColors.current.burgundy, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(20.dp)); Text(if (selectedTab == 0) "Nothing waiting for you" else "No completed reminders yet", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp)); Text(if (selectedTab == 0) "Add a gentle nudge for a plan, promise, or thoughtful gesture." else "Completed reminders will rest here.", color = LocalTwoHeartsColors.current.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    item { Text(if (selectedTab == 0) "Coming up" else "Held in mind", style = MaterialTheme.typography.headlineSmall); Text(if (selectedTab == 0) "Gentle nudges for the things that matter." else "The things you've already taken care of.", color = LocalTwoHeartsColors.current.textSecondary) }
                    items(filteredReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onClick = { onReminderClick(reminder.id) },
                            onToggleCompleted = { onToggleCompleted(reminder) },
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
    onToggleCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTwoHeartsColors.current
    val displayDate = scheduledReminderDate(reminder)
    val overdue = runCatching { LocalDate.parse(displayDate).isBefore(LocalDate.now()) }.getOrDefault(false) && reminder.status == "pending"
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = reminder.status == "completed", onCheckedChange = { onToggleCompleted() })
                Text(reminder.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }

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
                    text = DateTimeHelper.formatShortDisplay(displayDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overdue) MaterialTheme.colorScheme.error else colors.roseDeep
                )

                if (reminder.scheduledTime != null) {
                    Text(
                        text = " at ${reminder.scheduledTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            if (overdue) Text("Needs attention", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            if (reminder.notificationEnabled) { Spacer(Modifier.height(4.dp)); Text("Notification on", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary) }

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
