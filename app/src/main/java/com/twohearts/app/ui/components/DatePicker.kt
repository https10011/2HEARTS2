package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThDatePicker — the app's date field.
 *
 * ## Why this exists
 *
 * Phase 0 recorded that every date in the product — a birthday, the day a
 * relationship began, a memory's date — was captured as a bare text field
 * with the placeholder `yyyy-mm-dd`. That is the single most visible piece of
 * friction a person meets during first launch: it asks them to type a machine
 * format, gives no confirmation that the date is real, and rejects Feb 30 only
 * after the fact. It also produced the app's worst validation copy
 * ("start date must be in yyyy-mm-dd format").
 *
 * This renders the same interaction the approved reference screens use: a
 * warm trigger that shows a human-readable date, opening a three-column
 * wheel sheet. There is no text entry at all, so an invalid date is not
 * *rejected* — it cannot be expressed.
 *
 * ## Accessibility
 *
 * The trigger is a labelled button publishing its current value; the sheet's
 * three columns are individually labelled and expose each option as a
 * selectable-with-state item, so the control is usable with a screen reader
 * and — because the columns are ordinary scrollables — with keyboard and
 * switch navigation. The confirm action states the chosen date in its label.
 *
 * @param value ISO `yyyy-mm-dd`, or null/empty for "not chosen yet".
 * @param onValueChange receives the ISO string when the person confirms.
 * @param label the field label rendered above the trigger.
 * @param placeholder shown on the trigger before a date is chosen.
 * @param support optional supporting line under the label.
 * @param error validation message, which also switches the trigger to its
 *   error treatment so the state is never colour-only.
 * @param minYear earliest selectable year.
 * @param maxYear latest selectable year.
 */
@Composable
fun ThDatePicker(
    value: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "Choose a date",
    support: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    clearable: Boolean = true,
    minYear: Int = 1930,
    maxYear: Int = currentYear(),
) {
    val thColors = LocalTwoHeartsColors.current
    val isError = error != null
    var open by remember { mutableStateOf(false) }

    val parsed = remember(value) { parseIsoDate(value) }
    val display = parsed?.let { formatDisplayDate(it) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isError) thColors.error else thColors.textSecondary,
            )
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.labelGap))
        }
        if (support != null) {
            Text(
                text = support,
                style = MaterialTheme.typography.bodySmall,
                color = thColors.textTertiary,
            )
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.labelGap))
        }

        val shape = RoundedCornerShape(TwoHeartsTokens.Radius.sm)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
                .background(
                    if (enabled) MaterialTheme.colorScheme.surface else thColors.surfaceWarm,
                    shape,
                )
                .border(
                    TwoHeartsTokens.Border.hairline,
                    if (isError) thColors.error else thColors.borderStrong,
                    shape,
                )
                .clip(shape)
                .clickable(enabled = enabled, role = Role.Button) { open = true }
                .padding(
                    horizontal = TwoHeartsTokens.Spacing.space4,
                    vertical = TwoHeartsTokens.Spacing.space2,
                )
                .semantics {
                    contentDescription = if (display != null) {
                        "${label.ifEmpty { "Date" }}: $display"
                    } else {
                        "${label.ifEmpty { "Date" }}: $placeholder"
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ThIcons.Calendar,
                contentDescription = null,
                tint = if (display != null) MaterialTheme.colorScheme.primary else thColors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
            Text(
                text = display ?: placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = if (display != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    thColors.textTertiary
                },
                modifier = Modifier.weight(1f),
            )
        }

        if (isError) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.labelGap))
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodySmall,
                color = thColors.error,
            )
        }
    }

    if (open) {
        DatePickerSheet(
            initial = parsed,
            minYear = minYear,
            maxYear = maxYear,
            title = label.ifEmpty { "Choose a date" },
            clearable = clearable && value?.isNotEmpty() == true,
            onDismiss = { open = false },
            onConfirm = { iso ->
                onValueChange(iso)
                open = false
            },
            onClear = {
                onValueChange("")
                open = false
            },
        )
    }
}

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private val MONTH_SHORT = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/** A resolved calendar date. Pure data so the sheet's logic stays testable. */
internal data class CalendarDate(val year: Int, val month: Int, val day: Int)

/**
 * Days in a 1-indexed month, leap years included.
 *
 * Deliberately written out rather than delegated to `java.time` so the whole
 * picker is a pure function of its inputs — the same reason Phase 3's
 * datetime helpers avoid a date library.
 */
internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

internal fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

/**
 * Parses an ISO `yyyy-mm-dd` string, rejecting dates that do not exist.
 *
 * A regex alone would accept `2025-02-30`; this checks the day against the
 * month's real length so the field can never round-trip an impossible date.
 */
internal fun parseIsoDate(value: String?): CalendarDate? {
    if (value.isNullOrBlank()) return null
    val parts = value.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12) return null
    if (day !in 1..daysInMonth(year, month)) return null
    return CalendarDate(year, month, day)
}

