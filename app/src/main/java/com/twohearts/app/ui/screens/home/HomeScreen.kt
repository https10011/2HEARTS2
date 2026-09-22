package com.twohearts.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.twohearts.app.ui.components.ThQuietButton
import com.twohearts.app.ui.components.ThSectionHeader
import com.twohearts.app.ui.components.ThSurfaceCard
import com.twohearts.app.ui.decorations.DecorationPosition
import com.twohearts.app.ui.decorations.OnboardingArt
import com.twohearts.app.ui.decorations.ThDecoration
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * HomeScreen — the emotional entry point into TwoHearts.
 *
 * ## What was wrong before
 *
 * The migrated Home was a 16dp column holding a placeholder brand mark, two
 * avatars, a greeting, a day count and four identical equal-weight feature
 * cards (Notes, Reminders, Us, Yuki) using emoji as chrome. Phase 0 measured
 * it: content ended at ~43% of the viewport, every card carried the same
 * emphasis, and — most importantly — the **seeded render was byte-identical
 * to the unseeded one**. Home was not reading the relationship data it
 * claimed to show.
 *
 * ## What it does now
 *
 * It answers one question — *"what is our space like today?"* — with a
 * deliberate hierarchy instead of a feature directory:
 *
 *  1. **Our space** — the brand mark, the two people, the greeting, and the
 *     day counter as the emotional focal point. Time together is phrased as
 *     lived duration ("1 year and 8 months"), never as a bare analytics
 *     figure.
 *  2. **What is coming** — the single most relevant upcoming date, or a calm
 *     invitation when nothing is saved yet.
 *  3. **Waiting for you** — Notes and Reminders, deliberately high on the
 *     screen because that is where the product's own feedback asked for
 *     them, each carrying its real next item rather than a bare count.
 *  4. **Recent memory** — the latest real memory, as a keepsake composition.
 *  5. **Today's mood** — the actual check-in, or an invitation to make one.
 *  6. **Yuki** — a compact character-led row, because Home is the only route
 *     into Yuki outside the archived game redirects.
 *
 * Every value comes from a repository. Nothing is fabricated: an absent
 * memory, note, reminder, date or mood renders as an invitation, and the
 * pure derivation lives in [HomePresentation] so it can be tested without a
 * device.
 *
 * @param onNavigate route callback into the shell's navigation graph.
 * @param yukiPresence an optional, cheap read of Yuki's persisted mood/level,
 *   injected so this screen never touches SharedPreferences itself. Null
 *   renders the row without a mood claim.
 */
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
    val owner by relationshipService.observeOwner().collectAsState(initial = null)
    val partner by relationshipService.observePartner().collectAsState(initial = null)
    val relationship by relationshipService.observeRelationship().collectAsState(initial = null)
    val notes by noteRepository.observeAll().collectAsState(initial = emptyList())
    val reminders by reminderRepository.observeAll().collectAsState(initial = emptyList())
    val memories by memoryRepository.observeAll().collectAsState(initial = emptyList())
    val moods by moodEntryRepository.observeAll().collectAsState(initial = emptyList())
    val dates by importantDateRepository.observeAll().collectAsState(initial = emptyList())

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
    ) {
        HomeHero(data = data, onNavigate = onNavigate)

        Column(
            modifier = Modifier.padding(horizontal = TwoHeartsTokens.Spacing.screenGutter),
        ) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            UpcomingMomentSection(
                moment = data.upcomingMoment,
                onNavigate = onNavigate,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            WaitingForYouSection(
                notes = data.notes,
                reminders = data.reminders,
                today = today,
                onNavigate = onNavigate,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

            RecentMemorySection(
                memory = recentMemory(data.memories),
                total = data.memories.size,
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

// ─── 1. Our space ──────────────────────────────────────────────────────────

/**
 * The opening band.
 *
 * Deliberately a full-bleed tinted *band* rather than a card: the top of Home
 * is where the relationship lives, and putting it inside a bordered rectangle
 * would make the couple's own space read as one more widget. It is kept
 * compact — brand mark, pair, greeting, counter — so the first viewport still
 * reaches Notes and Reminders.
 */
@Composable
private fun HomeHero(data: HomeData, onNavigate: (String) -> Unit) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(
        bottomStart = TwoHeartsTokens.Radius.xl,
        bottomEnd = TwoHeartsTokens.Radius.xl,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(thColors.surfaceBlush, MaterialTheme.colorScheme.background)
                )
            ),
    ) {
        // One quiet botanical accent, cropped by the corner. Present so the
        // band belongs to the same world as onboarding; restrained so it never
        // competes with the names.
        ThDecoration(
            art = OnboardingArt.ROSE_LILY_TRAIL,
            position = DecorationPosition.TOP_END,
            size = 132.dp,
            alpha = 0.12f,
            offsetX = 18.dp,
            offsetY = (-10).dp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter)
                .padding(
                    top = TwoHeartsTokens.Spacing.space5,
                    bottom = TwoHeartsTokens.Spacing.space6,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandLogo(variant = BrandLogoVariant.MARK, size = 40)

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

            CoupleRow(
                ownerName = data.owner?.name,
                partnerName = data.partner?.name,
                ownerPhotoRef = data.owner?.photoRef,
                partnerPhotoRef = data.partner?.photoRef,
                onClick = { onNavigate(RoutePath.APP_US) },
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

            Text(
                text = homeGreetingLine(data.hour, data.owner?.name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))

            Text(
                text = homeIdentityLine(data.owner?.name, data.partner?.name),
                style = MaterialTheme.typography.bodyMedium,
                color = thColors.textSecondary,
                textAlign = TextAlign.Center,
            )

            if (data.hasRelationship) {
                Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))
                TogetherCounter(
                    days = data.daysTogether!!,
                    startDate = data.relationship!!.startDate,
                )
            }
        }
    }
}

