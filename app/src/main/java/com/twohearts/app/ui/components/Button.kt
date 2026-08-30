package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * TwoHearts Button — matches legacy Button.tsx variants.
 *
 * Variants:
 * - primary: burgundy background, white text
 * - secondary: outlined, burgundy border/text
 * - ghost: no background/border, burgundy text
 * - danger: red background, white text
 */
@Composable
fun ThButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    full: Boolean = false,
    text: String,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)

    val modifierApplied = modifier
        .then(if (full) Modifier.fillMaxWidth() else Modifier)
        .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)

    when (variant) {
        ButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifierApplied,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.38f),
            ),
        ) {
            Text(text)
        }

        ButtonVariant.SECONDARY -> OutlinedButton(
            onClick = onClick,
            modifier = modifierApplied,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            ),
        ) {
            Text(text)
        }

        ButtonVariant.GHOST -> TextButton(
            onClick = onClick,
            modifier = modifierApplied,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            ),
        ) {
            Text(text)
        }

        ButtonVariant.DANGER -> Button(
            onClick = onClick,
            modifier = modifierApplied,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = thColors.error,
                contentColor = Color.White,
                disabledContainerColor = thColors.error.copy(alpha = 0.38f),
                disabledContentColor = Color.White.copy(alpha = 0.38f),
            ),
        ) {
            Text(text)
        }
    }
}

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    GHOST,
    DANGER,
}
