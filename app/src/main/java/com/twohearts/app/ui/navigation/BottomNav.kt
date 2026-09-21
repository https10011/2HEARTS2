package com.twohearts.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
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

/**
 * Gap between a column's stacked parts.
 *
 * 1dp, not the 3dp first used here. The approved reference screens stack the
 * glyph and its caption almost directly on top of each other; a 3dp gap on
 * both sides of the selection dot pushed the bar taller than its content
 * needed and made the items read as three loose fragments rather than one
 * destination. The dot contributes 4dp of its own height, so the visual gap
 * between glyph and caption is still a comfortable ~5dp.
 */
private val NavStackGap = 1.dp

/** Thickness of the selected-state ring around the brand button. */
private val BrandRingWidth = 2.dp

/**
 * BottomNav — the floating TwoHearts navigation surface.
 *
 * ## What changed in Phase 2, and why
 *
 * The migrated bar was a 28dp-radius `Surface` with an 8dp Material shadow
 * holding five filled `Icons.Default` glyphs and a 60%-alpha label. It read
 * as a stock Material `NavigationBar` dropped inside a rounded box, and it
 * had four concrete defects that the Phase 0 and Phase 1 renders make
 * visible:
 *
 *  1. **The brand mark vanished when selected.** The centre button drew the
 *     burgundy artwork on a burgundy field. Sampling the rendered bar found
 *     only a few dozen distinct non-background pixels inside the button. The
 *     mark now always renders in the light tone, so it reads on the selected
 *     and unselected field alike.
 *  2. **The active state was colour-only.** Selected-ness was a tint change
 *     and nothing else, which fails the directive's rule that the selected
 *     state must not rely on colour alone and degrades in dark mode.
 *     Selection now carries four independent signals: a tonal capsule behind
 *     the glyph, a filled dot beneath the label, the glyph tint, and the
 *     label tint.
 *  3. **"Notifications" overflowed its column.** Five equal columns on a
 *     360dp screen leave roughly 62dp each, and the label needs more than
 *     that at the 12sp readability floor — so it rendered into its
 *     neighbours. The bar measures its own width and picks each item's full
 *     or short label accordingly (see [NavItem.shortLabel]). The full name is
 *     still what assistive technology announces.
 *  4. **The bar sat inside the system gesture area.** It applied no window
 *     insets of its own and relied on the `Scaffold`, which had already
 *     pushed the bottom inset into the *content* padding — leaving the pill
 *     about 9dp from the physical screen edge. The bar now owns
 *     `navigationBars` padding directly.
 *
 * Visually the bar is deliberately no longer one large pill: the directive
 * warns against "excessive pill geometry" and against "a giant rounded
 * rectangle containing five generic Material icons". It is a bar with a
 * pill's corner treatment, and the only pill *inside* it is the selected
 * item. One rounded shape stays meaningful instead of decorative.
 *
 * Depth is a hairline border plus the single warm shadow helper rather than
 * a Material elevation, so the bar reads as the same paper as the content.
 */
