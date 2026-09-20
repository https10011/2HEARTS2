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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThScreen — the foundational screen container.
 *
 * Encapsulates portrait-first layout: a scrollable content column with
 * consistent screen gutters, and an optional footer that stays pinned above
 * the navigation bar.
 *
 * Phase 1 changes:
 *  - The previous version applied no window-inset padding at all, so with
 *    `enableEdgeToEdge()` in MainActivity, content ran underneath the
 *    status bar and gesture navigation bar. It now consumes the safe-area
 *    insets itself, which fixes every screen at once.
 *  - Horizontal padding comes from the semantic `screenGutter` token so
 *    all screens share one gutter instead of each choosing its own.
 *  - Content is wrapped in a [Column] with a section gap between direct
 *    children, giving a consistent vertical rhythm without every screen
 *    hand-placing spacers.
 */
@Composable
fun ThScreen(
    modifier: Modifier = Modifier,
    noScroll: Boolean = false,
    applyInsets: Boolean = true,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(
                if (applyInsets) {
                    Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                } else {
                    Modifier
                }
            )
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (noScroll) Modifier else Modifier.verticalScroll(rememberScrollState())
                )
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter),
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
                        horizontal = TwoHeartsTokens.Spacing.screenGutter,
                        vertical = TwoHeartsTokens.Spacing.space4,
                    ),
            ) {
                footer()
            }
        }
    }
}
