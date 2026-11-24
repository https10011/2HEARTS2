package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.RecurrencePicker
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.ThSwitch
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate
import java.time.LocalTime

private val reminderRecurrences = listOf("none", "daily", "weekly", "monthly", "yearly")

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
    onBack: () -> Unit,
    saving: Boolean = false,
    errorMessage: String? = null,
) {
    var title by rememberSaveable(reminderId) { mutableStateOf(initialTitle) }
    var description by rememberSaveable(reminderId) { mutableStateOf(initialDescription) }
    var date by rememberSaveable(reminderId) { mutableStateOf(initialDate) }
    var time by rememberSaveable(reminderId) { mutableStateOf(initialTime) }
    var recurrence by rememberSaveable(reminderId) { mutableStateOf(initialRecurrence) }
    var notify by rememberSaveable(reminderId) { mutableStateOf(initialNotificationEnabled) }
    var attempted by rememberSaveable(reminderId) { mutableStateOf(false) }

    val normalizedDate = date.trim()
    val normalizedTime = time.trim()
    val validDate = runCatching { LocalDate.parse(normalizedDate) }.isSuccess
    val validTime = normalizedTime.isEmpty() || runCatching { LocalTime.parse(normalizedTime) }.isSuccess
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing

    RecordEditorLayout(
        title = if (reminderId == null) "New reminder" else "Edit reminder",
        saveText = "Save reminder",
        onSave = {
            attempted = true
            if (!saving && title.isNotBlank() && validDate && validTime) {
                onSave(title.trim(), description.trim().ifEmpty { null }, normalizedDate, normalizedTime.ifEmpty { null }, recurrence, notify)
            }
        },
        onBack = onBack, saving = saving, errorMessage = errorMessage,
    ) {
        ThInput(
            title, { title = it }, label = "Title", placeholder = "What do you want to remember?",
            enabled = !saving, error = "Give the reminder a title.".takeIf { attempted && title.isBlank() },
        )
        ThInput(
            description, { description = it }, label = "Details (optional)", multiline = true, enabled = !saving,
        )
        ThDivider()
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
            ThEyebrow("Schedule")
            ThInput(
                date, { date = it }, label = "Date (yyyy-mm-dd)", enabled = !saving,
                error = "Enter a valid date.".takeIf { attempted && !validDate },
            )
            ThInput(
                time, { time = it }, label = "Time (optional)", placeholder = "18:30", enabled = !saving,
                error = "Enter a 24-hour time such as 18:30.".takeIf { attempted && !validTime },
            )
            RecurrencePicker(recurrence, reminderRecurrences, { recurrence = it }, enabled = !saving)
        }
        ThDivider()
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                Text("Notify me", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (time.isBlank()) "Set a time for an alert." else "Uses this device’s local time and notification settings.",
                    style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
                )
            }
            ThSwitch(notify, { notify = it }, enabled = !saving, label = "Notify me")
        }
    }
}
