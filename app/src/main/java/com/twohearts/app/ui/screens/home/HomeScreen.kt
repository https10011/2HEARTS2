package com.twohearts.app.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.CardTone
import com.twohearts.app.ui.components.ProfileAvatar
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThQuietButton
import com.twohearts.app.ui.components.ThSectionHeader
import com.twohearts.app.ui.components.ThSurfaceCard
import com.twohearts.app.ui.decorations.DecorationPosition
import com.twohearts.app.ui.decorations.OnboardingArt
import com.twohearts.app.ui.decorations.ThDecoration
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.navigation.ShellSearchAction
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Home observes the local records that drive the shared portrait and daily highlights. */
@Composable
fun HomeScreen(
    relationshipService: RelationshipService,
    noteRepository: NoteRepository,
    reminderRepository: ReminderRepository,
    memoryRepository: MemoryRepository,
    moodEntryRepository: MoodEntryRepository,
    importantDateRepository: ImportantDateRepository,
    onNavigate: (String) -> Unit,
    yukiPresence: YukiPresence? = null,
) {
    val owner by remember(relationshipService) { relationshipService.observeOwner() }
        .collectAsStateWithLifecycle(initialValue = null)
    val partner by remember(relationshipService) { relationshipService.observePartner() }
        .collectAsStateWithLifecycle(initialValue = null)
    val relationship by remember(relationshipService) { relationshipService.observeRelationship() }
        .collectAsStateWithLifecycle(initialValue = null)
    val notes by remember(noteRepository) { noteRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val reminders by remember(reminderRepository) { reminderRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val memories by remember(memoryRepository) { memoryRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val moods by remember(moodEntryRepository) { moodEntryRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val dates by remember(importantDateRepository) { importantDateRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    HomeContent(
        owner = owner,
        partner = partner,
        relationship = relationship,
        notes = notes,
        reminders = reminders,
        memories = memories,
        moods = moods,
        dates = dates,
        yukiPresence = yukiPresence,
        onNavigate = onNavigate,
    )
}

/**
 * HomeContent — Home's pure view.
 *
 * Split from [HomeScreen] so the render harness and the tests can drive a
 * fully-controlled snapshot without a database, which is how the historical
 * "looks populated but is not wired" defect is kept from recurring.
 */
@Composable
fun HomeContent(
    owner: Profile?,
    partner: Profile?,
    relationship: CoupleRelationship?,
    notes: List<Note>,
    reminders: List<Reminder>,
    memories: List<Memory>,
    moods: List<MoodEntry>,
    dates: List<ImportantDate>,
    onNavigate: (String) -> Unit,
    today: java.time.LocalDate = java.time.LocalDate.now(),
    yukiPresence: YukiPresence? = null,
) {
    val data = remember(owner, partner, relationship, notes, reminders, memories, moods, dates, today) {
        buildHomeData(
            owner = owner,
            partner = partner,
            relationship = relationship,
            notes = notes,
            reminders = reminders,
            memories = memories,
            moods = moods,
            dates = dates,
            today = today,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = TwoHeartsTokens.Spacing.sectionGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeHero(data = data, onNavigate = onNavigate)

        Column(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter),
        ) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            UpcomingMomentSection(
                moment = data.upcomingMoment,
                onNavigate = onNavigate,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            RecentMemorySection(
                memory = recentMemory(data.memories),
                total = data.memories.size,
                onNavigate = onNavigate,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            NotesAndRemindersSection(
                notes = data.notes,
                reminders = data.reminders,
                today = today,
                onNavigate = onNavigate,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            MoodSection(
                mood = data.todayMood,
                onNavigate = onNavigate,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            YukiSection(
                presence = yukiPresence,
                onNavigate = onNavigate,
            )
        }
    }
}

@Composable
private fun HomeHero(data: HomeData, onNavigate: (String) -> Unit) {
    val colors = LocalTwoHeartsColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to colors.surfaceBlush,
                    0.5f to colors.surfaceWarm,
                    1f to MaterialTheme.colorScheme.background,
                )
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        ThDecoration(
            art = OnboardingArt.ROSE_LILY_TRAIL,
            position = DecorationPosition.TOP_END,
            size = 172.dp,
            alpha = 0.10f,
            offsetX = 18.dp,
            offsetY = 18.dp,
        )

        Column(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter)
                .padding(top = TwoHeartsTokens.Spacing.space2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandLogo(variant = BrandLogoVariant.MARK, size = 34)
                Spacer(Modifier.width(TwoHeartsTokens.Spacing.space2))
                Text(
                    homeGreeting(data.hour),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
                ThIconButton(onClick = { onNavigate(RoutePath.APP_NOTIFICATIONS) }, label = "Notifications") {
                    Icon(ThIcons.Bell, contentDescription = null, tint = colors.textSecondary)
                }
                ShellSearchAction { onNavigate(RoutePath.APP_SEARCH) }
            }

            Spacer(Modifier.height(TwoHeartsTokens.Spacing.space5))
            CouplePortrait(data.owner, data.partner, onClick = { onNavigate(RoutePath.APP_US) })
            Spacer(Modifier.height(TwoHeartsTokens.Spacing.space4))

            val days = data.daysTogether
            if (days != null && data.relationship != null) {
                TogetherCounter(days, data.relationship.startDate)
            } else if (data.owner != null) {
                ThQuietButton(
                    text = "Set your shared date",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_RELATIONSHIP) },
                )
            }
        }
    }
}

@Composable
private fun CouplePortrait(owner: Profile?, partner: Profile?, onClick: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.lg))
            .clickable(onClickLabel = "Open Us", role = Role.Button, onClick = onClick)
            .padding(vertical = TwoHeartsTokens.Spacing.space1),
    ) {
        val stackPortraits = maxWidth < 430.dp && LocalDensity.current.fontScale >= 1.5f
        if (stackPortraits) {
            Column(verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3)) {
                HomePortraitRow(owner, "You")
                HorizontalDivider(color = LocalTwoHeartsColors.current.borderSubtle)
                HomePortraitRow(partner, "Your special someone")
            }
        } else {
            val portraitSize = (maxWidth * 0.31f).value.toInt().coerceIn(72, 112)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                HomePortraitColumn(owner, "You", portraitSize, Modifier.weight(1f))
                Box(
                    modifier = Modifier.width(32.dp).height((portraitSize + 10).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        ThIcons.HeartFilled,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = LocalTwoHeartsColors.current.roseDeep,
                    )
                }
                HomePortraitColumn(partner, "Your special someone", portraitSize, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HomePortraitColumn(profile: Profile?, fallback: String, size: Int, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HomePortraitAvatar(profile, size)
        Spacer(Modifier.height(TwoHeartsTokens.Spacing.space3))
        HomePortraitName(profile, fallback, Modifier.fillMaxWidth(), TextAlign.Center)
    }
}

@Composable
private fun HomePortraitRow(profile: Profile?, fallback: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space4),
    ) {
        HomePortraitAvatar(profile, size = 78)
        HomePortraitName(profile, fallback, Modifier.weight(1f), TextAlign.Start)
    }
}

@Composable
private fun HomePortraitAvatar(profile: Profile?, size: Int) {
    val colors = LocalTwoHeartsColors.current
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(TwoHeartsTokens.Border.hairline, colors.borderStrong, CircleShape)
            .padding(5.dp),
    ) {
        ProfileAvatar(name = profile?.name.orEmpty(), photoUrl = profile?.photoRef, size = size)
    }
}

