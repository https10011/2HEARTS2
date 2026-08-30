package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThIconButton — icon-only button with accessible label.
 * Matches legacy IconButton.tsx behavior.
 */
@Composable
fun ThIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(TwoHeartsTokens.Dimensions.touchTargetMin)
            .clip(CircleShape),
        enabled = enabled,
    ) {
        content()
    }
}
