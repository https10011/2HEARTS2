package com.twohearts.app.ui.screens.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class DatedMoment(val event: TimelineEvent, val date: LocalDate?)

internal fun timelineMonths(events: List<TimelineEvent>): Map<YearMonth?, List<DatedMoment>> =
    events.map { event -> DatedMoment(event, runCatching { LocalDate.parse(event.eventDate) }.getOrNull()) }
        .sortedWith(compareByDescending<DatedMoment> { it.date }.thenByDescending { it.event.createdAt }.thenBy { it.event.id })
        .groupBy { it.date?.let(YearMonth::from) }

@Composable
fun TimelineHome(events: List<TimelineEvent>, onEventClick: (String) -> Unit, onAddEvent: () -> Unit, onBack: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    val groups = remember(events) { timelineMonths(events) }
    val locale = Locale.getDefault()
    val monthFormat = remember(locale) { DateTimeFormatter.ofPattern("MMMM uuuu", locale) }
    val dayFormat = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d", locale) }
    Scaffold(topBar = {
        ThTopBar("Our timeline", onBack = onBack, actions = {
            if (events.isNotEmpty()) ThIconButton(onAddEvent, label = "Add a moment") {
                Icon(ThIcons.Add, null, tint = MaterialTheme.colorScheme.primary)
            }
        })
    }) { padding ->
        if (groups.isEmpty()) {
            ThEmptyState(
                title = "Your story is waiting",
                description = "Start with a moment you want to keep.",
                visual = { Icon(ThIcons.Timeline, null, Modifier.size(48.dp), tint = colors.roseDeep) },
                actionLabel = "Add your first moment", onAction = onAddEvent,
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            ) {
                groups.forEach { (month, moments) ->
                    item {
                        Text(
                            month?.format(monthFormat) ?: "Date unavailable",
                            modifier = Modifier.padding(top = spacing.space3, bottom = spacing.space2).semantics { heading() },
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        ThDivider()
                    }
                    items(moments, key = { it.event.id }) { moment ->
                        TimelineMoment(moment, dayFormat) { onEventClick(moment.event.id) }
                        ThDivider()
                    }
                    item { Spacer(Modifier.height(spacing.space4)) }
                }
            }
        }
    }
}

@Composable
private fun TimelineMoment(moment: DatedMoment, formatter: DateTimeFormatter, onClick: () -> Unit) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    Row(
        Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.Top,
    ) {
        Canvas(Modifier.padding(top = 6.dp).size(12.dp)) {
            drawCircle(color = colors.roseDeep, radius = 4.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            Text(
                moment.date?.format(formatter) ?: moment.event.eventDate,
                style = MaterialTheme.typography.labelMedium, color = colors.textSecondary,
            )
            Text(
                moment.event.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
            moment.event.description?.takeIf(String::isNotBlank)?.let {
                Text(it.replace('\n', ' '), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(ThIcons.ChevronRight, null, Modifier.size(20.dp), tint = colors.textSecondary)
    }
}
