package com.twohearts.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input

/**
 * MoodEntryScreen — mood entry screen.
 *
 * matches legacy MoodEntryScreen with:
 * - Icon mood grid
 * - Optional note
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodEntryScreen(
    initialMood: String = "",
    initialNote: String = "",
    onSave: (moodValue: String, moodEmoji: String, note: String?) -> Unit,
    onBack: () -> Unit
) {
    var selectedMood by remember { mutableStateOf(initialMood) }
    var note by remember { mutableStateOf(initialNote) }

    Scaffold(
        topBar = {
            Header(
                title = "Log Mood",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (selectedMood.isNotBlank()) {
                                val moodData = MoodMeta.getMood(selectedMood)
                                if (moodData != null) {
                                    onSave(
                                        selectedMood,
                                        moodData.emoji,
                                        note.takeIf { it.isNotBlank() }?.trim()
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mood selection
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(400.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(MoodMeta.moods) { mood ->
                    MoodSelectCard(
                        mood = mood,
                        isSelected = selectedMood == mood.value,
                        onClick = { selectedMood = mood.value }
                    )
                }
            }

            // Note input
            Input(
                value = note,
                onValueChange = { note = it },
                label = "Add a note (optional)",
                placeholder = "How was your day?",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * MoodSelectCard — mood selection card.
 */
@Composable
fun MoodSelectCard(
    mood: MoodEntryData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mood.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mood.label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
