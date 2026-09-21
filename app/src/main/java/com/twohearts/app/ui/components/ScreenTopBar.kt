package com.twohearts.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.ShellSurfaces

/**
 * ThTopBarBackAction — the canonical header back affordance.
 *
 * ## Why this exists
 *
 * Phase 0 recorded that the app had drifted into **two parallel header
 * systems**: `ThHeader` on the feature screens, and a stock Material
 * `TopAppBar` on Search, Notifications, Settings, About, Security and the
 * vault screens. They were not merely styled differently — they were
 * *different components* with different heights, different colour behaviour,
 * different back-button styling and different status-bar handling, so the
 * same navigation level looked like a different app depending on which screen
 * you were on. Phase 0 listed this as consistency finding **C3**, "two
 * back-navigation systems".
 *
 * Nine of those screens had written out the identical eleven-line block:
 * an `IconButton`, `Icons.Default.ArrowBack`, `contentDescription = "Back"`.
 * That is the shape of a problem where a call site should instead name what
 * it wants. This helper *is* that name, and it routes through
 * [ThIconButton] so the icon, size, ripple and accessibility label are
 * decided in one place.
 *
 * ## Accessibility
 *
 * The label is carried by [ThIconButton], which merges it onto the button
 * itself. The icon's own `contentDescription` is deliberately null so the
 * control announces "Go back" once rather than twice — Phase 0 found several
 * icon-only controls where the label had been dropped or duplicated.
 */
@Composable
fun ThTopBarBackAction(onBack: () -> Unit) {
    ThIconButton(onClick = onBack, label = "Go back") {
        Icon(imageVector = ThIcons.Back, contentDescription = null)
    }
}

/**
 * ThTopBar — a screen header expressed in the shell's header vocabulary.
 *
 * This is the drop-in replacement for the stock Material `TopAppBar` call
 * sites described above. It takes the shape those call sites already had — a
 * title, an optional back action, optional trailing content — and renders it
 * through [ThHeader], so every screen at a given navigation level looks the
 * same and picks up the same status-bar handling.
 *
 * Pass [actions] only when the screen actually has trailing content; there is
 * no sentinel or empty-lambda trickery, so "no actions" stays obvious.
 *
 * The back affordance is suppressed on top-level areas for the same reason as
 * in `Header`: a primary destination has nothing above it, the bottom bar is
 * showing, and drawing a back arrow there is the exact double-affordance the
 * approved reference screens avoid.
 */
@Composable
fun ThTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    role: ShellSurfaces.Role = ShellSurfaces.roleOf(LocalShellRoute.current),
    titleBadge: String? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val showBack = onBack != null && role != ShellSurfaces.Role.ROOT
    ThHeader(
        title = title,
        modifier = modifier,
        role = role,
        titleBadge = titleBadge,
        left = if (showBack) {
            { ThTopBarBackAction(onBack!!) }
        } else null,
        right = actions,
    )
}