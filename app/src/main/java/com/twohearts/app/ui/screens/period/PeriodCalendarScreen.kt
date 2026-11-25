package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun PeriodCalendarScreen(
    entries: List<PeriodEntry>,
    onBack: () -> Unit,
    cycleLength: Int = 28,
    periodLength: Int = 5,
    today: LocalDate = LocalDate.now(),
) {
    var monthOffset by rememberSaveable(today.toString()) { mutableIntStateOf(0) }
    val month = YearMonth.from(today).plusMonths(monthOffset.toLong())
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val estimate = nextPeriodEstimate(latestLoggedPeriod(entries, today), cycleLength, today)
    val estimateRange = estimate?.let { it..it.plusDays(periodLength.coerceIn(1, 30).toLong() - 1) }
    val ranges = remember(entries, today) {
        entries.mapNotNull(::loggedPeriodRange)
            .filter { it.start <= today }
            .map { it.start..minOf(it.endInclusive, today) }
    }
    val locale = Locale.getDefault()
    val weekStart = WeekFields.of(locale).firstDayOfWeek
    val firstDay = (month.atDay(1).dayOfWeek.value - weekStart.value + 7) % 7

    Scaffold(topBar = { ThTopBar("Period calendar", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 580.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space4),
                verticalArrangement = Arrangement.spacedBy(spacing.space3),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThIconButton({ monthOffset-- }, label = "Previous month") { Icon(ThIcons.Back, null) }
                    Text(
                        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)),
                        modifier = Modifier.weight(1f).semantics { heading() }, textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    ThIconButton({ monthOffset++ }, label = "Next month") { Icon(ThIcons.ChevronRight, null) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(7) { column ->
                        Text(
                            weekStart.plus(column.toLong()).getDisplayName(TextStyle.NARROW, locale),
                            Modifier.weight(1f).clearAndSetSemantics {},
                            textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat((firstDay + month.lengthOfMonth() + 6) / 7) { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(7) { column ->
                                val day = week * 7 + column - firstDay + 1
                                if (day !in 1..month.lengthOfMonth()) {
                                    Spacer(Modifier.weight(1f))
                                } else {
                                    val date = month.atDay(day)
                                    val mark = when {
                                        ranges.any { date in it } -> PeriodMark.LOGGED
                                        estimateRange?.contains(date) == true -> PeriodMark.ESTIMATED
                                        else -> null
                                    }
                                    PeriodDay(date, date == today, mark, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                ThDivider()
                LegendItem(PeriodMark.LOGGED, "Logged period")
                if (estimateRange != null) LegendItem(PeriodMark.ESTIMATED, "Estimated period")
                if (ranges.isEmpty()) Text("Logged dates will appear here.", color = colors.textSecondary)
            }
        }
    }
}

private enum class PeriodMark { LOGGED, ESTIMATED }

@Composable
private fun PeriodDay(date: LocalDate, today: Boolean, mark: PeriodMark?, modifier: Modifier) {
    val colors = LocalTwoHeartsColors.current
    val label = buildString {
        append(DateTimeHelper.formatDisplay(date.toString()))
        if (today) append(", today")
        when (mark) {
            PeriodMark.LOGGED -> append(", logged period")
            PeriodMark.ESTIMATED -> append(", estimated period")
            null -> Unit
        }
    }
    val color = when (mark) {
        PeriodMark.LOGGED -> colors.surfaceBlush
        PeriodMark.ESTIMATED -> colors.goldBg
        null -> MaterialTheme.colorScheme.surface
    }
    Surface(
        modifier = modifier.heightIn(min = 46.dp).clearAndSetSemantics { contentDescription = label },
        shape = MaterialTheme.shapes.small, color = color,
        border = if (today) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false)
            if (mark != null) PeriodMarker(mark)
        }
    }
}

@Composable
private fun LegendItem(mark: PeriodMark, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2)) {
        PeriodMarker(mark)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = LocalTwoHeartsColors.current.textSecondary)
    }
}

@Composable
private fun PeriodMarker(mark: PeriodMark) {
    val colors = LocalTwoHeartsColors.current
    val shape = Modifier.size(8.dp)
    Box(when (mark) {
        PeriodMark.LOGGED -> shape.background(colors.roseDeep, CircleShape)
        PeriodMark.ESTIMATED -> shape.border(1.5.dp, colors.goldInk, CircleShape)
    })
}
