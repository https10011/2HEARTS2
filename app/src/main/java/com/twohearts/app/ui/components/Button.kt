package com.twohearts.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * TwoHearts Button.
 *
 * Phase 1 rebuild. The previous implementation delegated straight to
 * Material's `Button`/`OutlinedButton`/`TextButton`, which meant:
 *  - the default container/elevation treatments leaked Material's look;
 *  - `SECONDARY` rendered no visible border because the token it inherited
 *    was 1.27:1 against the surface;
 *  - there was no loading or icon affordance, so screens faked both.
 *
 * It now renders one consistent control across all four variants so
 * primary, secondary and destructive actions read as one family.
 *
 * Variants:
 *  - [ButtonVariant.PRIMARY]   burgundy field — the screen's main action.
 *  - [ButtonVariant.SECONDARY] bordered, burgundy label. Safe alternative.
 *  - [ButtonVariant.GHOST]     no field or border; tertiary/dismissive.
 *  - [ButtonVariant.DANGER]    destructive field. Deliberately a different
 *    hue from burgundy so "delete" never reads as "confirm".
 */
@Composable
fun ThButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    full: Boolean = false,
    loading: Boolean = false,
    icon: ImageVector? = null,
    text: String,
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Press feedback: a small, quick settle. Decorative, so it collapses
    // to nothing when reduced motion is on.
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) 0.975f else 1f,
        label = "th-button-press",
    )

    val isInteractive = enabled && !loading
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)

    val container = when (variant) {
        ButtonVariant.PRIMARY -> MaterialTheme.colorScheme.primary
        ButtonVariant.DANGER -> thColors.error
        ButtonVariant.SECONDARY, ButtonVariant.GHOST -> Color.Transparent
    }
    val contentColor = when (variant) {
        ButtonVariant.PRIMARY, ButtonVariant.DANGER -> Color.White
        ButtonVariant.SECONDARY, ButtonVariant.GHOST -> MaterialTheme.colorScheme.primary
    }
    val border = when (variant) {
        // The visible outline the old SECONDARY variant was missing.
        ButtonVariant.SECONDARY -> BorderStroke(
            TwoHeartsTokens.Border.hairline,
            if (isInteractive) thColors.borderStrong else thColors.borderSubtle,
        )
        else -> null
    }

    val effectiveContent = contentColor.copy(alpha = if (isInteractive) 1f else 0.45f)

    Box(
        modifier = modifier
            .then(if (full) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .scale(pressScale)
            .clip(shape)
            .background(container, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isInteractive,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = TwoHeartsTokens.Spacing.space5,
                vertical = TwoHeartsTokens.Spacing.space3,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = effectiveContent,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = effectiveContent,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = effectiveContent,
                maxLines = 2,
            )
        }
    }
}

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    GHOST,
    DANGER,
}

/**
 * ThQuietButton — a borderless, low-emphasis text action used inline inside
 * cards and section headers ("See all", "Edit"). Kept separate from
 * [ThButton] so inline actions never inherit a 44dp minimum height and blow
 * out a row's rhythm.
 */
@Composable
fun ThQuietButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.sm))
            .alpha(if (pressed) 0.6f else 1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = TwoHeartsTokens.Spacing.space2,
                vertical = TwoHeartsTokens.Spacing.space1,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.45f),
            maxLines = 1,
        )
    }
}