/**
 * The two people, side by side.
 *
 * A single tappable row into the couple's space, rather than a button that
 * happens to contain avatars. The heart between them is the canonical filled
 * heart from the icon set — the one place a filled heart is "meaningful"
 * rather than destructive — so no emoji chrome is reintroduced.
 */
@Composable
private fun CoupleRow(
    ownerName: String?,
    partnerName: String?,
    ownerPhotoRef: String?,
    partnerPhotoRef: String?,
    onClick: () -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current
    val hasTwo = !ownerName.isNullOrBlank() && !partnerName.isNullOrBlank()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.lg))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                horizontal = TwoHeartsTokens.Spacing.space2,
                vertical = TwoHeartsTokens.Spacing.space1,
            )
            .semantics { contentDescription = "Open your relationship space" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        PersonColumn(name = ownerName, photoRef = ownerPhotoRef)

        if (hasTwo) {
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
            Icon(
                imageVector = ThIcons.HeartFilled,
                contentDescription = null,
                tint = thColors.burgundy,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
        } else {
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space4))
        }

        PersonColumn(name = partnerName, photoRef = partnerPhotoRef)
    }
}

@Composable
private fun PersonColumn(name: String?, photoRef: String?) {
    val display = name?.takeIf { it.isNotBlank() } ?: "Your special someone"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileAvatar(
            name = display,
            photoUrl = photoRef,
            size = 60,
        )
        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
        Text(
            text = display,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The relationship counter.
 *
 * The directive is explicit that this must not read as a generic statistic,
 * so the day count is presented as a large serif numeral with the unit beside
 * it, followed by the same span expressed as lived duration and the date it
 * began. The *days* figure reuses the app's existing
 * [DateTimeHelper.daysSinceStartDate] rule — no new calculation is invented.
 */
@Composable
private fun TogetherCounter(days: Int, startDate: String) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.lg)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .border(TwoHeartsTokens.Border.hairline, thColors.borderSubtle, shape)
            .padding(
                horizontal = TwoHeartsTokens.Spacing.space4,
                vertical = TwoHeartsTokens.Spacing.space4,
            )
            .semantics {
                contentDescription = "Together with your special someone for $days days"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ThEyebrow(text = "Together for")

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatDayCount(days),
                style = ThTextStyles.numeral,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
            Text(
                text = if (days == 1) "day" else "days",
                style = MaterialTheme.typography.titleMedium,
                color = thColors.textSecondary,
                modifier = Modifier.padding(bottom = TwoHeartsTokens.Spacing.space2),
            )
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))

        Text(
            text = "${togetherPhrase(days)} · ${sinceLabel(startDate)}",
            style = MaterialTheme.typography.bodySmall,
            color = thColors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── 2. What is coming ─────────────────────────────────────────────────────

/**
 * The single most relevant upcoming moment.
 *
 * One card, not a calendar: the directive asks for the most relevant date, a
 * readable date, the title distinguished from the date, and an obvious route
 * into the feature. When nothing is saved the section becomes a quiet
 * invitation rather than an empty box.
 */
@Composable
private fun UpcomingMomentSection(moment: UpcomingMoment?, onNavigate: (String) -> Unit) {
    val thColors = LocalTwoHeartsColors.current

    if (moment == null) {
        InvitationRow(
            icon = ThIcons.Calendar,
            title = "Save a date that matters",
            detail = "Anniversaries, birthdays, the day you met",
            onClick = { onNavigate(RoutePath.APP_IMPORTANT_DATES) },
        )
        return
    }

    ThSurfaceCard(
        tone = CardTone.GOLD,
        onClick = { onNavigate(RoutePath.APP_IMPORTANT_DATES) },
        modifier = Modifier.semantics {
            contentDescription = "${moment.title}, ${relativeDayLabel(moment.daysAway)}"
        },
    ) {
        ThEyebrow(text = "Coming up", color = thColors.goldInk)

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

        Text(
            text = moment.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = relativeDayLabel(moment.daysAway),
                style = MaterialTheme.typography.titleMedium,
                color = thColors.goldInk,
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
            Text(
                text = "·",
                style = MaterialTheme.typography.bodyMedium,
                color = thColors.textTertiary,
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
            Text(
                text = momentDateLabel(moment.dateKey),
                style = MaterialTheme.typography.bodyMedium,
                color = thColors.textSecondary,
            )
        }
    }
}

// ─── 3. Waiting for you ────────────────────────────────────────────────────

/**
 * Notes and Reminders, positioned high on Home.
 *
 * This ordering is deliberate: the product's own feedback said Notes and
 * Reminders were buried below the recent-memory area. They are paired rather
 * than stacked because each is a small, actionable thing, and each shows its
 * real next item so neither is a bare count.
 */
@Composable
private fun WaitingForYouSection(
    notes: List<Note>,
    reminders: List<Reminder>,
    today: java.time.LocalDate,
    onNavigate: (String) -> Unit,
) {
    ThSectionHeader(title = "Waiting for you")

    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.itemGap))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.itemGap),
    ) {
        ActionTile(
            icon = ThIcons.Note,
            label = "Notes",
            count = countLabel(notes.size, "note", "notes"),
            detail = notesTileDetail(notes),
            onClick = { onNavigate(RoutePath.APP_NOTES) },
            modifier = Modifier.weight(1f),
        )
        ActionTile(
            icon = ThIcons.Alarm,
            label = "Reminders",
            count = countLabel(
                reminders.count { it.status == "pending" },
                "reminder",
                "reminders",
            ),
            detail = remindersTileDetail(reminders, today),
            onClick = { onNavigate(RoutePath.APP_REMINDERS) },
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * One actionable tile.
 *
 * Cards are justified here — these *are* things to do — but the treatment
 * carries real content (an icon, a count when there is one, and the actual
 * next item) so they do not read as two identical rounded rectangles.
 */
@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    count: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current

    ThSurfaceCard(
        modifier = modifier.semantics {
            contentDescription = "$label. $detail"
        },
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )

        if (count.isNotEmpty()) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.tightGap))
            Text(
                text = count,
                style = MaterialTheme.typography.labelMedium,
                color = thColors.textTertiary,
            )
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = thColors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── 4. Recent memory ──────────────────────────────────────────────────────

