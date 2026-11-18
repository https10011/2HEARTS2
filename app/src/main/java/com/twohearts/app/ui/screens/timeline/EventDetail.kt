package com.twohearts.app.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * EventDetail — timeline event detail screen.
 *
 * Matches legacy EventDetail with:
 * - Full event display
 * - Chapter system
 * - Edit/delete actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetail(
    event: TimelineEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val colors = LocalTwoHeartsColors.current

    Scaffold(
        topBar = {
            Header(
                title = "Event",
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
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            // Date
            Text(
                text = DateTimeHelper.formatDisplay(event.eventDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            if (event.description != null) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary,
                    fontFamily = FontFamily.Serif,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timestamps
            Text(
                text = "Created: ${DateTimeHelper.formatDisplay(event.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            if (event.updatedAt != event.createdAt) {
                Text(
                    text = "Updated: ${DateTimeHelper.formatDisplay(event.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(32.dp))
            OutlinedButton(onClick = { showDeleteDialog = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(ThIcons.Trash, null); Spacer(Modifier.width(8.dp)); Text("Delete moment")
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete this moment?",
            message = "This chapter will be removed from your timeline. This cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
