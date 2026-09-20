package com.twohearts.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThInput — the app's text field.
 *
 * Phase 1 rebuild. The previous version used Material's `TextField`, which
 * meant every field carried Material's floating-label animation and fill
 * treatment, and — critically — drew no container outline at all (the
 * `border` token it inherited measured 1.27:1 against the surface, so
 * fields were visually indistinguishable from the page).
 *
 * This renders a plain, fully-controlled field:
 *  - a real, visible 1dp border that brightens to burgundy on focus;
 *  - a label rendered as a normal caption above the field (no floating
 *    animation, which also removes a whole class of text-scaling overlap);
 *  - an error state that changes both border *and* supporting text, so it
 *    is never communicated by colour alone.
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
    readOnly: Boolean = false,
    singleLine: Boolean = !multiline,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    maxLines: Int = if (multiline) 8 else 1,
) {
    val thColors = LocalTwoHeartsColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.sm)
    val isError = error != null
    val isEditable = enabled && !readOnly

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> thColors.error
            focused -> MaterialTheme.colorScheme.primary
            else -> thColors.borderStrong
        },
        label = "th-input-border",
    )
    val borderWidth = if (focused || isError) {
        TwoHeartsTokens.Border.strong
    } else {
        TwoHeartsTokens.Border.hairline
    }

    val fieldMinHeight = if (multiline) 112.dp else TwoHeartsTokens.Dimensions.touchTargetMin

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isError) thColors.error else thColors.textSecondary,
            )
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.labelGap))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldMinHeight)
                .background(
                    if (isEditable) MaterialTheme.colorScheme.surface else thColors.surfaceWarm,
                    shape,
                )
                .border(BorderStroke(borderWidth, borderColor), shape)
                .padding(
                    horizontal = TwoHeartsTokens.Spacing.space4,
                    vertical = if (multiline) TwoHeartsTokens.Spacing.space3 else TwoHeartsTokens.Spacing.space2,
                ),
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = thColors.textTertiary,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterStart),
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(
                        if (multiline) androidx.compose.ui.Alignment.TopStart
                        else androidx.compose.ui.Alignment.CenterStart
                    ),
                enabled = isEditable,
                readOnly = readOnly,
                singleLine = singleLine,
                maxLines = maxLines,
                textStyle = LocalTextStyle.current.merge(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                interactionSource = interactionSource,
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.labelGap))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = thColors.error,
            )
        }
    }
}