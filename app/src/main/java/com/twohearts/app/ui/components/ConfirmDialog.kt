package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.ui.theme.TwoHeartsTokens

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
    dismissLabel: String = "Cancel",
) {
    val spacing = TwoHeartsTokens.Spacing
    ThModal(open = open, onClose = { if (!busy) onClose() }, modifier = modifier, label = label) {
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            Spacer(Modifier.height(spacing.space2))
            ThButton(
                onClick = onAction, variant = actionVariant, full = true,
                loading = busy, text = if (busy) busyLabel else actionLabel,
            )
            ThButton(onClick = onClose, variant = ButtonVariant.GHOST, full = true, enabled = !busy, text = dismissLabel)
        }
    }
}
