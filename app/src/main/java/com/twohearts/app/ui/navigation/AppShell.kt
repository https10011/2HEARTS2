package com.twohearts.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.components.ToastProvider

/**
 * AppShell — the application layout that wraps every in-app screen.
 *
 * Owns:
 *  - the bottom navigation bar;
 *  - the system back gesture/button handling;
 *  - the toast host.
 *
 * Phase 1: the toast host was missing entirely. `AppShell` declared an empty
 * `snackbarHost` with a "will be integrated later" comment, and
 * [ToastProvider] was never mounted anywhere in the tree. Because
 * `LocalToastApi` falls back to a no-op implementation, every
 * `toast.success(...)` call in the app compiled cleanly and then did
 * nothing — users got no confirmation after creating a note, saving a
 * memory, or updating a reminder. Mounting the provider here fixes all of
 * those call sites at once.
 */
@Composable
fun AppShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    BackHandler {
        onBack()
    }

    ToastProvider {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    BottomNav(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    }
}