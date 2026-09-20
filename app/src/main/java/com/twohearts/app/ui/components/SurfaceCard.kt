package com.twohearts.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * Card tones. Phase 1 gives cards distinct *meanings* rather than one
 * white rounded rectangle repeated everywhere.
 *
 *  - [PLAIN]     default content card. White, hairline border, no shadow.
 *  - [WARM]      soft warm panel for grouped secondary content.
 *  - [BLUSH]     relationship / emotional tint.
 *  - [GOLD]      anniversary, milestone and date-related content.
 *  - [OUTLINE]   no fill; a bordered container for nested content.
 *  - [BRAND]     burgundy field for hero moments that need commitment.
 */
enum class CardTone { PLAIN, WARM, BLUSH, GOLD, OUTLINE, BRAND }

/**
 * ThSurfaceCard — the single card primitive for the app.
 *
 * Design rules (Phase 1):
 *  - Depth comes from a hairline border and surface tone, not from shadow.
 *    Only [elevated] cards, which genuinely sit above other content, get a
 *    shadow.
 *  - Corner radius is [TwoHeartsTokens.Radius.lg] for cards; pass [shape]
 *    only when a specific composition needs it.
 *  - Padding defaults to the semantic `cardPadding` token so card interiors
 *    line up across screens.
 */
@Composable
fun ThSurfaceCard(
    modifier: Modifier = Modifier,
    tone: CardTone = CardTone.PLAIN,
    onClick: (() -> Unit)? = null,
    elevated: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(TwoHeartsTokens.Radius.lg),
    contentPadding: Dp = TwoHeartsTokens.Spacing.cardPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current

    val container = when (tone) {
        CardTone.PLAIN -> MaterialTheme.colorScheme.surface
        CardTone.WARM -> thColors.surfaceWarm
        CardTone.BLUSH -> thColors.surfaceBlush
        CardTone.GOLD -> thColors.goldBg
        CardTone.OUTLINE -> Color.Transparent
        CardTone.BRAND -> thColors.burgundy
    }
    val border = when (tone) {
        CardTone.PLAIN -> BorderStroke(TwoHeartsTokens.Border.hairline, thColors.borderSubtle)
        CardTone.GOLD -> BorderStroke(TwoHeartsTokens.Border.hairline, thColors.goldSoft)
        CardTone.OUTLINE -> BorderStroke(TwoHeartsTokens.Border.hairline, thColors.borderSubtle)
        CardTone.BRAND -> null
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (elevated) {
                    Modifier.thSoftShadow(shape, TwoHeartsTokens.Elevation.raise)
                } else {
                    Modifier
                }
            )
            .background(container, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(contentPadding),
        content = content,
    )
}

/**
 * ThSectionHeader — the shared "group of content starts here" header.
 *
 * Consistent section headings are one of the cheapest ways to make a
 * screen feel designed. Screens should use this instead of an ad-hoc
 * Text + Spacer.
 */
@Composable
fun ThSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.tightGap))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalTwoHeartsColors.current.textTertiary,
                )
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
            action()
        }
    }
}

/**
 * ThEyebrow — small tracked label that sits above a section or inside a
 * card to name it. Sentence case, never all-caps.
 */
@Composable
fun ThEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalTwoHeartsColors.current.textTertiary,
) {
    Text(
        text = text,
        modifier = modifier,
        style = ThTextStyles.eyebrow,
        color = color,
    )
}

/**
 * ThCard — the static content container, kept under its original name.
 *
 * Phase 1 collapsed the separate `Card.kt` implementation into
 * [ThSurfaceCard], so every card in the app — static or tappable — shares
 * one container treatment (white surface, hairline border, no shadow)
 * rather than the elevated Material `Card` it used to wrap.
 */
@Composable
fun ThCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ThSurfaceCard(modifier = modifier, content = content)
}

/**
 * ThTintedBand — a full-bleed horizontal band of tinted surface used to
 * break a long screen into emotional chapters. Distinct from a card: it
 * runs edge to edge and carries no border.
 */
@Composable
fun ThTintedBand(
    modifier: Modifier = Modifier,
    color: Color = LocalTwoHeartsColors.current.surfaceBlush,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color)
            .padding(
                horizontal = TwoHeartsTokens.Spacing.screenGutter,
                vertical = TwoHeartsTokens.Spacing.space5,
            ),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
