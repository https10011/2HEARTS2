package com.twohearts.app.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.Note
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * NoteDetail — note detail screen.
 *
 * Matches legacy NoteDetail with:
 * - Serif reading view
 * - Edit/delete actions
 * - Timestamp display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetail(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val category = NoteCategory.fromString(note.category)
    val colors = LocalTwoHeartsColors.current

    Scaffold(
        topBar = {
            Header(
                title = "Note",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = ThIcons.Edit,
                            contentDescription = "Edit"
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
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Category badge
            Surface(
                color = colors.surfaceBlush,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.roseDeep,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = note.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = "Created: ${DateTimeHelper.formatDisplay(note.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            if (note.updatedAt != note.createdAt) {
                Text(
                    text = "Updated: ${DateTimeHelper.formatDisplay(note.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content (serif reading view)
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Serif,
            )
            Spacer(Modifier.height(36.dp))
            OutlinedButton(onClick = { showDeleteDialog = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(ThIcons.Trash, null); Spacer(Modifier.width(8.dp)); Text("Delete note")
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete this note?",
            message = "This note will be removed from TwoHearts. This cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
