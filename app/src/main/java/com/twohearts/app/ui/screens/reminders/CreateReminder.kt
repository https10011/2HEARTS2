package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate
import java.time.LocalTime

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
    initialNotificationEnabled: Boolean = true,
    onSave: (title: String, description: String?, scheduledDate: String, scheduledTime: String?, recurrence: String, notificationEnabled: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var scheduledDate by remember { mutableStateOf(initialDate) }
    var scheduledTime by remember { mutableStateOf(initialTime) }
    var recurrence by remember { mutableStateOf(initialRecurrence) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }
    var notificationEnabled by remember { mutableStateOf(initialNotificationEnabled) }
    var attempted by remember { mutableStateOf(false) }
    val validDate = runCatching { LocalDate.parse(scheduledDate) }.isSuccess
    val validTime = scheduledTime.isBlank() || runCatching { LocalTime.parse(scheduledTime) }.isSuccess

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
                            attempted = true
                            if (title.isNotBlank() && validDate && validTime) {
                                onSave(
                                    title.trim(),
                                    description.takeIf { it.isNotBlank() }?.trim(),
                                    scheduledDate,
                                    scheduledTime.takeIf { it.isNotBlank() },
                                    recurrence,
                                    notificationEnabled,
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = ThIcons.Check,
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val colors = LocalTwoHeartsColors.current
            Text("A gentle reminder", style = MaterialTheme.typography.titleLarge)
            Text("Keep something meaningful from slipping through the day.", color = colors.textSecondary)
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
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Notify me"); Text("TwoHearts will remind you on this device.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }; Switch(notificationEnabled, { notificationEnabled = it }) }
            if (attempted && title.isBlank()) Text("Give this reminder a title.", color = MaterialTheme.colorScheme.error)
            if (attempted && !validDate) Text("Use a valid date in yyyy-mm-dd format.", color = MaterialTheme.colorScheme.error)
            if (attempted && !validTime) Text("Use a 24-hour time such as 18:30.", color = MaterialTheme.colorScheme.error)
            Button(onClick = { attempted = true; if (title.isNotBlank() && validDate && validTime) onSave(title.trim(), description.trim().ifEmpty { null }, scheduledDate, scheduledTime.trim().ifEmpty { null }, recurrence, notificationEnabled) }, modifier = Modifier.fillMaxWidth()) { Icon(ThIcons.Check, null); Spacer(Modifier.width(8.dp)); Text(if (isEditing) "Save changes" else "Save reminder") }
        }
    }
}
