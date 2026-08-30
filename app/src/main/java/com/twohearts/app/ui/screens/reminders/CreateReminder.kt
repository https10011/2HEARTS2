package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * CreateReminder — reminder creation screen.
 *
 * Matches legacy CreateReminder with:
 * - Title input (required)
 * - Description input (optional)
 * - Date picker (required)
 * - Time picker (optional)
 * - Recurrence selector
 * - Notification toggle
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminder(
    reminderId: String? = null,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: String = DateTimeHelper.todayLocal(),
    initialTime: String = "",
    initialRecurrence: String = "none",
    onSave: (title: String, description: String?, scheduledDate: String, scheduledTime: String?, recurrence: String) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var scheduledDate by remember { mutableStateOf(initialDate) }
    var scheduledTime by remember { mutableStateOf(initialTime) }
    var recurrence by remember { mutableStateOf(initialRecurrence) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }

    val isEditing = reminderId != null
    val recurrenceOptions = listOf("none", "daily", "weekly", "monthly", "yearly")

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Reminder" else "New Reminder",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    description.takeIf { it.isNotBlank() }?.trim(),
                                    scheduledDate,
                                    scheduledTime.takeIf { it.isNotBlank() },
                                    recurrence
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input
            Input(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                placeholder = "Enter reminder title",
                modifier = Modifier.fillMaxWidth()
            )

            // Description input
            Input(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                placeholder = "Add a description...",
                modifier = Modifier.fillMaxWidth()
            )

            // Date input
            Input(
                value = scheduledDate,
                onValueChange = { scheduledDate = it },
                label = "Date",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            // Time input
            Input(
                value = scheduledTime,
                onValueChange = { scheduledTime = it },
                label = "Time (optional)",
                placeholder = "HH:mm",
                modifier = Modifier.fillMaxWidth()
            )

            // Recurrence selector
            ExposedDropdownMenuBox(
                expanded = showRecurrenceMenu,
                onExpandedChange = { showRecurrenceMenu = it }
            ) {
                Input(
                    value = recurrence.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    label = "Recurrence",
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showRecurrenceMenu,
                    onDismissRequest = { showRecurrenceMenu = false }
                ) {
                    recurrenceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                recurrence = option
                                showRecurrenceMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}
