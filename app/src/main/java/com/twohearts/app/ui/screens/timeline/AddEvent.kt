package com.twohearts.app.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * AddEvent — timeline event creation screen.
 *
 * Matches legacy AddEvent with:
 * - Title input (required)
 * - Date picker (required)
 * - Description input (optional)
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvent(
    eventId: String? = null,
    initialTitle: String = "",
    initialDate: String = DateTimeHelper.todayLocal(),
    initialDescription: String = "",
    onSave: (title: String, eventDate: String, description: String?) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var eventDate by remember { mutableStateOf(initialDate) }
    var description by remember { mutableStateOf(initialDescription) }

    val isEditing = eventId != null

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Event" else "New Event",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    eventDate,
                                    description.takeIf { it.isNotBlank() }?.trim()
                                )
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
            // Title input
            Input(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                placeholder = "Enter event title",
                modifier = Modifier.fillMaxWidth()
            )

            // Date input
            Input(
                value = eventDate,
                onValueChange = { eventDate = it },
                label = "Date",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            // Description input
            Input(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                placeholder = "Add a description...",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}