internal fun formatIsoDate(date: CalendarDate): String {
    val m = if (date.month < 10) "0${date.month}" else "${date.month}"
    val d = if (date.day < 10) "0${date.day}" else "${date.day}"
    return "${date.year}-$m-$d"
}

/** Human-readable form used on the trigger: "14 June 2022". */
internal fun formatDisplayDate(date: CalendarDate): String =
    "${date.day} ${MONTH_NAMES[date.month - 1]} ${date.year}"

private fun currentYear(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

/**
 * DatePickerSheet — the modal date chooser.
 *
 * Three independently scrollable columns of selectable chips with a live
 * preview line above them. Each column is an ordinary scrollable, so touch,
 * keyboard and assistive technology all get the platform's normal scrolling
 * behaviour rather than a bespoke gesture surface.
 */
@Composable
private fun DatePickerSheet(
    initial: CalendarDate?,
    minYear: Int,
    maxYear: Int,
    title: String,
    clearable: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClear: () -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current
    val today = remember { java.util.Calendar.getInstance() }
    val defaultYear = today.get(java.util.Calendar.YEAR)
    val defaultMonth = today.get(java.util.Calendar.MONTH) + 1
    val defaultDay = today.get(java.util.Calendar.DAY_OF_MONTH)

    // The sheet holds a working copy; nothing is written back until confirm,
    // so dismissing by tapping the scrim is always a cancel.
    var year by remember { mutableIntStateOf(initial?.year ?: defaultYear) }
    var month by remember { mutableIntStateOf(initial?.month ?: defaultMonth) }
    var day by remember { mutableIntStateOf(initial?.day ?: defaultDay) }

    val years = remember(minYear, maxYear) { (maxYear downTo minYear).toList() }
    val days = remember(year, month) { (1..daysInMonth(year, month)).toList() }

    // Clamp the day when a shorter month is selected: choosing 31 then
    // switching to February must not leave an impossible date selected.
    LaunchedEffect(days.size) {
        if (day > days.size) day = days.size
    }

    ThModal(
        open = true,
        onClose = onDismiss,
        label = title,
        modifier = Modifier
            .heightIn(max = 460.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))
        Text(
            text = formatDisplayDate(CalendarDate(year, month, day)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
        ) {
            PickerColumn(
                label = "Day",
                items = days.map { it.toString() },
                selectedIndex = day - 1,
                onSelected = { day = days[it] },
                modifier = Modifier.weight(0.7f),
            )
            PickerColumn(
                label = "Month",
                items = MONTH_SHORT,
                selectedIndex = month - 1,
                onSelected = { month = it + 1 },
                modifier = Modifier.weight(1.1f),
            )
            PickerColumn(
                label = "Year",
                items = years.map { it.toString() },
                selectedIndex = years.indexOf(year).coerceAtLeast(0),
                onSelected = { year = years[it] },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

        ThButton(
            onClick = { onConfirm(formatIsoDate(CalendarDate(year, month, day))) },
            text = "Use ${formatDisplayDate(CalendarDate(year, month, day))}",
            full = true,
        )

        if (clearable) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
            ThQuietButton(
                onClick = onClear,
                text = "Clear date",
                modifier = Modifier.fillMaxWidth(),
                contentColor = thColors.textSecondary,
            )
        }
    }
}

/**
 * PickerColumn — one scrollable column of selectable options.
 *
 * The approved reference screens use selectable chips in month/day/year
 * columns rather than a spinning wheel. That choice is worth keeping: a chip
 * list is an ordinary scrollable, so touch, keyboard, switch access and
 * screen readers all get standard behaviour, and — unlike a fixed-height
 * wheel — it does not clip when the text-size setting is raised.
 *
 * Selection is communicated by fill, border *and* posted selected state, so
 * it never depends on colour alone.
 */
@Composable
private fun PickerColumn(
    label: String,
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentWidth: Dp = 64.dp,
) {
    val thColors = LocalTwoHeartsColors.current

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = thColors.textTertiary,
        )
        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
                .semantics { contentDescription = label },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val shape = RoundedCornerShape(TwoHeartsTokens.Radius.sm)
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .width(contentWidth)
                        .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
                        .clip(shape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape,
                        )
                        .border(
                            TwoHeartsTokens.Border.hairline,
                            if (isSelected) MaterialTheme.colorScheme.primary else thColors.borderSubtle,
                            shape,
                        )
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onSelected(index) },
                        )
                        .padding(vertical = TwoHeartsTokens.Spacing.space1),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * ThDateField — a read-only date display used where the date is a consequence
 * of another choice rather than something the person types (the completion
 * summary). Kept separate so it never gains an edit affordance it should not
 * have.
 */
@Composable
fun ThDateValue(
    value: String?,
    modifier: Modifier = Modifier,
    emptyLabel: String = "Not set",
) {
    val thColors = LocalTwoHeartsColors.current
    val parsed = remember(value) { parseIsoDate(value) }
    Text(
        text = parsed?.let { formatDisplayDate(it) } ?: emptyLabel,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = if (parsed != null) MaterialTheme.colorScheme.onSurface else thColors.textTertiary,
    )
}
