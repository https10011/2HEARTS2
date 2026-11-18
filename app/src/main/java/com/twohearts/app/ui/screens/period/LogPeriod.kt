package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogPeriod(
    entryId: String? = null,
    initialStartDate: String = DateTimeHelper.todayLocal(),
    initialEndDate: String = "",
    initialFlowLevel: String = "medium",
    initialNote: String = "",
    onSave: (startDate: String, endDate: String?, flowLevel: String, note: String?) -> Unit,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null,
    saving: Boolean = false,
    error: String? = null,
) {
    var start by rememberSaveable(entryId) { mutableStateOf(initialStartDate) }
    var end by rememberSaveable(entryId) { mutableStateOf(initialEndDate) }
    var flow by rememberSaveable(entryId) { mutableStateOf(initialFlowLevel) }
    var note by rememberSaveable(entryId) { mutableStateOf(initialNote) }
    var confirming by rememberSaveable { mutableStateOf(false) }
    val colors = LocalTwoHeartsColors.current
    val valid = validPeriodDates(start, end, LocalDate.now())

    Scaffold(topBar = {
        Header(title = if (entryId == null) "Log a period" else "Edit period", onBack = onBack)
    }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThDatePicker(
                value = start, onValueChange = { start = it },
                label = "Start date", clearable = false,
            )
            ThDatePicker(
                value = end, onValueChange = { end = it },
                label = "End date (optional)", placeholder = "Choose when it ended",
            )
            if (!valid) {
                Text("Enter a valid start and end in order, up to today.", color = colors.error)
            }
            Column {
                Text("Flow", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("light", "medium", "heavy").forEach { option ->
                        FilterChip(
                            selected = flow == option,
                            onClick = { flow = option },
                            label = { Text(option.replaceFirstChar { it.titlecase() }) },
                        )
                    }
                }
            }
            Input(
                value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(),
                label = "Private note (optional)", multiline = true,
            )
            if (error != null) ThStatusBanner(StatusVariant.ERROR, error)
            ThButton(
                onClick = { onSave(start, end.ifBlank { null }, flow, note.trim().ifBlank { null }) },
                enabled = valid, loading = saving, full = true, text = "Save period",
            )
            if (onDelete != null) {
                TextButton(onClick = { confirming = true }, enabled = !saving) {
                    Text("Delete entry", color = colors.error)
                }
            }
        }
    }
    if (confirming && onDelete != null) {
        ConfirmDialog(
            title = "Delete this period entry?",
            message = "This will remove its dates and note from your history.",
            confirmText = "Delete",
            onConfirm = { confirming = false; onDelete() },
            onDismiss = { confirming = false },
        )
    }
}
