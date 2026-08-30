package com.twohearts.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.MoodEntry

/**
 * MoodEntryData — mood entry display data.
 */
data class MoodEntryData(
    val value: String,
    val emoji: String,
    val label: String
)

/**
 * MoodMeta — mood metadata.
 */
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
        MoodEntryData("stressed", "😤", "Stressed")
    )

    fun getMood(value: String): MoodEntryData? {
        return moods.find { it.value == value }
    }
}

/**
 * MoodHome — mood check-in screen.
 *
 * matches legacy MoodHome with:
 * - Today's feeling card
 * - Quick selector
 * - Streak display
 * - History link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodHome(
    todayMood: MoodEntry?,
    recentMoods: List<MoodEntry>,
    onAddMood: () -> Unit,
    onViewHistory: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Header(
                title = "Mood",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMood,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log Mood"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's mood card
            item {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (todayMood != null) {
                    val moodData = MoodMeta.getMood(todayMood.moodValue)
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = moodData?.emoji ?: "😐",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = moodData?.label ?: todayMood.moodValue,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (todayMood.note != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = todayMood.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        onClick = onAddMood,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "How are you feeling today?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to log your mood",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Quick mood selector
            item {
                Text(
                    text = "Quick Select",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MoodMeta.moods) { mood ->
                        MoodQuickButton(
                            mood = mood,
                            onClick = { /* Will be implemented */ }
                        )
                    }
                }
            }

            // Recent moods
            item {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = onViewHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View History",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * MoodQuickButton — quick mood selection button.
 */
@Composable
fun MoodQuickButton(
    mood: MoodEntryData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mood.emoji,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = mood.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
