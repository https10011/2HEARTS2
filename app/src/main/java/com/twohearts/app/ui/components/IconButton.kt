package com.twohearts.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThIconButton — icon-only button.
 *
 * [label] is required and is applied as the button's accessibility label.
 * It previously accepted a `label` and dropped it entirely, and because the
 * `content` slot usually holds a bare `Icon` with no description, icon-only
 * actions across the app were unlabelled for screen readers.
 */
@Composable
fun ThIconButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val focusRing = if (enabled && focused) {
        Modifier.border(TwoHeartsTokens.Border.strong, MaterialTheme.colorScheme.primary, CircleShape)
    } else {
        Modifier
    }
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(TwoHeartsTokens.Dimensions.touchTargetMin)
            .clip(CircleShape)
            .then(focusRing)
            .semantics { contentDescription = label },
        enabled = enabled,
        interactionSource = interactions,
    ) {
        content()
    }
}
