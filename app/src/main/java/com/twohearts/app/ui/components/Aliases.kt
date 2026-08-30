package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

// ─── Header ────────────────────────────────────────────────────────
/**
 * Bridge alias so screen files can call `Header(title, onBack, actions)`.
 */
@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    ThHeader(
        title = title,
        modifier = modifier,
        left = if (onBack != null) {
            { BackIconButton(onClick = onBack) }
        } else null,
        right = { actions() },
    )
}

// ─── EmptyState ────────────────────────────────────────────────────
/**
 * Bridge alias so screen files can call `EmptyState(title, message)`.
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    description: String? = null,
    visual: @Composable (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    ThEmptyState(
        title = title,
        modifier = modifier,
        description = message ?: description,
        visual = visual,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}

// ─── Input ─────────────────────────────────────────────────────────
/**
 * Bridge alias so screen files can call
 * `Input(value, onValueChange, label, placeholder, readOnly, error)`.
 */
@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    readOnly: Boolean = false,
    error: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    multiline: Boolean = false,
) {
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.sm)
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = if (label.isNotEmpty()) { Text(label) } else null,
        placeholder = if (placeholder.isNotEmpty()) { Text(placeholder) } else null,
        enabled = enabled && !readOnly,
        readOnly = readOnly,
        shape = shape,
        colors = colors,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error, color = MaterialTheme.colorScheme.error) }
        } else null,
    )
}

// ─── Card (clickable) ──────────────────────────────────────────────
/**
 * Bridge alias so screen files can call `Card(onClick, modifier) { content }`.
 */
@Composable
fun Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(TwoHeartsTokens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        content()
    }
}

// ─── ConfirmDialog ─────────────────────────────────────────────────
/**
 * Bridge alias so screen files can call
 * `ConfirmDialog(title, message, confirmText, onConfirm, onDismiss)`.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Cancel",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
        modifier = modifier,
    )
}

// ─── TextThButton ──────────────────────────────────────────────────
/**
 * Bridge alias for the TextThButton used in onboarding screens.
 */
@Composable
fun TextThButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        content()
    }
}

// ─── Internal helpers ──────────────────────────────────────────────

@Composable
private fun BackIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
        )
    }
}
