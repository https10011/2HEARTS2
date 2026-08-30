package com.twohearts.app.ui.screens.memories

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
 * AddMemory — memory creation screen.
 *
 * Matches legacy AddMemory with:
 * - Title input (required)
 * - Caption input (optional)
 * - Date picker (defaults to today)
 * - Photo/video selection (placeholder)
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemory(
    memoryId: String? = null,
    initialTitle: String = "",
    initialCaption: String = "",
    initialDate: String = DateTimeHelper.todayLocal(),
    onSave: (title: String, caption: String?, memoryDate: String) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var caption by remember { mutableStateOf(initialCaption) }
    var memoryDate by remember { mutableStateOf(initialDate) }

    val isEditing = memoryId != null

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Memory" else "New Memory",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    title.trim(),
                                    caption.takeIf { it.isNotBlank() }?.trim(),
                                    memoryDate
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
                placeholder = "Enter memory title",
                modifier = Modifier.fillMaxWidth()
            )

            // Caption input
            Input(
                value = caption,
                onValueChange = { caption = it },
                label = "Caption (optional)",
                placeholder = "Add a caption...",
                modifier = Modifier.fillMaxWidth()
            )

            // Date input
            Input(
                value = memoryDate,
                onValueChange = { memoryDate = it },
                label = "Date",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            // Photo/video selection (placeholder)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📸 Add Photos/Videos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
