package com.twohearts.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ToastProvider

/**
 * AppShell — the layout that wraps every in-app screen.
 *
 * Owns the bottom navigation, system back handling, and the toast host.
 *
 * ## Phase 2 changes, and why
 *
 *  - **The navigation bar is shown on primary areas only.** It previously
 *    sat outside the `NavHost` and so rendered over detail, editor and modal
 *    surfaces too — including create flows, where the bar's destinations are
 *    not where leaving the screen would take you, and where the approved
 *    reference screens show no bar. Visibility now comes from
 *    [NavConfig.isPrimaryRoute]: one decision in one place, instead of a
 *    rule each screen has to remember.
 *
 *  - **The bar actually floats over the content.** It used to live in
 *    `Scaffold`'s `bottomBar` slot, which reserves a full-width strip of
 *    page colour beneath the pill — so the "floating" surface was resting on
 *    its own opaque band and never over the content. The bar is now drawn in
 *    an overlay above the content, and the content is given matching bottom
 *    padding so its last row can still be scrolled clear.
 *
 *  - **The shell owns the window insets.** With `enableEdgeToEdge()`, the
 *    previous arrangement let the bar sit inside the system gesture area:
 *    `Scaffold` pushed the bottom inset into the *content* padding while the
 *    bar itself contributed nothing. The shell now consumes the safe-drawing
 *    insets for the whole content region and then hands that region exactly
 *    the padding the navigation reserve needs, so the two never double up
 *    (screens themselves still apply their own insets and see them already
 *    consumed, which is the intended contract — see `ThScreen`).
 */
@Composable
fun AppShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    canNavigateBack: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    // Phase 2: only intercept back when there is somewhere to go back to.
    //
    // The migrated shell installed an unconditional BackHandler whose
    // callback silently did nothing when the stack was empty, so pressing
    // system back on a top-level area was a no-op — the user could not leave
    // the app the way every other Android app lets them. Enabling the
    // handler conditionally restores the platform default at the root while
    // keeping the in-app stack correct everywhere else.
    if (canNavigateBack) {
        BackHandler {
            onBack()
        }
    }

    val showBottomNav = NavConfig.isPrimaryRoute(currentRoute)
    val bottomInset = ShellLayout.contentBottomInsetFor(currentRoute)

    ToastProvider {
        CompositionLocalProvider(LocalShellRoute provides currentRoute) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // The content region owns the safe-area insets for
                        // the whole app. Screens inside still request them;
                        // because they are consumed here, that request is
                        // already satisfied and adds nothing — which is what
                        // keeps the layout from double-padding under the
                        // status bar.
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .consumeWindowInsets(WindowInsets.safeDrawing)
                        .padding(bottom = bottomInset)
                ) {
                    content(PaddingValues(0.dp))
                }

                // Primary areas only: detail and editor surfaces are reached
                // *through* the areas below, not alongside them.
                if (showBottomNav) {
                    BottomNav(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}