@Composable
private fun HomePortraitName(profile: Profile?, fallback: String, modifier: Modifier, textAlign: TextAlign) {
    Text(
        text = profile?.name.orEmpty().trim().ifEmpty { fallback },
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall.copy(lineBreak = LineBreak.Heading, hyphens = Hyphens.Auto),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = textAlign,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TogetherCounter(days: Int, startDate: String) {
    val duration = if (days == 1) "day" else "days"
    Text(
        text = "${formatDayCount(days)} $duration together",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(TwoHeartsTokens.Spacing.tightGap))
    Text(
        text = sinceLabel(startDate).replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.bodySmall,
        color = LocalTwoHeartsColors.current.textSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun UpcomingMomentSection(moment: UpcomingMoment?, onNavigate: (String) -> Unit) {
    val thColors = LocalTwoHeartsColors.current

    if (moment == null) {
        HomeActionRow(
            icon = ThIcons.Calendar,
            title = "Save a date that matters",
            detail = "An anniversary, birthday, or the day you met",
            onClick = { onNavigate(RoutePath.APP_IMPORTANT_DATES) },
            emphasized = true,
        )
        return
    }

    ThSurfaceCard(tone = CardTone.GOLD, onClick = { onNavigate(RoutePath.APP_IMPORTANT_DATES) }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3),
        ) {
            Icon(ThIcons.Calendar, null, Modifier.size(24.dp), tint = thColors.goldInk)
            Column(Modifier.weight(1f)) {
                ThEyebrow(text = "Coming up", color = thColors.goldInk)
                Spacer(Modifier.height(TwoHeartsTokens.Spacing.tightGap))
                Text(
                    text = moment.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${momentDateLabel(moment.dateKey)} · ${relativeDayLabel(moment.daysAway)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = thColors.textSecondary,
                )
            }
            Icon(ThIcons.ChevronRight, null, Modifier.size(20.dp), tint = thColors.goldInk)
        }
    }
}

@Composable
private fun NotesAndRemindersSection(
    notes: List<Note>,
    reminders: List<Reminder>,
    today: java.time.LocalDate,
    onNavigate: (String) -> Unit,
) {
    ThSectionHeader(title = "Notes & reminders")
    Spacer(Modifier.height(TwoHeartsTokens.Spacing.space1))
    HomeActionRow(
        icon = ThIcons.Note,
        title = "Notes",
        detail = notesTileDetail(notes),
        onClick = { onNavigate(RoutePath.APP_NOTES) },
    )
    HorizontalDivider(color = LocalTwoHeartsColors.current.borderSubtle)
    HomeActionRow(
        icon = ThIcons.Alarm,
        title = "Reminders",
        detail = remindersTileDetail(reminders, today),
        onClick = { onNavigate(RoutePath.APP_REMINDERS) },
    )
}

@Composable
private fun RecentMemorySection(
    memory: Memory?,
    total: Int,
    onNavigate: (String) -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current

    ThSectionHeader(
        title = "Memories",
        subtitle = when {
            total > 1 -> "$total memories saved"
            total == 1 -> "1 memory saved"
            else -> null
        },
        action = {
            ThQuietButton(
                onClick = { onNavigate(RoutePath.APP_MEMORIES) },
                text = "See all",
            )
        },
    )

    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.itemGap))

    if (memory == null) {
        HomeActionRow(
            icon = ThIcons.PhotoLibrary,
            title = "Keep your first memory",
            detail = "A photo, a day, a few words",
            onClick = { onNavigate(RoutePath.APP_MEMORIES_ADD) },
        )
        return
    }

    ThSurfaceCard(
        tone = CardTone.WARM,
        onClick = { onNavigate(RoutePath.APP_MEMORIES_DETAIL.replace(":memoryId", Uri.encode(memory.id))) },
    ) {
        ThEyebrow(text = DateTimeHelper.formatShortDisplay(memory.memoryDate))

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

        Text(
            text = memory.title,
            style = ThTextStyles.reading,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        val caption = memory.caption?.takeIf { it.isNotBlank() }
        if (caption != null) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = thColors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── 5. Today's mood ───────────────────────────────────────────────────────

/**
 * Today's check-in, or an invitation to make one.
 *
 * Expressive rather than analytical: a check-in shows the mood's own word and
 * any note the person wrote, in an accent-tinted row. No chart, no streak
 * statistic — those belong to the Mood feature.
 */
@Composable
private fun MoodSection(mood: MoodEntry?, onNavigate: (String) -> Unit) {
    ThSectionHeader(title = "Today's mood")

    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.itemGap))

    if (mood == null) {
        HomeActionRow(
            icon = ThIcons.Smile,
            title = "How are you feeling today?",
            detail = "Check in with yourself",
            onClick = { onNavigate(RoutePath.APP_MOOD) },
        )
        return
    }

    val thColors = LocalTwoHeartsColors.current
    val label = mood.moodValue.replaceFirstChar { it.uppercase() }

    ThSurfaceCard(
        tone = CardTone.BLUSH,
        onClick = { onNavigate(RoutePath.APP_MOOD) },
        modifier = Modifier.semantics { contentDescription = "Today's mood: $label" },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ThIcons.Smile,
                contentDescription = null,
                tint = thColors.roseDeep,
                modifier = Modifier.size(26.dp),
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val note = mood.note?.takeIf { it.isNotBlank() }
                if (note != null) {
                    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.tightGap))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = thColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ─── 6. Yuki ───────────────────────────────────────────────────────────────

/**
 * Yuki's presence on Home.
 *
 * The preview uses Yuki's saved mood and level when they are available.
 */
@Composable
private fun YukiSection(presence: YukiPresence?, onNavigate: (String) -> Unit) {
    val thColors = LocalTwoHeartsColors.current

    ThSurfaceCard(
        onClick = { onNavigate(RoutePath.APP_YUKI) },
        modifier = Modifier.semantics {
            contentDescription = "Yuki" + (presence?.let { ", ${it.statusLine}" } ?: "")
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(TwoHeartsTokens.Radius.md))
                    .background(thColors.surfaceWarm),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ThIcons.Cat,
                    contentDescription = null,
                    tint = thColors.goldInk,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = presence?.name ?: "Yuki",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.tightGap))
                Text(
                    text = presence?.statusLine ?: "Come say hello",
                    style = MaterialTheme.typography.bodySmall,
                    color = thColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (presence != null) {
                Text(
                    text = "Lv ${presence.level}",
                    style = MaterialTheme.typography.labelMedium,
                    color = thColors.textTertiary,
                )
            }

            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))

            Icon(
                imageVector = ThIcons.ChevronRight,
                contentDescription = null,
                tint = thColors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ─── Shared invitation ─────────────────────────────────────────────────────

/**
 * A compact, non-empty-state invitation.
 *
 * Used everywhere Home would otherwise show a large empty placeholder box.
 * It is a real, tappable row with an icon, a sentence and a route — so a
 * nearly-empty Home still looks intentional and still points somewhere.
 */
@Composable
private fun HomeActionRow(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)
    val surface = if (emphasized) Modifier.background(thColors.surfaceWarm) else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(surface)
            .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                horizontal = TwoHeartsTokens.Spacing.cardPadding,
                vertical = TwoHeartsTokens.Spacing.space4,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.tightGap))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = thColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
        Icon(
            imageVector = ThIcons.ChevronRight,
            contentDescription = null,
            tint = thColors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── Yuki presence ─────────────────────────────────────────────────────────

/**
 * A cheap, honest snapshot of Yuki for Home.
 *
 * @param name Yuki's display name.
 * @param statusLine what Yuki would say, derived from the persisted mood.
 * @param level Yuki's current level.
 */
data class YukiPresence(
    val name: String,
    val statusLine: String,
    val level: Int,
)
