package com.twohearts.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * MoodHistory — mood history screen.
 *
 * matches legacy MoodHistory with:
 * - Week/month/all-time distribution
 * - Mood list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodHistory(
    moods: List<MoodEntry>,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Week", "Month", "All Time")

    val filteredMoods = remember(moods, selectedTab) {
        val now = java.time.LocalDate.now()
        when (selectedTab) {
            0 -> moods.filter {
                val entryDate = try {
                    java.time.LocalDate.parse(it.entryDate)
                } catch (e: Exception) { null }
                entryDate != null && entryDate.isAfter(now.minusWeeks(1))
            }
            1 -> moods.filter {
                val entryDate = try {
                    java.time.LocalDate.parse(it.entryDate)
                } catch (e: Exception) { null }
                entryDate != null && entryDate.isAfter(now.minusMonths(1))
            }
            else -> moods
        }
    }

    Scaffold(
        topBar = {
            Header(
                title = "Mood History",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (filteredMoods.isEmpty()) {
                EmptyState(
                    title = "No Moods Logged",
                    message = "Start tracking your mood to see history"
                )
            } else {
                // Distribution summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Mood Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${filteredMoods.size} entries",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Mood list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMoods) { mood ->
                        MoodHistoryItem(mood = mood)
                    }
                }
            }
        }
    }
}

/**
 * MoodHistoryItem — mood history list item.
 */
@Composable
fun MoodHistoryItem(
    mood: MoodEntry,
    modifier: Modifier = Modifier
) {
    val moodData = MoodMeta.getMood(mood.moodValue)

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = moodData?.emoji ?: "😐",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Mood info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moodData?.label ?: mood.moodValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (mood.note != null) {
                    Text(
                        text = mood.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // Date
            Text(
                text = DateTimeHelper.formatShortDisplay(mood.entryDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
