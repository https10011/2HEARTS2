package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * ReminderDetail — reminder detail screen.
 *
 * Matches legacy ReminderDetail with:
 * - Full reminder display
 * - Moment card with delete
 * - Edit/delete actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetail(
    reminder: Reminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Header(
                title = "Reminder",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Status badge
            Surface(
                color = if (reminder.status == "pending") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = reminder.status.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (reminder.status == "pending") {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (reminder.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date and time
            Text(
                text = "Scheduled",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = DateTimeHelper.formatDisplay(reminder.scheduledDate),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (reminder.scheduledTime != null) {
                Text(
                    text = "at ${reminder.scheduledTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            if (reminder.recurrence != "none") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Repeats: ${reminder.recurrence.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification status
            Text(
                text = if (reminder.notificationEnabled) "Notifications enabled" else "Notifications disabled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timestamps
            Text(
                text = "Created: ${DateTimeHelper.formatDisplay(reminder.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            if (reminder.updatedAt != reminder.createdAt) {
                Text(
                    text = "Updated: ${DateTimeHelper.formatDisplay(reminder.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Reminder",
            message = "Are you sure you want to delete this reminder?",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
