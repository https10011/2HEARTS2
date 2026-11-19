package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
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
    if (!open) return
    AlertDialog(
        shape = RoundedCornerShape(TwoHeartsTokens.Radius.xl),
        containerColor = LocalTwoHeartsColors.current.surfaceElevated,
        modifier = modifier.semantics { paneTitle = label },
        onDismissRequest = { if (!busy) onClose() },
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text(
                description, Modifier.heightIn(max = 196.dp).verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThButton(
                    onClick = onAction, variant = actionVariant, full = true,
                    loading = busy, text = if (busy) busyLabel else actionLabel,
                )
                ThButton(onClick = onClose, variant = ButtonVariant.GHOST, full = true, enabled = !busy, text = dismissLabel)
            }
        },
    )
}
