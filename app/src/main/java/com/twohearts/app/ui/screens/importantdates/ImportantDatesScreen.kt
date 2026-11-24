package com.twohearts.app.ui.screens.importantdates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.datetime.nextCalendarOccurrence
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.RecurrencePicker
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.components.recurrenceLabel
import com.twohearts.app.ui.components.useToast
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val dateRecurrences = listOf("none", "monthly", "yearly")

@Composable
fun ImportantDatesScreen(
    dates: List<ImportantDate>,
    onSaveDate: suspend (ImportantDate?, String, String, String) -> Unit,
    onDeleteDate: suspend (ImportantDate) -> Unit,
    onBack: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val (dated, unavailable) = dates.map { it to nextOccurrence(it, today) }.partition { it.second != null }
    val (upcoming, past) = dated.partition { (_, date) -> date?.isBefore(today) == false }
    val orderedUpcoming = upcoming.sortedBy { it.second }
    val groups = listOf(
        "Coming up" to orderedUpcoming.drop(1),
        "Past dates" to past.sortedByDescending { it.second },
        "Check these dates" to unavailable,
    )

    Scaffold(
        topBar = { ThTopBar("Important dates", onBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                ThDivider()
                ThButton(
                    onClick = { editingId = null; showEditor = true }, text = "Add a date", icon = ThIcons.Add,
                    full = true, modifier = Modifier.padding(horizontal = spacing.screenGutter, vertical = spacing.space2),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            state = listState,
            contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            if (dates.isEmpty()) item {
                ThEmptyState(
                    title = "Keep the days that matter",
                    description = "Save anniversaries, birthdays, and dates with a story of their own.",
                    visual = { Icon(ThIcons.Calendar, null, tint = colors.goldInk) },
                )
            }
            orderedUpcoming.firstOrNull()?.let { (date, occurrence) ->
                item(key = date.id) {
                    Surface(
                        onClick = { editingId = date.id; showEditor = true },
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = colors.goldBg,
                    ) {
                        Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                            Icon(ThIcons.CalendarMonth, null, tint = colors.goldInk)
                            ThEyebrow("Next important date", color = colors.goldInk)
                            Text(date.title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                            if (occurrence != null) {
                                Text(dateDistance(today, occurrence), style = MaterialTheme.typography.titleMedium, color = colors.goldInk)
                            }
                            DateMetadata(date, occurrence)
                        }
                    }
                }
            }
            for ((label, group) in groups) {
                if (group.isEmpty()) continue
                item {
                    Text(
                        label, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.space2).semantics { heading() },
                    )
                }
                items(group, key = { it.first.id }) { (date, occurrence) ->
                    Column(
                        Modifier.fillMaxWidth().clickable(onClickLabel = "Edit date") { editingId = date.id; showEditor = true }
                            .padding(vertical = spacing.space2),
                        verticalArrangement = Arrangement.spacedBy(spacing.space1),
                    ) {
                        Text(date.title, style = MaterialTheme.typography.titleMedium)
                        DateMetadata(date, occurrence)
                    }
                    ThDivider()
                }
            }
        }
    }

    val item = dates.firstOrNull { it.id == editingId }
    if (showEditor && (editingId == null || item != null)) {
        ImportantDateEditor(
            item = item, today = today, onDismiss = { showEditor = false },
            onSave = { title, date, recurrence ->
                onSaveDate(item, title, date, recurrence)
                if (item == null) listState.requestScrollToItem(0)
            },
            onDelete = item?.let { { onDeleteDate(it) } },
        )
    }
}

@Composable
private fun DateMetadata(item: ImportantDate, occurrence: LocalDate?) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    Text(
        occurrence?.let { DateTimeHelper.formatDisplay(it.toString()) } ?: "Saved date needs editing",
        style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary,
    )
    if (item.recurrence != "none") {
        Text(recurrenceLabel(item.recurrence), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ImportantDateEditor(
    item: ImportantDate?,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: suspend (String, String, String) -> Unit,
    onDelete: (suspend () -> Unit)?,
) {
    var title by rememberSaveable(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    var date by rememberSaveable(item?.id) { mutableStateOf(item?.date ?: today.toString()) }
    var recurrence by rememberSaveable(item?.id) { mutableStateOf(item?.recurrence ?: "yearly") }
    var attempted by rememberSaveable(item?.id) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(item?.id) { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val feedback = useToast()
    val spacing = TwoHeartsTokens.Spacing
    val validDate = runCatching { LocalDate.parse(date.trim()) }.isSuccess
    val dismiss = { if (!saving) onDismiss() }

    fun submit(success: String, failure: String, write: suspend () -> Unit) {
        if (saving) return
        saving = true
        error = null
        scope.launch {
            try {
                write()
                feedback.success(success)
                onDismiss()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                error = failure
            } finally {
                saving = false
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        ConfirmDialog(
            title = "Delete this date?", message = "It will be removed from your important dates.", confirmText = "Delete",
            onConfirm = {
                confirmDelete = false
                submit("Important date deleted.", "Couldn’t delete this date.", onDelete)
            },
            onDismiss = { confirmDelete = false },
        )
    } else {
        val label = if (item == null) "Add an important date" else "Edit important date"
        ThModal(open = true, onClose = dismiss, label = label, modifier = Modifier.imePadding()) {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                Text(label, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                error?.let { ThStatusBanner(StatusVariant.ERROR, it) }
                ThInput(
                    title, { title = it }, label = "What happened?", placeholder = "Our anniversary", enabled = !saving,
                    error = "Give the date a title.".takeIf { attempted && title.isBlank() },
                )
                ThInput(
                    date, { date = it }, label = "Date (yyyy-mm-dd)", enabled = !saving,
                    error = "Enter a valid date.".takeIf { attempted && !validDate },
                )
                RecurrencePicker(recurrence, dateRecurrences, { recurrence = it }, enabled = !saving)
                ThButton(
                    onClick = {
                        attempted = true
                        if (title.isNotBlank() && validDate) submit("Important date saved.", "Couldn’t save this date.") {
                            onSave(title.trim(), date.trim(), recurrence)
                        }
                    },
                    text = "Save date", full = true, loading = saving, icon = ThIcons.Check,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ThButton(dismiss, text = "Cancel", variant = ButtonVariant.GHOST, enabled = !saving)
                    if (onDelete != null) {
                        ThButton({ confirmDelete = true }, text = "Delete date", variant = ButtonVariant.DANGER, enabled = !saving)
                    }
                }
            }
        }
    }
}

internal fun nextOccurrence(item: ImportantDate, today: LocalDate = LocalDate.now()): LocalDate? {
    val original = runCatching { LocalDate.parse(item.date) }.getOrNull() ?: return null
    return nextCalendarOccurrence(original, item.recurrence, today) ?: original
}

private fun dateDistance(today: LocalDate, date: LocalDate): String = when (val days = ChronoUnit.DAYS.between(today, date)) {
    0L -> "Today"
    1L -> "Tomorrow"
    else -> "In $days days"
}
