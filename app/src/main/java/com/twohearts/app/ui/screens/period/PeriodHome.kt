package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate

@Composable
fun PeriodHome(
    latestEntry: PeriodEntry?,
    onLogPeriod: () -> Unit,
    onViewCalendar: () -> Unit,
    onViewHistory: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit,
    cycleLength: Int = 28,
    onEditLatest: (() -> Unit)? = null,
    today: LocalDate = LocalDate.now(),
) {
    val colors = LocalTwoHeartsColors.current
    val start = periodDate(latestEntry?.startDate)
    val end = periodDate(latestEntry?.endDate)
    val estimate = nextPeriodEstimate(latestEntry, cycleLength, today)

    Scaffold(topBar = { Header(title = "Period tracker", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Surface(shape = MaterialTheme.shapes.large, color = colors.surfaceWarm) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Latest period", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    Text(
                        start?.let { DateTimeHelper.formatDisplay(it.toString()) } ?: "No dates logged yet",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    if (start != null) {
                        Text(
                            if (end == null) "End date missing" else "Ended ${DateTimeHelper.formatDisplay(end.toString())}",
                            color = colors.textSecondary,
                        )
                        if (onEditLatest != null) TextButton(onClick = onEditLatest) {
                            Text(if (end == null) "Add end date" else "Edit latest entry")
                        }
                    }
                }
            }
            if (estimate != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Estimated next start", style = MaterialTheme.typography.titleMedium)
                    Text(DateTimeHelper.formatDisplay(estimate.toString()), color = colors.textSecondary)
                    Text("Based on your typical cycle length. Dates can vary.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
            }
            ThButton(onClick = onLogPeriod, full = true, text = "Log a period")
            HorizontalDivider()
            PeriodLink("Calendar", onViewCalendar)
            PeriodLink("History", onViewHistory)
            PeriodLink("Cycle settings", onSettings)
            Text("Period data stays on this device.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun PeriodLink(label: String, action: () -> Unit) {
    Surface(onClick = action, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(ThIcons.ChevronRight, contentDescription = null)
        }
    }
}
