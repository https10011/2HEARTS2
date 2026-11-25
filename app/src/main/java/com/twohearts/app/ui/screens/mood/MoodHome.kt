package com.twohearts.app.ui.screens.mood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate

data class MoodEntryData(val value: String, val emoji: String, val label: String)

object MoodMeta {
    val moods = listOf(
        MoodEntryData("happy", "😊", "Happy"),
        MoodEntryData("love", "❤️", "Love"),
        MoodEntryData("excited", "🎉", "Excited"),
        MoodEntryData("calm", "😌", "Calm"),
        MoodEntryData("grateful", "🙏", "Grateful"),
        MoodEntryData("neutral", "😐", "Neutral"),
        MoodEntryData("tired", "😴", "Tired"),
        MoodEntryData("sad", "😢", "Sad"),
        MoodEntryData("anxious", "😰", "Anxious"),
        MoodEntryData("stressed", "😤", "Stressed"),
    )

    fun getMood(value: String): MoodEntryData? = moods.firstOrNull { it.value == value }
}

internal fun moodLabel(entry: MoodEntry): String =
    MoodMeta.getMood(entry.moodValue)?.label ?: entry.moodValue.replaceFirstChar(Char::titlecase).ifBlank { "Saved mood" }

internal fun MoodEntry.belongsToOwner(ownerId: String?): Boolean = profileId == ownerId || profileId == "owner"

@Composable
fun MoodHome(
    todayMood: MoodEntry?,
    recentMoods: List<MoodEntry>,
    onAddMood: () -> Unit,
    onViewHistory: () -> Unit,
    onBack: () -> Unit,
    onQuickMood: (MoodEntryData) -> Unit = {},
    updating: Boolean = false,
    errorMessage: String? = null,
    today: LocalDate = LocalDate.now(),
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Scaffold(topBar = { ThTopBar("Mood", onBack = onBack) }) { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            item {
                Surface(color = colors.surfaceBlush, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        ThEyebrow("Today · ${DateTimeHelper.formatShortDisplay(today.toString())}", color = colors.roseDeep)
                        if (todayMood == null) {
                            Text("How are you feeling?", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                            ) {
                                val emoji = todayMood.moodEmoji.ifBlank { MoodMeta.getMood(todayMood.moodValue)?.emoji.orEmpty() }
                                if (emoji.isNotEmpty()) Text(emoji, Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.headlineMedium)
                                Text(moodLabel(todayMood), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                            }
                            todayMood.note?.takeIf(String::isNotBlank)?.let {
                                Text(it, style = ThTextStyles.reading, color = colors.textSecondary, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        ThButton(
                            onAddMood, text = if (todayMood == null) "Check in with a note" else "Edit today’s check-in",
                            variant = ButtonVariant.GHOST, icon = ThIcons.Edit, enabled = !updating,
                        )
                    }
                }
            }
            errorMessage?.let { message -> item { ThStatusBanner(StatusVariant.ERROR, message) } }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Text("Quick check-in", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    MoodChoices(todayMood?.moodValue, onQuickMood, compact = true, enabled = !updating)
                }
            }
            item {
                ThDivider()
                ThButton(onViewHistory, text = "Mood history", variant = ButtonVariant.GHOST, icon = ThIcons.ChevronRight)
            }
            items(recentMoods.filter { it.entryDate != today.toString() }.take(3), key = { it.id }) { entry ->
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                    Text(moodLabel(entry), style = MaterialTheme.typography.titleMedium)
                    Text(DateTimeHelper.formatShortDisplay(entry.entryDate), color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                    ThDivider()
                }
            }
        }
    }
}
