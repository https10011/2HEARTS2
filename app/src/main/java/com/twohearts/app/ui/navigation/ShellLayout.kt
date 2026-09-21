package com.twohearts.app.ui.navigation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ShellLayout — the arithmetic behind the app shell, kept pure.
 *
 * The shell's two easy-to-get-wrong numbers are how much vertical room the
 * navigation surface occupies and how much of that must be reclaimed from
 * the system navigation-bar inset. Keeping them here (rather than inline in
 * the composable) makes the rule readable and testable without a device.
 */
object ShellLayout {

    /** Vertical margin on the top and bottom of the floating bar. */
    val navVerticalMargin = TwoHeartsTokens.Spacing.space2

    /** Horizontal margin between the floating bar and the screen edge. */
    val navHorizontalMargin = TwoHeartsTokens.Dimensions.bottomNavMargin

    /**
     * Full height the navigation surface will consume, including its own
     * margins but *not* the system navigation-bar inset (the bar adds that
     * itself, and it varies by device and gesture mode).
     */
    val navOccupiedHeight: Dp =
        TwoHeartsTokens.Dimensions.bottomNavHeight + navVerticalMargin * 2

    /**
     * Extra bottom room the content should leave while the bar is visible.
     *
     * The bar floats over the content, so the content needs to be able to
     * scroll past it. One extra comfortable step beyond the bar's own height
     * keeps the last row clear of the pill rather than touching it.
     */
    val contentBottomInset: Dp = navOccupiedHeight + TwoHeartsTokens.Spacing.space2

    /**
     * Bottom room the content should leave when the bar is hidden.
     *
     * Zero: a detail or editor screen is full-bleed, and leaving a gap where
     * a bar used to be would read as a layout bug.
     */
    val contentBottomInsetWithoutNav: Dp = 0.dp

    /** Resolve the content inset for the current route. */
    fun contentBottomInsetFor(route: String): Dp =
        if (NavConfig.isPrimaryRoute(route)) contentBottomInset else contentBottomInsetWithoutNav
}