package com.twohearts.app.ui.screens.importantdates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.Input
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantDatesScreen(
    dates: List<ImportantDate>, onSaveDate: (ImportantDate?, String, String, String) -> Unit,
    onDeleteDate: (ImportantDate) -> Unit, onBack: () -> Unit,
) {
    var editing by remember { mutableStateOf<ImportantDate?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    val colors = LocalTwoHeartsColors.current
    val ordered = dates.sortedBy { nextOccurrence(it) }
    Scaffold(
        topBar = { Header(title = "Important dates", onBack = onBack) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { editing = null; showEditor = true }, icon = { Icon(ThIcons.Add, null) }, text = { Text("Add a date") }, containerColor = colors.burgundy, contentColor = colors.textOnAccent) },
    ) { padding ->
        if (dates.isEmpty()) Column(Modifier.fillMaxSize().padding(padding).padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(ThIcons.Calendar, null, tint = colors.burgundy, modifier = Modifier.size(52.dp)); Spacer(Modifier.height(22.dp))
            Text("Keep the days that matter", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp)); Text("Anniversaries, birthdays, and the dates that belong to your story.", color = colors.textSecondary)
            Spacer(Modifier.height(24.dp)); Button(onClick = { showEditor = true }) { Text("Save your first date") }
        } else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 104.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("Moments to look forward to", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text("Meaningful dates, ordered by what comes next.", color = colors.textSecondary) }
            items(ordered, key = { it.id }) { date ->
                val days = ChronoUnit.DAYS.between(LocalDate.now(), nextOccurrence(date)).toInt()
                Surface(Modifier.fillMaxWidth().clickable { editing = date; showEditor = true }, shape = RoundedCornerShape(22.dp), color = if (days in 0..30) colors.goldBg else colors.surfaceElevated) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(ThIcons.Calendar, null, tint = if (days in 0..30) colors.goldInk else colors.burgundy, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) { Text(date.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text(DateTimeHelper.formatDisplay(date.date), color = colors.textSecondary); if (date.recurrence != "none") Text("Repeats ${date.recurrence}", style = MaterialTheme.typography.labelSmall, color = colors.roseDeep) }
                        Text(
                            when {
                                days < 0 -> "Past"
                                days == 0 -> "Today"
                                days == 1 -> "Tomorrow"
                                else -> "In $days days"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (days < 0) colors.textSecondary else colors.goldInk,
                        )
                    }
                }
            }
        }
    }
    if (showEditor) ImportantDateEditor(editing, onDismiss = { showEditor = false }, onSave = { title, date, recurrence -> onSaveDate(editing, title, date, recurrence); showEditor = false }, onDelete = editing?.let { item -> ({ onDeleteDate(item); showEditor = false }) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportantDateEditor(item: ImportantDate?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit, onDelete: (() -> Unit)?) {
    var title by remember(item) { mutableStateOf(item?.title.orEmpty()) }
    var date by remember(item) { mutableStateOf(item?.date ?: DateTimeHelper.todayLocal()) }
    var recurrence by remember(item) { mutableStateOf(item?.recurrence ?: "yearly") }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val valid = title.isNotBlank() && runCatching { LocalDate.parse(date) }.isSuccess
    if (confirmDelete && onDelete != null) {
        ConfirmDialog(
            title = "Delete this date?", message = "It will be removed from your important dates.",
            confirmText = "Delete", onConfirm = onDelete, onDismiss = { confirmDelete = false },
        )
    } else AlertDialog(onDismissRequest = onDismiss, title = { Text(if (item == null) "Add an important date" else "Edit important date") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Input(title, { title = it }, Modifier.fillMaxWidth(), label = "What happened?", placeholder = "Our anniversary")
            Input(date, { date = it }, Modifier.fillMaxWidth(), label = "Date", placeholder = "yyyy-mm-dd")
            ExposedDropdownMenuBox(menu, { menu = it }) { OutlinedTextField(recurrence.replaceFirstChar(Char::uppercase), {}, readOnly = true, label = { Text("Repeats") }, modifier = Modifier.fillMaxWidth().menuAnchor()); ExposedDropdownMenu(menu, { menu = false }) { listOf("none", "yearly", "monthly").forEach { option -> DropdownMenuItem({ Text(option.replaceFirstChar(Char::uppercase)) }, { recurrence = option; menu = false }) } } }
            if (onDelete != null) TextButton(onClick = { confirmDelete = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(ThIcons.Trash, null); Spacer(Modifier.width(6.dp)); Text("Delete date") }
        }
    }, confirmButton = { TextButton(onClick = { onSave(title.trim(), date, recurrence) }, enabled = valid) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

internal fun nextOccurrence(item: ImportantDate, today: LocalDate = LocalDate.now()): LocalDate {
    val original = runCatching { LocalDate.parse(item.date) }.getOrElse { return today }
    if (item.recurrence == "none") return original
    var next = when (item.recurrence) { "monthly" -> original.withYear(today.year).withMonth(today.monthValue).withDayOfMonth(original.dayOfMonth.coerceAtMost(today.lengthOfMonth())); else -> original.withYear(today.year) }
    while (next.isBefore(today)) next = if (item.recurrence == "monthly") next.plusMonths(1) else next.plusYears(1)
    return next
}
