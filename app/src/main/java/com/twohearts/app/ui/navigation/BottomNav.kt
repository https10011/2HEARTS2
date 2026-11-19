package com.twohearts.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.BrandLogoTone
import com.twohearts.app.ui.components.thSoftShadow
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Glyph size inside the bar. Sits just under the 24dp optical minimum. */
private val NavIconSize = 23.dp

private val NavStackGap = 1.dp

/** Thickness of the selected-state ring around the brand button. */
private val BrandRingWidth = 2.dp

/** Floating navigation surface with equal-width tabs above the system gesture area. */
@Composable
fun BottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBodyHeight: (Dp) -> Unit = {},
) {
    val items = NavConfig.bottomNavItems
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.pill)
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                horizontal = if (density.fontScale > 1.2f) TwoHeartsTokens.Spacing.space2
                    else TwoHeartsTokens.Dimensions.bottomNavMargin,
                vertical = TwoHeartsTokens.Spacing.space2,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .thSoftShadow(shape, TwoHeartsTokens.Elevation.lift)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(
                    BorderStroke(TwoHeartsTokens.Border.hairline, thColors.borderSubtle),
                    shape,
                )
                .onSizeChanged { size -> onBodyHeight(with(density) { size.height.toDp() }) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = TwoHeartsTokens.Dimensions.bottomNavHeight)
                    .selectableGroup()
                    .padding(horizontal = TwoHeartsTokens.Spacing.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val isActive = currentRoute == item.route
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.isBrand) {
                            BrandNavTab(
                                label = item.label,
                                accessibilityLabel = item.accessibilityLabel,
                                isActive = isActive,
                                onClick = { onNavigate(item.route) },
                            )
                        } else {
                            NavItemView(
                                item = item,
                                isActive = isActive,
                                onClick = { onNavigate(item.route) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Relationship tab, using the official mark and a visible selected ring. */
@Composable
fun BrandNavTab(
    onClick: () -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    label: String = "Us",
    accessibilityLabel: String = "Us — your relationship space",
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) 0.93f else 1f,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "brandPressScale",
    )
    val fieldColor by animateColorAsState(
        targetValue = if (isActive) TwoHeartsTokens.Color.burgundy600 else TwoHeartsTokens.Color.burgundy400,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "brandField",
    )
    val ringWidth by animateDpAsState(
        targetValue = if (isActive) BrandRingWidth else 0.dp,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "brandRing",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .semantics { contentDescription = accessibilityLabel }
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.md))
            .selectable(
                selected = isActive,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            )
            .scale(pressScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(TwoHeartsTokens.Dimensions.bottomNavBrandSize)
                .clip(CircleShape)
                .background(fieldColor, CircleShape)
                .then(
                    if (ringWidth > 0.dp) {
                        Modifier.border(BorderStroke(ringWidth, thColors.blush), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            BrandLogo(
                modifier = Modifier.size(28.dp).clearAndSetSemantics {},
                variant = BrandLogoVariant.MARK,
                tone = BrandLogoTone.LIGHT,
            )
        }

        Spacer(modifier = Modifier.height(NavStackGap))

        Text(
            text = label,
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) thColors.burgundy else thColors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A destination tab with a capsule and dot to show selection independently of color. */
@Composable
fun NavItemView(
    item: NavItem,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val tint = if (isActive) thColors.burgundy else thColors.textSecondary
    val capsule by animateColorAsState(
        targetValue = if (isActive) thColors.blush else Color.Transparent,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navCapsule",
    )
    val iconTint by animateColorAsState(
        targetValue = tint,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navIconTint",
    )
    val labelColor by animateColorAsState(
        targetValue = tint,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navLabelColor",
    )
    val dotAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navDot",
    )
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) 0.94f else 1f,
        animationSpec = if (motion.reduced) snap() else tween(TwoHeartsTokens.Duration.fast.toInt()),
        label = "navPressScale",
    )

    Column(
        modifier = modifier
            .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .fillMaxWidth()
            .semantics { contentDescription = item.accessibilityLabel }
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.md))
            .selectable(
                selected = isActive,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(
                vertical = TwoHeartsTokens.Spacing.space1,
            )
            .scale(pressScale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Selected destination's tonal highlight.
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 24.dp)
                .clip(RoundedCornerShape(TwoHeartsTokens.Radius.pill))
                .background(capsule, RoundedCornerShape(TwoHeartsTokens.Radius.pill)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null, // the column announces the label
                tint = iconTint,
                modifier = Modifier.size(NavIconSize),
            )
        }

        Spacer(modifier = Modifier.height(NavStackGap))

        // Shape signal — readable without colour.
        Box(
            modifier = Modifier
                .size(4.dp)
                .scale(if (isActive) 1f else 0.5f)
                .background(thColors.burgundy.copy(alpha = dotAlpha), CircleShape),
        )

        Spacer(modifier = Modifier.height(NavStackGap))

        Text(
            text = item.label,
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
