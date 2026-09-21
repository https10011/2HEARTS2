package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.ShellSurfaces
import com.twohearts.app.ui.theme.TwoHeartsTokens

// ─── Header ────────────────────────────────────────────────────────
/**
 * Bridge alias so screen files can call `Header(title, onBack, actions)`.
 *
 * The back affordance goes through [ThIconButton], which is the only place
 * that knows how to size and label an icon-only control. Previously this
 * shim built a raw `IconButton` with `Icons.Default.ArrowBack` — a Material 2
 * import in a Phase 1 codebase, and one of the call sites that made the
 * back button look different from every other icon action.
 *
 * Phase 2: the back affordance is *suppressed on top-level areas* even when
 * a caller passes `onBack`. Several primary screens (Notes, Notifications,
 * More) were routed with a `popBackStack` callback and therefore drew a back
 * button at the top level, where there is nothing to go back to — Android's
 * convention is that a primary destination shows no back affordance, and the
 * approved reference screens agree. Deciding it here from the shell role
 * means a screen cannot opt back into the wrong behaviour by accident, and
 * the call sites need no change.
 */
@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    val role = ShellSurfaces.roleOf(LocalShellRoute.current)
    val showBack = onBack != null && role != ShellSurfaces.Role.ROOT
    ThHeader(
        title = title,
        modifier = modifier,
        left = if (showBack) {
            {
                ThIconButton(onClick = onBack!!, label = "Go back") {
                    Icon(
                        imageVector = ThIcons.Back,
                        contentDescription = null,
                    )
                }
            }
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
 *
 * Delegates to [ThInput] — this used to be a second, independent
 * implementation of the same field, which is why some screens had
 * differently-styled inputs.
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
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    imeAction: androidx.compose.ui.text.input.ImeAction =
        androidx.compose.ui.text.input.ImeAction.Default,
    capitalization: androidx.compose.ui.text.input.KeyboardCapitalization =
        androidx.compose.ui.text.input.KeyboardCapitalization.None,
) {
    ThInput(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        multiline = multiline,
        enabled = enabled,
        readOnly = readOnly,
        keyboardType = keyboardType,
        visualTransformation = visualTransformation,
        imeAction = imeAction,
        capitalization = capitalization,
        error = error,
    )
}

// ─── Card (clickable) ──────────────────────────────────────────────
/**
 * Bridge alias so screen files can call `Card(onClick, modifier) { content }`.
 * Delegates to [ThSurfaceCard] so gesture cards and static cards share one
 * container treatment.
 */
@Composable
fun Card(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    ThSurfaceCard(
        modifier = modifier,
        onClick = if (enabled) onClick else null,
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
