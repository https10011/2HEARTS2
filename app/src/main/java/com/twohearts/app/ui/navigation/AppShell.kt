package com.twohearts.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ToastProvider
import com.twohearts.app.ui.components.ToastState
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Owns safe insets, measured dock height, feedback and system Back. */
@Composable
internal fun AppShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    canNavigateBack: Boolean,
    feedback: ToastState = remember { ToastState() },
    keyboardInsets: WindowInsets = WindowInsets.ime,
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

    val keyboardVisible = keyboardInsets.getBottom(LocalDensity.current) > 0
    val showBottomNav = ShellLayout.showsBottomNav(currentRoute, keyboardVisible)
    var navBodyHeight by remember { mutableStateOf(TwoHeartsTokens.Dimensions.bottomNavHeight) }
    val bottomInset = if (showBottomNav) navBodyHeight else 0.dp

    ToastProvider(state = feedback, bottomInset = bottomInset) {
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
                        onBodyHeight = { navBodyHeight = it },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
