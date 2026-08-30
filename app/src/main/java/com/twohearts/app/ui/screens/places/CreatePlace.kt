package com.twohearts.app.ui.screens.places

import androidx.compose.foundation.layout.*
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
 * CreatePlace — place creation screen.
 *
 * Matches legacy CreatePlace with:
 * - Name input (required)
 * - Address input (optional)
 * - City input (optional)
 * - Notes input (optional)
 * - Category selector
 * - Photo dropzone (placeholder)
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlace(
    placeId: String? = null,
    initialName: String = "",
    initialAddress: String = "",
    initialCity: String = "",
    initialNotes: String = "",
    initialCategory: String = "general",
    onSave: (name: String, address: String?, city: String?, notes: String?, category: String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var address by remember { mutableStateOf(initialAddress) }
    var city by remember { mutableStateOf(initialCity) }
    var notes by remember { mutableStateOf(initialNotes) }
    var category by remember { mutableStateOf(initialCategory) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val isEditing = placeId != null
    val categoryOptions = listOf("general", "restaurant", "park", "home", "travel", "other")

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Place" else "New Place",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    name.trim(),
                                    address.takeIf { it.isNotBlank() }?.trim(),
                                    city.takeIf { it.isNotBlank() }?.trim(),
                                    notes.takeIf { it.isNotBlank() }?.trim(),
                                    category
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
            // Name input
            Input(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                placeholder = "Enter place name",
                modifier = Modifier.fillMaxWidth()
            )

            // Address input
            Input(
                value = address,
                onValueChange = { address = it },
                label = "Address (optional)",
                placeholder = "Enter address",
                modifier = Modifier.fillMaxWidth()
            )

            // City input
            Input(
                value = city,
                onValueChange = { city = it },
                label = "City (optional)",
                placeholder = "Enter city",
                modifier = Modifier.fillMaxWidth()
            )

            // Category selector
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                Input(
                    value = category.replaceFirstChar { it.uppercase() },
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
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                category = option
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Notes input
            Input(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                placeholder = "Add notes about this place...",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            // Photo dropzone (placeholder)
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
                        text = "📸 Add Photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
