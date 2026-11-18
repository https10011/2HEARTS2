package com.twohearts.app.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import java.time.LocalDate

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
    var attempted by remember { mutableStateOf(false) }
    val validDate = runCatching { LocalDate.parse(eventDate) }.isSuccess

    val isEditing = eventId != null

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Event" else "New Event",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            attempted = true
                            if (title.isNotBlank() && validDate) {
                                onSave(
                                    title.trim(),
                                    eventDate,
                                    description.takeIf { it.isNotBlank() }?.trim()
                                )
                            }
                        }
                    ) {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val colors = LocalTwoHeartsColors.current
            Text("Add a chapter to your story", style = MaterialTheme.typography.titleLarge)
            Text("Choose the date it happened and keep the details you still remember.", color = colors.textSecondary)
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
                    .heightIn(min = 190.dp),
                multiline = true,
            )
            if (attempted && title.isBlank()) Text("Give this moment a title.", color = MaterialTheme.colorScheme.error)
            if (attempted && !validDate) Text("Use a valid date in yyyy-mm-dd format.", color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                attempted = true
                if (title.isNotBlank() && validDate) onSave(title.trim(), eventDate, description.trim().ifEmpty { null })
            }, modifier = Modifier.fillMaxWidth()) { Icon(ThIcons.Check, null); Spacer(Modifier.width(8.dp)); Text(if (isEditing) "Save changes" else "Add to our story") }
        }
    }
}
