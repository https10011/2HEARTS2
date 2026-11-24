package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.datetime.scheduledReminderDate
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.components.recurrenceLabel
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun ReminderDetail(
    reminder: Reminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onToggleCompleted: () -> Unit = {},
    updating: Boolean = false,
    errorMessage: String? = null,
) {
    var confirmDelete by rememberSaveable(reminder.id) { mutableStateOf(false) }
    val completed = reminder.status == "completed"
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing

    Scaffold(topBar = {
        ThTopBar("Reminder", onBack = onBack, actions = {
            ThIconButton(onEdit, label = "Edit reminder", enabled = !updating) { Icon(ThIcons.Edit, null) }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space6),
                verticalArrangement = Arrangement.spacedBy(spacing.space4),
            ) {
                ThEyebrow(
                    when (reminder.status) {
                        "completed" -> "Completed"
                        "missed" -> "Needs attention"
                        else -> "Pending"
                    },
                    color = if (reminder.status == "missed") colors.error else colors.roseDeep,
                )
                Text(reminder.title, style = MaterialTheme.typography.displayMedium, modifier = Modifier.semantics { heading() })
                reminder.description?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = ThTextStyles.reading, color = colors.textSecondary)
                }
                Surface(shape = MaterialTheme.shapes.large, color = colors.surfaceWarm, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        ThEyebrow("Schedule", color = colors.roseDeep)
                        Text(DateTimeHelper.formatDisplay(scheduledReminderDate(reminder)), style = MaterialTheme.typography.titleLarge)
                        reminder.scheduledTime?.takeIf(String::isNotBlank)?.let {
                            Text("At $it · Local time", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        }
                        if (reminder.recurrence != "none") Text(recurrenceLabel(reminder.recurrence), color = colors.textSecondary)
                        Text(
                            when {
                                completed -> "Alerts pause while completed."
                                reminder.status == "missed" -> "This reminder is marked as missed."
                                !reminder.notificationEnabled -> "Alert off for this reminder."
                                reminder.scheduledTime.isNullOrBlank() -> "Set a time for an alert."
                                else -> "Alerts follow your notification settings."
                            },
                            style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
                        )
                    }
                }
                errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
                ThButton(
                    onClick = onToggleCompleted, text = if (completed) "Mark as pending" else "Complete reminder",
                    icon = if (completed) ThIcons.Repeat else ThIcons.CheckCircle,
                    full = true, enabled = !updating,
                )
                ThDivider()
                Text("Created ${DateTimeHelper.formatDisplay(reminder.createdAt)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                if (reminder.updatedAt != reminder.createdAt) {
                    Text("Updated ${DateTimeHelper.formatDisplay(reminder.updatedAt)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                ThButton(
                    onClick = { confirmDelete = true }, text = "Delete reminder", enabled = !updating,
                    variant = ButtonVariant.DANGER, icon = ThIcons.Trash,
                )
            }
        }
    }

    if (confirmDelete) ConfirmDialog(
        title = "Delete this reminder?", message = "This reminder and its future alerts will be removed.",
        confirmText = "Delete", onConfirm = { confirmDelete = false; onDelete() }, onDismiss = { confirmDelete = false },
    )
}
