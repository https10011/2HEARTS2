package com.twohearts.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.twohearts.app.ui.onboarding.OnboardingGate
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService

/**
 * AppRouter — main application router.
 *
 * Matches legacy AppRouter with:
 * - All routes defined
 * - Onboarding gate
 * - App shell
 * - Route transitions (fade + rise)
 */
@Composable
fun AppRouter(
    appStateService: AppStateService,
    relationshipService: RelationshipService,
    appLockService: AppLockService
) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    // Determine current route
    val currentRoutePath = currentRoute?.destination?.route ?: RoutePath.APP_HOME

    // Check if onboarding is complete
    val isOnboarded by appStateService.isOnboarded.collectAsState()
    val onboardingStage by appStateService.onboardingStage.collectAsState()

    // Show onboarding or app
    if (!isOnboarded || onboardingStage != "complete") {
        OnboardingFlow(
            appStateService = appStateService,
            relationshipService = relationshipService,
            appLockService = appLockService,
            onComplete = {
                // Navigate to home after onboarding
                navController.navigate(RoutePath.APP_HOME) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    } else {
        // App shell with navigation
        AppShell(
            currentRoute = currentRoutePath,
            onNavigate = { route ->
                navController.navigate(route) {
                    // Pop up to start destination to avoid building up large back stack
                    popUpTo(RoutePath.APP_HOME) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination
                    launchSingleTop = true
                    // Restore state when re-selecting a tab
                    restoreState = true
                }
            },
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = RoutePath.APP_HOME,
                modifier = Modifier.padding(paddingValues),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically(
                        initialOffsetY = { it / 20 },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutVertically(
                        targetOffsetY = { it / 20 },
                        animationSpec = tween(300)
                    )
                }
            ) {
                // Home
                composable(RoutePath.APP_HOME) {
                    // Placeholder - will be implemented in Stage 7
                    Text("Home Screen")
                }

                // Us / Relationship Hub
                composable(RoutePath.APP_US) {
                    Text("Us Screen")
                }

                // More
                composable(RoutePath.APP_MORE) {
                    Text("More Screen")
                }

                // Notifications
                composable(RoutePath.APP_NOTIFICATIONS) {
                    Text("Notifications Screen")
                }

                // Search
                composable(RoutePath.APP_SEARCH) {
                    Text("Search Screen")
                }

                // Notes
                composable(RoutePath.APP_NOTES) {
                    Text("Notes Screen")
                }

                composable(RoutePath.APP_NOTES_ADD) {
                    Text("Add Note Screen")
                }

                composable(RoutePath.APP_NOTES_DETAIL) {
                    Text("Note Detail Screen")
                }

                composable(RoutePath.APP_NOTES_EDIT) {
                    Text("Edit Note Screen")
                }

                // Memories
                composable(RoutePath.APP_MEMORIES) {
                    Text("Memories Screen")
                }

                composable(RoutePath.APP_MEMORIES_ADD) {
                    Text("Add Memory Screen")
                }

                composable(RoutePath.APP_MEMORIES_DETAIL) {
                    Text("Memory Detail Screen")
                }

                composable(RoutePath.APP_MEMORIES_EDIT) {
                    Text("Edit Memory Screen")
                }

                // Timeline
                composable(RoutePath.APP_TIMELINE) {
                    Text("Timeline Screen")
                }

                composable(RoutePath.APP_TIMELINE_ADD) {
                    Text("Add Event Screen")
                }

                composable(RoutePath.APP_TIMELINE_DETAIL) {
                    Text("Event Detail Screen")
                }

                composable(RoutePath.APP_TIMELINE_EDIT) {
                    Text("Edit Event Screen")
                }

                // Reminders
                composable(RoutePath.APP_REMINDERS) {
                    Text("Reminders Screen")
                }

                composable(RoutePath.APP_REMINDERS_ADD) {
                    Text("Add Reminder Screen")
                }

                composable(RoutePath.APP_REMINDERS_DETAIL) {
                    Text("Reminder Detail Screen")
                }

                composable(RoutePath.APP_REMINDERS_EDIT) {
                    Text("Edit Reminder Screen")
                }

                // Places
                composable(RoutePath.APP_PLACES) {
                    Text("Places Screen")
                }

                composable(RoutePath.APP_PLACES_ADD) {
                    Text("Add Place Screen")
                }

                composable(RoutePath.APP_PLACES_DETAIL) {
                    Text("Place Detail Screen")
                }

                composable(RoutePath.APP_PLACES_EDIT) {
                    Text("Edit Place Screen")
                }

                // Mood
                composable(RoutePath.APP_MOOD) {
                    Text("Mood Screen")
                }

                composable(RoutePath.APP_MOOD_ADD) {
                    Text("Add Mood Screen")
                }

                composable(RoutePath.APP_MOOD_HISTORY) {
                    Text("Mood History Screen")
                }

                composable(RoutePath.APP_MOOD_EDIT) {
                    Text("Edit Mood Screen")
                }

                // Period
                composable(RoutePath.APP_PERIOD) {
                    Text("Period Screen")
                }

                composable(RoutePath.APP_PERIOD_LOG) {
                    Text("Log Period Screen")
                }

                composable(RoutePath.APP_PERIOD_CALENDAR) {
                    Text("Period Calendar Screen")
                }

                composable(RoutePath.APP_PERIOD_HISTORY) {
                    Text("Period History Screen")
                }

                composable(RoutePath.APP_PERIOD_SETTINGS) {
                    Text("Period Settings Screen")
                }

                composable(RoutePath.APP_PERIOD_LOG_EDIT) {
                    Text("Edit Period Screen")
                }

                // Vault
                composable(RoutePath.APP_VAULT) {
                    Text("Vault Screen")
                }

                composable(RoutePath.APP_VAULT_ADD) {
                    Text("Add Vault Content Screen")
                }

                composable(RoutePath.APP_VAULT_CONTENT) {
                    Text("Vault Content Screen")
                }

                // Yuki
                composable(RoutePath.APP_YUKI) {
                    Text("Yuki Screen")
                }

                // Games
                composable(RoutePath.APP_GAMES) {
                    Text("Games Screen")
                }

                composable(RoutePath.APP_GAMES_MEMORY_MATCH) {
                    Text("Memory Match Screen")
                }

                composable(RoutePath.APP_GAMES_WORD_SCRAMBLE) {
                    Text("Word Scramble Screen")
                }

                composable(RoutePath.APP_GAMES_COUPLE_TRIVIA) {
                    Text("Couple Trivia Screen")
                }

                composable(RoutePath.APP_GAMES_WHO_KNOWS) {
                    Text("Who Knows Screen")
                }

                composable(RoutePath.APP_GAMES_WOULD_YOU_RATHER) {
                    Text("Would You Rather Screen")
                }

                composable(RoutePath.APP_GAMES_THIS_OR_THAT) {
                    Text("This Or That Screen")
                }

                composable(RoutePath.APP_GAMES_GUESS_MY_ANSWER) {
                    Text("Guess My Answer Screen")
                }

                composable(RoutePath.APP_GAMES_CASUAL_TRIVIA) {
                    Text("Casual Trivia Screen")
                }

                composable(RoutePath.APP_GAMES_RIDDLE_ROOM) {
                    Text("Riddle Room Screen")
                }

                composable(RoutePath.APP_GAMES_RESULTS) {
                    Text("Game Results Screen")
                }

                // Settings
                composable(RoutePath.APP_SETTINGS) {
                    Text("Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_PROFILE) {
                    Text("Profile Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_RELATIONSHIP) {
                    Text("Relationship Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_APPEARANCE) {
                    Text("Appearance Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_NOTIFICATIONS) {
                    Text("Notification Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_SECURITY) {
                    Text("Security Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_STORAGE) {
                    Text("Storage Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_IMPORT) {
                    Text("Import Screen")
                }

                composable(RoutePath.APP_ABOUT) {
                    Text("About Screen")
                }

                // Important dates
                composable(RoutePath.APP_IMPORTANT_DATES) {
                    Text("Important Dates Screen")
                }
            }
        }
    }
}
