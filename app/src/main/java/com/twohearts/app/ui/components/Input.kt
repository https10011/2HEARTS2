package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextArea
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThInput — text input matching legacy Input.tsx.
 *
 * Supports both single-line and multiline (textarea) modes.
 * Uses design tokens for consistent styling.
 */
@Composable
fun ThInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    multiline: Boolean = false,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = if (multiline) Int.MAX_VALUE else 1,
) {
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.sm)
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.primary,
    )

    if (multiline) {
        TextArea(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            label = if (label.isNotEmpty()) {{ Text(label) }} else null,
            placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
            enabled = enabled,
            shape = shape,
            colors = colors,
            maxLines = maxLines,
        )
    } else {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = if (label.isNotEmpty()) {{ Text(label) }} else null,
            placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
            enabled = enabled,
            shape = shape,
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            maxLines = maxLines,
        )
    }
}
