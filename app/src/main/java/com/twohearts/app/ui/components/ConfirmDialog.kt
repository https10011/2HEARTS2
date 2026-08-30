package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThConfirmDialog — destructive/confirmation modal matching legacy ConfirmDialog.tsx.
 *
 * Button order (mobile convention):
 *   1. Action (top — most likely user intent)
 *   2. Cancel (bottom — safe escape)
 */
@Composable
fun ThConfirmDialog(
    open: Boolean,
    onClose: () -> Unit,
    label: String,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionVariant: ButtonVariant = ButtonVariant.DANGER,
    busy: Boolean = false,
    busyLabel: String = "Working…",
) {
    ThModal(
        open = open,
        onClose = onClose,
        modifier = modifier,
        label = label,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ThButton(
                onClick = onAction,
                variant = actionVariant,
                full = true,
                enabled = !busy,
                text = if (busy) busyLabel else actionLabel,
            )

            ThButton(
                onClick = onClose,
                variant = ButtonVariant.GHOST,
                full = true,
                enabled = !busy,
                text = "Cancel",
            )
        }
    }
}
