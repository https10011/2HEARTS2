package com.twohearts.app.ui.screens.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.datetime.scheduledReminderDate
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThSectionTabs
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.components.recurrenceLabel
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate

@Composable
fun RemindersHome(
    reminders: List<Reminder>,
    onReminderClick: (String) -> Unit,
    onAddReminder: () -> Unit,
    onBack: () -> Unit,
    onToggleCompleted: (Reminder) -> Unit = {},
    updating: Boolean = false,
    errorMessage: String? = null,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val today = LocalDate.now()
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val pending = reminders.filter { it.status != "completed" }
        .sortedWith(compareBy({ scheduledReminderDate(it, today) }, { it.scheduledTime.orEmpty() }, { it.id }))
    val groups = if (tab == 0) {
        val (overdue, scheduled) = pending.partition { it.needsAttention(today) }
        listOf("Needs attention" to overdue, "Scheduled" to scheduled)
    } else {
        listOf("Completed" to reminders.filter { it.status == "completed" }.sortedByDescending { it.updatedAt })
    }

    Scaffold(
        topBar = { ThTopBar("Reminders", onBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                ThDivider()
                ThButton(
                    onClick = onAddReminder, text = "Add reminder", icon = ThIcons.Add, full = true,
                    modifier = Modifier.padding(horizontal = spacing.screenGutter, vertical = spacing.space2),
                )
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ThSectionTabs(listOf("Pending", "Completed"), tab, { tab = it })
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space3),
            ) {
                errorMessage?.let { message -> item { ThStatusBanner(StatusVariant.ERROR, message) } }
                if (groups.all { it.second.isEmpty() }) item {
                    ThEmptyState(
                        title = if (tab == 0) "Nothing on your list" else "Nothing completed yet",
                        description = if (tab == 0) "Give a plan or promise a place on your list." else "Completed reminders will appear here.",
                        visual = { Icon(if (tab == 0) ThIcons.Alarm else ThIcons.CheckCircle, null, tint = colors.roseDeep) },
                    )
                }
                for ((label, group) in groups) {
                    if (group.isEmpty()) continue
                    item {
                        Text(
                            label, style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = spacing.space3, bottom = spacing.space2).semantics { heading() },
                        )
                    }
                    items(group, key = { it.id }) { reminder ->
                        ReminderRow(reminder, today, { onReminderClick(reminder.id) }, { onToggleCompleted(reminder) }, updating)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    today: LocalDate,
    open: () -> Unit,
    toggle: () -> Unit,
    updating: Boolean,
) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val completed = reminder.status == "completed"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = completed, onCheckedChange = { toggle() }, enabled = !updating,
            modifier = Modifier.semantics { contentDescription = if (completed) "Reopen ${reminder.title}" else "Complete ${reminder.title}" },
        )
        Column(
            Modifier.weight(1f).clickable(onClickLabel = "Open reminder", onClick = open).padding(vertical = spacing.space3),
            verticalArrangement = Arrangement.spacedBy(spacing.space1),
        ) {
            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
            reminder.description?.takeIf(String::isNotBlank)?.let {
                Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            val date = DateTimeHelper.formatShortDisplay(scheduledReminderDate(reminder, today))
            val time = reminder.scheduledTime?.takeIf(String::isNotBlank)?.let { " at $it" }.orEmpty()
            Text(
                date + time, style = MaterialTheme.typography.labelMedium,
                color = if (reminder.needsAttention(today)) colors.error else colors.textSecondary,
            )
            if (reminder.recurrence != "none") {
                Text(recurrenceLabel(reminder.recurrence), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
    ThDivider()
}

private fun Reminder.needsAttention(today: LocalDate): Boolean {
    if (status == "missed") return true
    return status == "pending" && runCatching { LocalDate.parse(scheduledReminderDate(this, today)).isBefore(today) }.getOrDefault(false)
}
