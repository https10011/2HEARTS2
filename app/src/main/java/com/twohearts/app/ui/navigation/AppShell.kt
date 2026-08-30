package com.twohearts.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Toast

/**
 * AppShell — main application layout.
 *
 * Matches legacy AppShell with:
 * - Scrollable content area
 * - Bottom navigation
 * - Toast host
 * - Back button handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    // Back button handler
    BackHandler {
        onBack()
    }

    Scaffold(
        bottomBar = {
            BottomNav(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        },
        snackbarHost = {
            // Toast host (SnackbarHost)
            // Will be integrated with Toast system
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
