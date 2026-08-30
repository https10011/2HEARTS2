package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThModal — bottom-sheet foundation matching legacy Modal.tsx.
 * Closes on overlay tap. Mobile-native pattern.
 */
@Composable
fun ThModal(
    open: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Dialog",
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!open) return

    val thColors = LocalTwoHeartsColors.current

    // Full-screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(thColors.overlay)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Bottom sheet content
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = TwoHeartsTokens.Radius.xl, topEnd = TwoHeartsTokens.Radius.xl))
                .background(MaterialTheme.colorScheme.surface)
                .padding(TwoHeartsTokens.Spacing.space4)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume click */ },
                ),
        ) {
            content()
        }
    }
}
