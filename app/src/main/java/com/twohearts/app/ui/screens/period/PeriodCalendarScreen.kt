package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun PeriodCalendarScreen(
    entries: List<PeriodEntry>,
    onBack: () -> Unit,
    cycleLength: Int = 28,
    periodLength: Int = 5,
    today: LocalDate = LocalDate.now(),
) {
    var monthOffset by rememberSaveable(today) { mutableIntStateOf(0) }
    val month = YearMonth.from(today).plusMonths(monthOffset.toLong())
    val colors = LocalTwoHeartsColors.current
    val estimate = nextPeriodEstimate(entries.firstOrNull(), cycleLength, today)
    val ranges = remember(entries) { entries.mapNotNull(::loggedPeriodRange) }
    val firstDay = month.atDay(1).dayOfWeek.value % 7

    Scaffold(topBar = { Header(title = "Period calendar", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { monthOffset-- }) { Icon(ThIcons.Back, "Previous month") }
                Text(
                    month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = { monthOffset++ }) { Icon(ThIcons.ChevronRight, "Next month") }
            }
            Row {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { weekday ->
                    Text(
                        weekday,
                        Modifier.weight(1f).clearAndSetSemantics {},
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                }
            }
            repeat((firstDay + month.lengthOfMonth() + 6) / 7) { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(7) { column ->
                        val day = week * 7 + column - firstDay + 1
                        val date = if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
                        if (date == null) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            val logged = ranges.any { date in it }
                            val estimated = estimate != null &&
                                date in estimate..estimate.plusDays(periodLength.coerceIn(1, 30).toLong() - 1)
                            val label = buildString {
                                append(DateTimeHelper.formatDisplay(date.toString()))
                                if (date == today) append(", today")
                                if (logged) append(", logged period")
                                else if (estimated) append(", estimated period")
                            }
                            val background = when {
                                logged -> colors.surfaceBlush
                                estimated -> colors.goldBg
                                else -> MaterialTheme.colorScheme.surface
                            }
                            Surface(
                                modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                                    .clearAndSetSemantics { contentDescription = label },
                                shape = MaterialTheme.shapes.small,
                                color = background,
                                border = when {
                                    date == today -> BorderStroke(2.dp, colors.burgundy)
                                    estimated && !logged -> BorderStroke(1.dp, colors.goldInk)
                                    else -> null
                                },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(day.toString(), style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
            Text("Rose · Logged period", color = colors.roseDeep, style = MaterialTheme.typography.bodyMedium)
            if (estimate != null) Text("Gold · Estimated dates", color = colors.goldInk, style = MaterialTheme.typography.bodyMedium)
            if (ranges.isEmpty()) Text("Logged periods will appear here.", color = colors.textSecondary)
        }
    }
}
