package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
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
    val palette = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val start = periodDate(latestEntry?.startDate)
    val end = periodDate(latestEntry?.endDate)?.takeIf { start != null && it in start..today }
    val estimate = nextPeriodEstimate(latestEntry, cycleLength, today)

    Scaffold(topBar = { ThTopBar("Period tracker", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
                verticalArrangement = Arrangement.spacedBy(spacing.space4),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ThEyebrow("Latest period", color = palette.roseDeep)
                    Text(
                        start?.let { DateTimeHelper.formatDisplay(it.toString()) } ?: "No dates logged yet",
                        style = MaterialTheme.typography.displayMedium, modifier = Modifier.semantics { heading() },
                    )
                    if (start != null) {
                        Text(
                            end?.let { "Ended ${DateTimeHelper.formatDisplay(it.toString())}" } ?: "End date missing",
                            color = palette.textSecondary,
                        )
                        if (onEditLatest != null) ThButton(
                            onEditLatest, text = if (end == null) "Add end date" else "Edit latest entry",
                            variant = ButtonVariant.GHOST, icon = ThIcons.Edit,
                        )
                    }
                }
                ThButton(onLogPeriod, full = true, text = "Log a period", icon = ThIcons.Add)
                if (estimate != null) {
                    Surface(color = palette.goldBg, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                            ThEyebrow("Estimated next start", color = palette.goldInk)
                            Text(DateTimeHelper.formatDisplay(estimate.toString()), style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "Based on your typical cycle length. Dates can vary.",
                                style = MaterialTheme.typography.bodySmall, color = palette.textSecondary,
                            )
                        }
                    }
                }
                Column {
                    ThDivider()
                    PeriodLink("Calendar", ThIcons.CalendarMonth, onViewCalendar)
                    ThDivider()
                    PeriodLink("History", ThIcons.Calendar, onViewHistory)
                    ThDivider()
                    PeriodLink("Cycle settings", ThIcons.Settings, onSettings)
                    ThDivider()
                }
                Text("Period data stays on this device.", style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
            }
        }
    }
}

@Composable
private fun PeriodLink(label: String, icon: ImageVector, action: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    Row(
        Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = LocalTwoHeartsColors.current.roseDeep)
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Icon(ThIcons.ChevronRight, null, tint = LocalTwoHeartsColors.current.textSecondary)
    }
}
