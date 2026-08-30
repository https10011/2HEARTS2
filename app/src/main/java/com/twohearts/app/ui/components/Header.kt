package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThHeader — screen header with title and optional left/right slots.
 * Matches legacy Header.tsx.
 */
@Composable
fun ThHeader(
    title: String,
    modifier: Modifier = Modifier,
    left: @Composable (() -> Unit)? = null,
    right: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TwoHeartsTokens.Dimensions.headerHeight)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = TwoHeartsTokens.Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left slot (e.g., back button)
        if (left != null) {
            left()
        } else {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Dimensions.touchTargetMin))
        }

        // Title (centered, flex: 1)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        // Right slot (e.g., action button)
        if (right != null) {
            right()
        } else {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Dimensions.touchTargetMin))
        }
    }
}
