package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThScreen — foundational mobile screen container matching legacy Screen.tsx.
 *
 * Encapsulates portrait-first, safe-area-aware, keyboard-aware layout.
 * Children render inside a scrollable region.
 * Footer slot stays pinned above keyboard/nav bar.
 */
@Composable
fun ThScreen(
    modifier: Modifier = Modifier,
    noScroll: Boolean = false,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (noScroll) Modifier else Modifier.verticalScroll(rememberScrollState())
                )
                .padding(horizontal = TwoHeartsTokens.Spacing.space4),
        ) {
            content()
        }

        // Footer (pinned above nav/keyboard)
        if (footer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        horizontal = TwoHeartsTokens.Spacing.space4,
                        vertical = TwoHeartsTokens.Spacing.space4,
                    ),
            ) {
                footer()
            }
        }
    }
}
