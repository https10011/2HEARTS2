package com.twohearts.app.ui.screens.mood

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThSectionTabs
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

internal enum class MoodWindow(val label: String) { WEEK("7 days"), MONTH("Month"), ALL("All") }

private fun moodDate(entry: MoodEntry): LocalDate? = runCatching { LocalDate.parse(entry.entryDate) }.getOrNull()

internal fun moodHistoryGroups(
    moods: List<MoodEntry>, window: MoodWindow, today: LocalDate,
): Map<YearMonth?, List<MoodEntry>> {
    val first = when (window) {
        MoodWindow.WEEK -> today.minusDays(6)
        MoodWindow.MONTH -> today.minusMonths(1).plusDays(1)
        MoodWindow.ALL -> null
    }
    return moods.map { it to moodDate(it) }
        .filter { (_, date) -> first == null || (date != null && date in first..today) }
        .sortedWith(compareByDescending<Pair<MoodEntry, LocalDate?>> { it.second }.thenByDescending { it.first.updatedAt }.thenBy { it.first.id })
        .groupBy({ (_, date) -> date?.let(YearMonth::from) }, { (entry, _) -> entry })
}

@Composable
fun MoodHistory(
    moods: List<MoodEntry>,
    onBack: () -> Unit,
    ownerId: String? = null,
    onEdit: ((MoodEntry) -> Unit)? = null,
    today: LocalDate = LocalDate.now(),
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val window = MoodWindow.entries[tab]
    val groups = remember(moods, window, today) { moodHistoryGroups(moods, window, today) }
    val spacing = TwoHeartsTokens.Spacing

    Scaffold(topBar = { ThTopBar("Mood history", onBack = onBack) }) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            ThSectionTabs(MoodWindow.entries.map { it.label }, tab, { tab = it })
            key(window) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space3),
                    verticalArrangement = Arrangement.spacedBy(spacing.space2),
                ) {
                    if (groups.isEmpty()) item {
                        ThEmptyState(
                            title = if (moods.isEmpty()) "No check-ins yet" else "No check-ins in this period",
                            description = if (moods.isEmpty()) "Your saved moods will appear here." else null,
                            actionLabel = "Show all".takeIf { moods.isNotEmpty() },
                            onAction = { tab = MoodWindow.ALL.ordinal },
                        )
                    }
                    for ((month, entries) in groups) {
                        item {
                            Text(
                                month?.format(monthTitle) ?: "Check these dates", style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = spacing.space2).semantics { heading() },
                            )
                        }
                        items(entries, key = { it.id }) { entry ->
                            val own = entry.belongsToOwner(ownerId)
                            MoodHistoryRow(entry, own, onEdit?.takeIf { own }?.let { { it(entry) } })
                            ThDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodHistoryRow(entry: MoodEntry, own: Boolean, edit: (() -> Unit)?) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Row(Modifier.padding(vertical = spacing.space2), horizontalArrangement = Arrangement.spacedBy(spacing.space3)) {
        val emoji = entry.moodEmoji.ifBlank { MoodMeta.getMood(entry.moodValue)?.emoji.orEmpty() }
        if (emoji.isNotEmpty()) Text(emoji, Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.titleLarge)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            val date = moodDate(entry)?.let { DateTimeHelper.formatShortDisplay(it.toString()) } ?: "Date unavailable"
            ThEyebrow(if (own) date else "$date · Other profile", color = colors.roseDeep)
            Text(moodLabel(entry), style = MaterialTheme.typography.titleMedium)
            entry.note?.takeIf(String::isNotBlank)?.let { Text(it, color = colors.textSecondary) }
            if (edit != null) ThButton(edit, text = "Edit check-in", variant = ButtonVariant.GHOST, icon = ThIcons.Edit)
        }
    }
}

private val monthTitle = DateTimeFormatter.ofPattern("MMMM yyyy")
