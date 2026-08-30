package com.twohearts.app.ui.screens.period

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
 * LogPeriod — period logging screen.
 *
 * matches legacy LogPeriod with:
 * - Start date (required)
 * - End date (optional)
 * - Flow level selector
 * - Note input
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogPeriod(
    entryId: String? = null,
    initialStartDate: String = DateTimeHelper.todayLocal(),
    initialEndDate: String = "",
    initialFlowLevel: String = "medium",
    initialNote: String = "",
    onSave: (startDate: String, endDate: String?, flowLevel: String, note: String?) -> Unit,
    onBack: () -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var flowLevel by remember { mutableStateOf(initialFlowLevel) }
    var note by remember { mutableStateOf(initialNote) }
    var showFlowMenu by remember { mutableStateOf(false) }

    val isEditing = entryId != null
    val flowOptions = listOf("light", "medium", "heavy")

    Scaffold(
        topBar = {
            Header(
                title = if (isEditing) "Edit Period" else "Log Period",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            if (startDate.isNotBlank()) {
                                onSave(
                                    startDate,
                                    endDate.takeIf { it.isNotBlank() },
                                    flowLevel,
                                    note.takeIf { it.isNotBlank() }?.trim()
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
            // Start date
            Input(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Start Date",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            // End date
            Input(
                value = endDate,
                onValueChange = { endDate = it },
                label = "End Date (optional)",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            // Flow level
            ExposedDropdownMenuBox(
                expanded = showFlowMenu,
                onExpandedChange = { showFlowMenu = it }
            ) {
                Input(
                    value = flowLevel.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    label = "Flow Level",
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showFlowMenu,
                    onDismissRequest = { showFlowMenu = false }
                ) {
                    flowOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                flowLevel = option
                                showFlowMenu = false
                            }
                        )
                    }
                }
            }

            // Note
            Input(
                value = note,
                onValueChange = { note = it },
                label = "Note (optional)",
                placeholder = "Add a note...",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
