package com.twohearts.app.ui.screens.us

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.components.CardTone
import com.twohearts.app.ui.components.ProfileAvatar
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThSurfaceCard
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.home.UpcomingMoment
import com.twohearts.app.ui.screens.home.countLabel
import com.twohearts.app.ui.screens.home.formatDayCount
import com.twohearts.app.ui.screens.home.momentDateLabel
import com.twohearts.app.ui.screens.home.relativeDayLabel
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate

@Composable
fun UsScreen(
    relationshipService: RelationshipService,
    importantDateRepository: ImportantDateRepository,
    memoryRepository: MemoryRepository,
    timelineEventRepository: TimelineEventRepository,
    placeRepository: PlaceRepository,
    moodEntryRepository: MoodEntryRepository,
    onNavigate: (String) -> Unit,
) {
    val owner by remember(relationshipService) { relationshipService.observeOwner() }
        .collectAsStateWithLifecycle(initialValue = null)
    val partner by remember(relationshipService) { relationshipService.observePartner() }
        .collectAsStateWithLifecycle(initialValue = null)
    val relationship by remember(relationshipService) { relationshipService.observeRelationship() }
        .collectAsStateWithLifecycle(initialValue = null)
    val dates by remember(importantDateRepository) { importantDateRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val memories by remember(memoryRepository) { memoryRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val events by remember(timelineEventRepository) { timelineEventRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val places by remember(placeRepository) { placeRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val moods by remember(moodEntryRepository) { moodEntryRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val today = LocalDate.now()
    val highlights = remember(relationship, dates, memories, events, places, moods, today) {
        usHighlights(relationship, dates, memories, events, places, moods, today)
    }
    UsContent(owner, partner, highlights, onNavigate)
}

@Composable
internal fun UsContent(
    owner: Profile?,
    partner: Profile?,
    highlights: UsHighlights,
    onNavigate: (String) -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                .padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space6),
        ) {
            Text(
                "Us",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            RelationshipPortrait(owner, partner, highlights)

            Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                HubHeading(if (highlights.nextDate == null) "Important dates" else "Coming up")
                NextDate(highlights.nextDate) { onNavigate(RoutePath.APP_IMPORTANT_DATES) }
            }

            Column {
                HubHeading("Our story", Modifier.padding(bottom = spacing.space2))
                val memory = highlights.memory
                if (memory == null) {
                    ThSurfaceCard(tone = CardTone.WARM) {
                        HubRow(
                            title = "Add a memory",
                            detail = "Save a moment you want to remember.",
                            icon = ThIcons.Image,
                            onClick = { onNavigate(RoutePath.APP_MEMORIES_ADD) },
                        )
                    }
                } else {
                    ThSurfaceCard(
                        tone = CardTone.WARM,
                        onClick = { onNavigate("${RoutePath.APP_MEMORIES}/${Uri.encode(memory.id)}") },
                    ) {
                        Text("Latest memory", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                        Text(
                            momentDateLabel(memory.memoryDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = spacing.space1),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = spacing.space2),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                        ) {
                            Text(
                                memory.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(ThIcons.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                        }
                    }
                }
                HubRow(
                    title = "Memories",
                    detail = countLabel(highlights.memoryCount, "memory saved", "memories saved")
                        .takeIf { highlights.memoryCount > 0 },
                    icon = ThIcons.PhotoLibrary,
                    onClick = { onNavigate(RoutePath.APP_MEMORIES) },
                )
                ThDivider()
                val event = highlights.timelineEvent
                HubRow(
                    title = "Timeline",
                    detail = event?.let { "${momentDateLabel(it.eventDate)} · ${it.title}" }
                        ?: "Mark a moment in your story.",
                    icon = ThIcons.Timeline,
                    onClick = { onNavigate(RoutePath.APP_TIMELINE) },
                )
            }

            Column {
                HubHeading("Our world", Modifier.padding(bottom = spacing.space2))
                val place = highlights.place
                HubRow(
                    title = "Places",
                    detail = place?.let {
                        "${it.name} · ${countLabel(highlights.placeCount, "place saved", "places saved")}"
                    } ?: "Keep the places that matter to you.",
                    icon = ThIcons.MapPin,
                    onClick = { onNavigate(RoutePath.APP_PLACES) },
                )
                ThDivider()
                HubRow(
                    title = "Mood",
                    detail = highlights.todayMood?.let { "Today · ${it.moodValue.replaceFirstChar(Char::uppercaseChar)}" }
                        ?: "A little space to check in.",
                    icon = ThIcons.Smile,
                    onClick = { onNavigate(RoutePath.APP_MOOD) },
                )
            }

            Column {
                HubHeading("Private", Modifier.padding(bottom = spacing.space2))
                HubRow("Period tracker", icon = ThIcons.CalendarMonth) { onNavigate(RoutePath.APP_PERIOD) }
                ThDivider()
                HubRow("Vault", icon = ThIcons.Lock) { onNavigate(RoutePath.APP_VAULT) }
            }
        }
    }
}

@Composable
private fun RelationshipPortrait(owner: Profile?, partner: Profile?, highlights: UsHighlights) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    ThSurfaceCard(tone = CardTone.BLUSH) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            Portrait(owner, "You", Modifier.weight(1f))
            Icon(
                ThIcons.Heart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = spacing.space4),
            )
            Portrait(partner, "Your special someone", Modifier.weight(1f))
        }
        ThDivider(Modifier.padding(vertical = spacing.space4))
        val days = highlights.daysTogether
        val start = highlights.startDate
        val title = when {
            days == 1 -> "1 day together"
            days != null -> "${formatDayCount(days)} days together"
            start != null -> "Your story begins soon"
            else -> "Relationship date"
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            when {
                start == null -> "Your shared date will appear here."
                days == null -> "Begins ${DateTimeHelper.formatDisplay(start.toString())}"
                else -> "Since ${DateTimeHelper.formatDisplay(start.toString())}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = spacing.space1),
        )
    }
}

@Composable
private fun Portrait(profile: Profile?, fallback: String, modifier: Modifier) {
    val name = profile?.name?.takeIf(String::isNotBlank)
    val colors = LocalTwoHeartsColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
    ) {
        if (name != null) {
            ProfileAvatar(name, profile.photoRef, size = 56)
        } else {
            Box(
                Modifier.size(56.dp)
                    .background(colors.surfaceWarm, CircleShape)
                    .border(TwoHeartsTokens.Border.hairline, colors.borderSubtle, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(ThIcons.Person, contentDescription = null, tint = colors.textSecondary)
            }
        }
        Text(
            name ?: fallback,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NextDate(moment: UpcomingMoment?, onClick: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    ThSurfaceCard(tone = CardTone.GOLD, onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.CenterVertically) {
            Icon(ThIcons.Calendar, contentDescription = null, tint = colors.goldInk)
            Column(Modifier.weight(1f)) {
                Text(
                    moment?.title ?: "Save an important date",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    moment?.let { "${relativeDayLabel(it.daysAway)} · ${momentDateLabel(it.dateKey)}" }
                        ?: "Keep birthdays, anniversaries, and milestones close.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = spacing.space1),
                )
            }
            Icon(ThIcons.ChevronRight, contentDescription = null, tint = colors.textSecondary)
        }
    }
}

@Composable
private fun HubHeading(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
private fun HubRow(title: String, detail: String? = null, icon: ImageVector, onClick: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).background(colors.surfaceBlush, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (detail != null) {
                Text(
                    detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = spacing.space1),
                )
            }
        }
        Icon(ThIcons.ChevronRight, contentDescription = null, tint = colors.textSecondary)
    }
}