@Composable
fun BottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = NavConfig.bottomNavItems
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.pill)
    val density = LocalDensity.current

    // Width of one column, measured from the real bar rather than assumed, so
    // the label strategy responds to the actual screen (411dp, the narrower
    // 360dp Tecno target, or a large-text configuration) instead of being
    // tuned for one device.
    var columnWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                horizontal = TwoHeartsTokens.Dimensions.bottomNavMargin,
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
                .onSizeChanged { size ->
                    // Five equal columns share the bar's inner width (the row's
                    // own horizontal padding already excluded).
                    columnWidth = with(density) {
                        ((size.width - TwoHeartsTokens.Spacing.space1.toPx() * 2) / 5).toDp()
                    }
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TwoHeartsTokens.Dimensions.bottomNavHeight)
                    .padding(horizontal = TwoHeartsTokens.Spacing.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val isActive = currentRoute == item.route
                    // Equal-width columns rather than `SpaceEvenly`.
                    // `SpaceEvenly` sizes the gaps from the items' own widths,
                    // so the wider centre button pulled the middle column off
                    // centre — measured at ~3.5dp on a 360dp screen. Equal
                    // weights make the five positions exact, give every
                    // destination the same (generous) hit area, and make the
                    // label fitting predictable.
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.isCenter) {
                            CenterBrandButton(
                                label = item.label,
                                accessibilityLabel = item.accessibilityLabel,
                                isActive = isActive,
                                onClick = { onNavigate(item.route) },
                            )
                        } else {
                            NavItemView(
                                item = item,
                                isActive = isActive,
                                columnWidth = columnWidth,
                                onClick = { onNavigate(item.route) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CenterBrandButton — the relationship position.
 *
 * The one deliberately larger target in the bar (50dp, comfortably over the
 * 44dp minimum) because it opens the product's most meaningful destination.
 *
 * The mark is always drawn in the light tone: the burgundy artwork is
 * invisible against the burgundy selected field, which is exactly the defect
 * Phase 0 found. Selection is signalled by the field darkening *and* by a
 * blush ring, so it survives both dark mode and a colour-blind reading.
 */
@Composable
fun CenterBrandButton(
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
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "brandPressScale",
    )
    val fieldColor by animateColorAsState(
        targetValue = if (isActive) thColors.burgundy else thColors.burgundyLight,
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "brandField",
    )
    val ringWidth by animateDpAsState(
        targetValue = if (isActive) BrandRingWidth else 0.dp,
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "brandRing",
    )

    Column(
        modifier = modifier
            // Touch target spans the whole column, not just the circle. The
            // column is ~66dp wide at 360dp, and the product's most important
            // destination should be the easiest thing in the bar to hit —
            // `widthIn(min = 44dp)` alone left the target at the circle's own
            // width. The circle inside stays deliberately small.
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                selected = isActive
                role = Role.Tab
            }
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.md))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = TwoHeartsTokens.Spacing.space1)
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
                modifier = Modifier.size(28.dp),
                variant = BrandLogoVariant.MARK,
                tone = BrandLogoTone.LIGHT,
            )
        }

        Spacer(modifier = Modifier.height(NavStackGap))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) thColors.burgundy else thColors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            // Ellipsis, not Clip. The centre column sits between two others
            // and its label is a single short word, so overflow here is
            // unlikely — but if a future label or a very large text setting
            // did overflow, an ellipsis degrades honestly where a hard clip
            // silently cuts a letter in half.
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * NavItemView — one non-centre destination.
 *
 * The selected state uses three signals that survive greyscale and dark mode
 * (capsule, dot, tint) before colour is even considered, because this is the
 * app's most persistent surface and it is visible in every configuration.
 */
@Composable
fun NavItemView(
    item: NavItem,
    isActive: Boolean,
    columnWidth: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current
    val textMeasurer = rememberTextMeasurer()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Does the full label actually fit its column?
    //
    // This is measured, not guessed. The bar previously compared the column
    // width against a hand-tuned 68dp threshold, which happened to work at
    // Default text size on a 411dp screen and would silently start truncating
    // at larger text sizes or on a narrower device. Measuring the real string
    // against the real available width means the short label appears exactly
    // when the long one would no longer fit, on any screen and at any text
    // size — and the full label is still what assistive technology announces.
    val labelStyle = MaterialTheme.typography.labelMedium
    val availableForLabel = if (columnWidth > 0.dp) {
        columnWidth - TwoHeartsTokens.Spacing.space1 * 2
    } else {
        Dp.Unspecified
    }
    val fullLabelWidth = if (availableForLabel != Dp.Unspecified) {
        with(density) {
            textMeasurer.measure(
                text = item.label,
                style = labelStyle,
                maxLines = 1,
                layoutDirection = layoutDirection,
            ).size.width.toDp()
        }
    } else {
        0.dp
    }
    val label = if (availableForLabel == Dp.Unspecified || fullLabelWidth <= availableForLabel) {
        item.label
    } else {
        item.shortLabel
    }

    val tint = if (isActive) thColors.burgundy else thColors.textSecondary
    val capsule by animateColorAsState(
        targetValue = if (isActive) thColors.blush else Color.Transparent,
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navCapsule",
    )
    val iconTint by animateColorAsState(
        targetValue = tint,
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navIconTint",
    )
    val labelColor by animateColorAsState(
        targetValue = tint,
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navLabelColor",
    )
    val dotAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(TwoHeartsTokens.Duration.quick.toInt()),
        label = "navDot",
    )
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && !motion.reduced) 0.94f else 1f,
        animationSpec = tween(TwoHeartsTokens.Duration.fast.toInt().coerceAtLeast(1)),
        label = "navPressScale",
    )

    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = item.accessibilityLabel
                selected = isActive
                role = Role.Tab
            }
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.md))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = TwoHeartsTokens.Spacing.space1,
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
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}