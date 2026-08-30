package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThStatusBanner — inline error/success/info message matching legacy StatusBanner.tsx.
 *
 * Variants:
 * - error: red tint, error icon
 * - success: green tint, check icon
 * - info: neutral tint, info icon
 */
@Composable
fun ThStatusBanner(
    variant: StatusVariant,
    text: String,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val thIcon = when (variant) {
        StatusVariant.ERROR -> ThIcons.Close
        StatusVariant.SUCCESS -> ThIcons.Check
        StatusVariant.INFO -> ThIcons.Info
    }
    val bgColor = when (variant) {
        StatusVariant.ERROR -> thColors.errorBg
        StatusVariant.SUCCESS -> thColors.successBg
        StatusVariant.INFO -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (variant) {
        StatusVariant.ERROR -> thColors.error
        StatusVariant.SUCCESS -> thColors.success
        StatusVariant.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(TwoHeartsTokens.Radius.sm))
            .padding(TwoHeartsTokens.Spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon placeholder — will use ThIcons when available
        Text(
            text = "•",
            color = textColor,
            modifier = Modifier.padding(end = TwoHeartsTokens.Spacing.space2),
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
    }
}

enum class StatusVariant {
    ERROR,
    SUCCESS,
    INFO,
}
