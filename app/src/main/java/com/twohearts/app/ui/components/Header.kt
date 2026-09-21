package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.ShellSurfaces
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Header height. A minimum, so large text grows the bar instead of clipping. */
private val ThHeaderHeight = TwoHeartsTokens.Dimensions.headerHeight

/** Header side gutter. */
private val ThHeaderGutter = TwoHeartsTokens.Spacing.space2

/**
 * ThHeader — the shell's screen header.
 *
 * ## Phase 2 changes, and why
 *
 * The migrated header had four problems, all visible in the Phase 0/1
 * renders:
 *
 *  1. **The title was not centred.** The left and right slots each fell back
 *     to a `Spacer(Modifier.height(44.dp))`. A `Spacer` with only a height
 *     occupies zero *width*, so on every screen with no back button and no
 *     actions the title was squeezed toward the trailing edge instead of
 *     sitting in the middle. Measured on the baseline renders, the title on
 *     a detail screen was centred around x≈800px on a 1233px-wide canvas
 *     (centre 616) — that is a bug, not a style choice.
 *  2. **It ignored the status bar.** The header drew at y=0 regardless of
 *     the system status bar, so with the app's `enableEdgeToEdge()` the
 *     title overlapped the status bar's own text. On the baseline renders
 *     the title ink began at y≈66px (~24dp), which is exactly where a status
 *     bar sits.
 *  3. **Every surface looked the same.** A top-level area, a detail page and
 *     a create form all got an identical 56dp bar, while the directive asks
 *     for those roles to be distinguished.
 *  4. **It hardcoded 56dp**, so a large text-size setting could clip the
 *     title rather than letting the header grow.
 *
 * The header now derives its *role* from the current shell route (see
 * [ShellSurfaces]) and adapts:
 *
 *  - [ShellSurfaces.Role.ROOT] — a primary area. No back button (there is
 *    nowhere above it), a larger title, and a transparent surface so the
 *    page's own background carries. This keeps the bar from reading as a
 *    heavy slab stacked on top of the content.
 *  - [ShellSurfaces.Role.DETAIL] — a step down into one thing. Back button,
 *    medium title, the plain surface tone so the header separates from the
 *    page behind it.
 *  - [ShellSurfaces.Role.MODAL] — a task in progress (creating, editing,
 *    logging). Back button, medium title, and the warm surface tone so the
 *    screen reads as a temporary layer rather than a place.
 */
@Composable
fun ThHeader(
    title: String,
    modifier: Modifier = Modifier,
    role: ShellSurfaces.Role = ShellSurfaces.roleOf(LocalShellRoute.current),
    titleBadge: String? = null,
    left: @Composable (() -> Unit)? = null,
    right: @Composable (() -> Unit)? = null,
) {
    val thColors = LocalTwoHeartsColors.current

    val backgroundColor = when (role) {
        ShellSurfaces.Role.ROOT -> Color.Transparent
        ShellSurfaces.Role.DETAIL -> MaterialTheme.colorScheme.surface
        ShellSurfaces.Role.MODAL -> thColors.surfaceWarm
    }

    // A top-level area's title says where you are; a detail or task title says
    // what this is. Keeping the second pair a step smaller preserves that
    // hierarchy instead of shouting every screen at the same volume.
    val titleStyle = when (role) {
        ShellSurfaces.Role.ROOT -> MaterialTheme.typography.titleLarge
        ShellSurfaces.Role.DETAIL,
        ShellSurfaces.Role.MODAL -> MaterialTheme.typography.titleMedium
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            // The shell consumes safe-drawing insets for the content region,
            // so this is normally already satisfied; it matters when a header
            // is rendered outside the shell, as in previews and isolated
            // render tests.
            .windowInsetsPadding(WindowInsets.statusBars)
            .heightIn(min = ThHeaderHeight)
            .padding(horizontal = ThHeaderGutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left slot (back affordance). Reserves button width even when empty
        // so the title stays optically centred between absent controls.
        HeaderSlot(slot = left)

        // Title and its optional count badge share the middle third.
        //
        // The badge lives inside the flexible slot rather than in the trailing
        // actions, because it counts the content *below* it ("3 unread
        // notifications") — grouping it with the title keeps that meaning,
        // while the trailing slot stays reserved for things you can *do*.
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = TwoHeartsTokens.Spacing.space2),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (titleBadge != null) {
                Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space2))
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(text = titleBadge)
                }
            }
        }

        HeaderSlot(slot = right)
    }
}

/**
 * One header slot.
 *
 * When the caller supplies nothing, this still reserves the width an icon
 * button would occupy. That equal width on both sides is what keeps the
 * title centred — the previous implementation reserved *height* instead of
 * width, which is why titles drifted off-centre.
 */
@Composable
private fun HeaderSlot(slot: (@Composable () -> Unit)?) {
    Row(
        modifier = Modifier.width(TwoHeartsTokens.Dimensions.touchTargetMin),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (slot != null) {
            slot()
        } else {
            Spacer(modifier = Modifier.size(TwoHeartsTokens.Dimensions.touchTargetMin))
        }
    }
}