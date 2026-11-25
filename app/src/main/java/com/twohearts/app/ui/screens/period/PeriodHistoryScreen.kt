package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun PeriodHistoryScreen(
    entries: List<PeriodEntry>,
    onBack: () -> Unit,
    onEdit: ((PeriodEntry) -> Unit)? = null,
) {
    val spacing = TwoHeartsTokens.Spacing
    val groups = remember(entries) {
        entries.sortedWith(compareByDescending<PeriodEntry> { periodDate(it.startDate) }.thenByDescending { it.updatedAt })
            .groupBy { periodDate(it.startDate)?.let(YearMonth::from) }
    }
    Scaffold(topBar = { ThTopBar("Period history", onBack = onBack) }) { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            if (entries.isEmpty()) item {
                ThEmptyState(
                    title = "No periods logged yet",
                    description = "The dates and notes you save will appear here.",
                    visual = { Icon(ThIcons.Calendar, null, tint = LocalTwoHeartsColors.current.roseDeep) },
                )
            }
            for ((month, group) in groups) {
                item {
                    Text(
                        month?.format(periodHistoryMonth) ?: "Check these dates",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.space2).semantics { heading() },
                    )
                }
                items(group, key = { it.id }) { entry ->
                    PeriodHistoryRow(entry, onEdit?.let { { onEdit(entry) } })
                    ThDivider()
                }
            }
        }
    }
}

@Composable
private fun PeriodHistoryRow(entry: PeriodEntry, edit: (() -> Unit)?) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    val start = periodDate(entry.startDate)
    val end = periodDate(entry.endDate)?.takeIf { start != null && !it.isBefore(start) }

    Column(Modifier.padding(vertical = spacing.space2), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
        ThEyebrow(periodFlowLabel(entry.flowLevel), color = colors.roseDeep)
        Text(
            start?.let { "Started ${DateTimeHelper.formatDisplay(it.toString())}" } ?: "Start date needs editing",
            style = MaterialTheme.typography.titleMedium,
        )
        val details = when {
            start != null && end != null -> {
                val days = ChronoUnit.DAYS.between(start, end) + 1
                "Ended ${DateTimeHelper.formatDisplay(end.toString())} · ${if (days == 1L) "1 day" else "$days days"}"
            }
            entry.endDate.isNullOrBlank() -> "End date missing"
            else -> "End date needs editing"
        }
        Text(details, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        entry.note?.takeIf(String::isNotBlank)?.let { Text(it, style = ThTextStyles.reading, color = colors.textSecondary) }
        if (edit != null) ThButton(edit, text = "Edit entry", variant = ButtonVariant.GHOST, icon = ThIcons.Edit)
    }
}

private val periodHistoryMonth = DateTimeFormatter.ofPattern("MMMM yyyy")
