package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
 * ThEmptyState — empty feature state matching legacy EmptyState.tsx.
 * Visual, title, description, and optional action button.
 */
@Composable
fun ThEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    visual: @Composable (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(TwoHeartsTokens.Spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (visual != null) {
            visual()
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))
            ThButton(
                onClick = onAction,
                text = actionLabel,
            )
        }
    }
}