/**
 * The most recent real memory, as a keepsake rather than a list row.
 *
 * The app currently has no image decoder wired for memory media anywhere, so
 * Home deliberately does not pretend to show a photo: it presents the memory's
 * own words in a paper-like composition with the count as context. Rendering
 * memory imagery belongs to the Memories phase and is recorded as deferred.
 */
@Composable
private fun RecentMemorySection(
    memory: Memory?,
    total: Int,
    onNavigate: (String) -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current

    ThSectionHeader(
        title = "From your memories",
        subtitle = when {
            total > 1 -> "$total memories saved"
            total == 1 -> "1 memory saved"
            else -> null
        },
        action = {
            ThQuietButton(
                onClick = { onNavigate(RoutePath.APP_MEMORIES) },
                text = if (memory == null) "Add" else "See all",
            )
        },
    )

    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.itemGap))

    if (memory == null) {
        InvitationRow(
            icon = ThIcons.PhotoLibrary,
            title = "Keep your first memory",
            detail = "A photo, a day, a few words",
            onClick = { onNavigate(RoutePath.APP_MEMORIES) },
        )
        return
    }

    ThSurfaceCard(
        tone = CardTone.WARM,
        onClick = { onNavigate(RoutePath.APP_MEMORIES) },
        modifier = Modifier.semantics {
            contentDescription = "Memory: ${memory.title}"
        },
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
        InvitationRow(
            icon = ThIcons.Smile,
            title = "How are you feeling today?",
            detail = "A one-tap check-in",
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
 * Yuki is a character, not a feature button, so this is a character-led row
 * that speaks in Yuki's own voice using real persisted mood and level. It
 * exists because Home is the only route into Yuki outside the archived game
 * redirects — removing it would strand the character.
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
private fun InvitationRow(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.lg)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.Transparent)
            .border(TwoHeartsTokens.Border.hairline, thColors.borderSubtle, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                horizontal = TwoHeartsTokens.Spacing.cardPadding,
                vertical = TwoHeartsTokens.Spacing.space4,
            )
            .semantics { contentDescription = "$title. $detail" },
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
