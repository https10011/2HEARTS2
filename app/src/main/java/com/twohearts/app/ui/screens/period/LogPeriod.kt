package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThChoiceChip
import com.twohearts.app.ui.components.ThDatePicker
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
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
    today: LocalDate = LocalDate.now(),
) {
    var start by rememberSaveable(entryId) { mutableStateOf(initialStartDate) }
    var end by rememberSaveable(entryId) { mutableStateOf(initialEndDate) }
    var flow by rememberSaveable(entryId) { mutableStateOf(initialFlowLevel) }
    var note by rememberSaveable(entryId) { mutableStateOf(initialNote) }
    var confirming by rememberSaveable(entryId) { mutableStateOf(false) }
    val valid = validPeriodDates(start, end, today)
    val startDate = periodDate(start)
    val startError = "Choose a date on or before today.".takeIf { startDate == null || startDate.isAfter(today) }
    val endError = "Choose a date between the start and today.".takeIf { end.isNotBlank() && startError == null && !valid }
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current

    RecordEditorLayout(
        title = if (entryId == null) "Log a period" else "Edit period",
        saveText = "Save period",
        onSave = { if (valid && !saving) onSave(start, end.ifBlank { null }, flow, note.trim().ifBlank { null }) },
        onBack = onBack, saving = saving, errorMessage = error, saveEnabled = valid,
    ) {
        ThEyebrow("Dates", color = colors.roseDeep)
        ThDatePicker(start, { start = it }, label = "Start date", clearable = false, error = startError, enabled = !saving, maxYear = today.year)
        ThDatePicker(
            end, { end = it }, label = "End date (optional)", placeholder = "Choose when it ended",
            error = endError, enabled = !saving, maxYear = today.year,
        )
        ThDivider()
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            Text("Flow", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                verticalArrangement = Arrangement.spacedBy(spacing.space1),
            ) {
                (listOf("light", "medium", "heavy") + initialFlowLevel).distinct().forEach { option ->
                    ThChoiceChip(
                        selected = flow == option, onClick = { flow = option }, enabled = !saving,
                        label = { Text(periodFlowLabel(option)) },
                    )
                }
            }
        }
        ThInput(note, { note = it }, label = "Private note (optional)", multiline = true, enabled = !saving)
        if (onDelete != null) {
            ThButton(
                onClick = { confirming = true }, text = "Delete entry", enabled = !saving,
                variant = ButtonVariant.DANGER, icon = ThIcons.Trash,
            )
        }
    }
    if (confirming && onDelete != null) ConfirmDialog(
        title = "Delete this period entry?", message = "Its dates and note will be removed from your history.", confirmText = "Delete",
        onConfirm = { confirming = false; onDelete() }, onDismiss = { confirming = false },
    )
}
