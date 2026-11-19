package com.twohearts.app.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineHome(events: List<TimelineEvent>, onEventClick: (String) -> Unit, onAddEvent: () -> Unit, onBack: () -> Unit) {
    val colors = LocalTwoHeartsColors.current
    val ordered = events.sortedByDescending { runCatching { LocalDate.parse(it.eventDate) }.getOrNull() }
    Scaffold(
        topBar = { Header(title = "Our timeline", onBack = onBack) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAddEvent, icon = { Icon(ThIcons.Add, null) }, text = { Text("Add a moment") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) },
    ) { padding ->
        if (events.isEmpty()) TimelineEmpty(onAddEvent, Modifier.padding(padding)) else LazyColumn(
            Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
        ) {
            item {
                Text("The story of us", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp)); Text("Every beginning, turning point, and ordinary day worth remembering.", color = colors.textSecondary)
                Spacer(Modifier.height(24.dp))
            }
            itemsIndexed(ordered, key = { _, event -> event.id }) { index, event ->
                TimelineMoment(event, index == 0, index == ordered.lastIndex) { onEventClick(event.id) }
            }
        }
    }
}

@Composable
private fun TimelineEmpty(onAdd: () -> Unit, modifier: Modifier) {
    val colors = LocalTwoHeartsColors.current
    Column(modifier.fillMaxSize().padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(88.dp).clip(CircleShape).background(colors.surfaceBlush), contentAlignment = Alignment.Center) { Icon(ThIcons.Timeline, null, tint = colors.burgundy, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(22.dp)); Text("Your story is waiting", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp)); Text("Begin with the moment that feels like the start, then add the chapters that followed.", color = colors.textSecondary)
        Spacer(Modifier.height(24.dp)); Button(onClick = onAdd) { Icon(ThIcons.Add, null); Spacer(Modifier.width(8.dp)); Text("Add your first moment") }
    }
}

@Composable
private fun TimelineMoment(event: TimelineEvent, first: Boolean, last: Boolean, onClick: () -> Unit) {
    val colors = LocalTwoHeartsColors.current
    val date = runCatching { LocalDate.parse(event.eventDate) }.getOrNull()
    Row(Modifier.fillMaxWidth().heightIn(min = 142.dp)) {
        Column(Modifier.width(38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!first) Box(Modifier.width(2.dp).height(18.dp).background(colors.dustyRose)) else Spacer(Modifier.height(18.dp))
            Box(Modifier.size(16.dp).clip(CircleShape).background(if (first) colors.gold else colors.burgundy))
            if (!last) Box(Modifier.width(2.dp).height(108.dp).background(colors.dustyRose))
        }
        Surface(Modifier.weight(1f).padding(bottom = 18.dp).clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), color = if (first) colors.surfaceBlush else colors.surfaceElevated, tonalElevation = 1.dp) {
            Column(Modifier.padding(18.dp)) {
                Text(date?.let { "${it.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${it.dayOfMonth}, ${it.year}" } ?: event.eventDate, style = MaterialTheme.typography.labelMedium, color = colors.roseDeep)
                Spacer(Modifier.height(7.dp)); Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                event.description?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(7.dp)); Text(it, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis) }
            }
        }
    }
}
