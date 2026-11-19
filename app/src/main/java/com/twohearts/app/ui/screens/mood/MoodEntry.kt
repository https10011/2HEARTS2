package com.twohearts.app.ui.screens.mood

import com.twohearts.app.ui.components.ThIcons
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.ui.components.ThButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    var selectedMood by rememberSaveable { mutableStateOf(initialMood) }
    var note by rememberSaveable { mutableStateOf(initialNote) }
    val selected = MoodMeta.getMood(selectedMood)
    val save: () -> Unit = {
        selected?.let { onSave(it.value, it.emoji, note.trim().ifEmpty { null }) }
    }

    Scaffold(
        topBar = {
            Header(
                title = "Log Mood",
                onBack = onBack,
                actions = {
                    IconButton(onClick = save, enabled = selected != null) {
                        Icon(
                            imageVector = ThIcons.Check,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mood selection
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            MoodChoices(selected = selectedMood, onSelect = { selectedMood = it.value })

            // Note input
            Input(
                value = note,
                onValueChange = { note = it },
                label = "Add a note (optional)",
                placeholder = "How was your day?",
                multiline = true,
                modifier = Modifier.fillMaxWidth()
            )
            ThButton(onClick = save, enabled = selected != null, full = true, text = "Save mood")
        }
    }
}
