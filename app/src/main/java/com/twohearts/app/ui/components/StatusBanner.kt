package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThStatusBanner — inline error/success/info/warning message.
 *
 * Phase 1: this previously rendered its "icon" as a literal "•" character
 * placeholder (the real icon slot was never wired), which is exactly the
 * kind of unfinished detail that makes a screen feel unpolished. It now
 * renders the real icon from the central set, and each variant carries a
 * tinted surface *plus* a matching border so it reads as a distinct
 * component rather than a coloured paragraph.
 *
 * Each variant pairs its colour with an icon so status is never conveyed
 * by colour alone (WCAG 1.4.1).
 */
@Composable
fun ThStatusBanner(
    variant: StatusVariant,
    text: String,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current

    val icon = when (variant) {
        StatusVariant.ERROR -> ThIcons.Close
        StatusVariant.SUCCESS -> ThIcons.CheckCircle
        StatusVariant.WARNING -> ThIcons.Info
        StatusVariant.INFO -> ThIcons.Info
    }
    val bgColor = when (variant) {
        StatusVariant.ERROR -> thColors.errorBg
        StatusVariant.SUCCESS -> thColors.successBg
        StatusVariant.WARNING -> thColors.warningBg
        StatusVariant.INFO -> thColors.infoBg
    }
    val accentColor = when (variant) {
        StatusVariant.ERROR -> thColors.error
        StatusVariant.SUCCESS -> thColors.success
        StatusVariant.WARNING -> thColors.warning
        StatusVariant.INFO -> thColors.info
    }
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, shape)
            .border(TwoHeartsTokens.Border.hairline, accentColor.copy(alpha = 0.35f), shape)
            .padding(
                horizontal = TwoHeartsTokens.Spacing.space4,
                vertical = TwoHeartsTokens.Spacing.space3,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = accentColor,
        )
    }
}

enum class StatusVariant {
    ERROR,
    SUCCESS,
    WARNING,
    INFO,
}
