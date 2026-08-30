package com.twohearts.app.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input

/**
 * NoteEditor — note creation/editing screen.
 *
 * Matches legacy NoteEditor with:
 * - Title input (required)
 * - Content input (required)
 * - Category selector
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditor(
    noteId: String? = null,
    initialTitle: String = "",
    initialContent: String = "",
    initialCategory: NoteCategory = NoteCategory.GENERAL,
    onSave: (title: String, content: String, category: NoteCategory) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var category by remember { mutableStateOf(initialCategory) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val isEditing = noteId != null

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Note" else "New Note",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSave(title.trim(), content.trim(), category)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input
            Input(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                placeholder = "Enter note title",
                modifier = Modifier.fillMaxWidth()
            )

            // Category selector
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                Input(
                    value = category.label,
                    onValueChange = {},
                    label = "Category",
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    NoteCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.label) },
                            onClick = {
                                category = cat
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Content input
            Input(
                value = content,
                onValueChange = { content = it },
                label = "Content",
                placeholder = "Write your note here...",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
