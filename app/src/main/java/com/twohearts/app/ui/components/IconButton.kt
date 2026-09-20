package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(TwoHeartsTokens.Dimensions.touchTargetMin)
            .clip(CircleShape)
            .semantics { contentDescription = label },
        enabled = enabled,
    ) {
        content()
    }
}
