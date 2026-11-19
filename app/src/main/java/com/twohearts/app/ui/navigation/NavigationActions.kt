package com.twohearts.app.ui.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect

internal fun NavController.selectPrimaryRoute(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(RoutePath.APP_HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

internal fun NavController.openNotifications() {
    navigate(RoutePath.APP_NOTIFICATIONS) { launchSingleTop = true }
}

internal fun NavController.openSearchResult(type: String, id: String) {
    val collection = when (type) {
        "note" -> RoutePath.APP_NOTES
        "memory" -> RoutePath.APP_MEMORIES
        "reminder" -> RoutePath.APP_REMINDERS
        "place" -> RoutePath.APP_PLACES
        "timeline" -> RoutePath.APP_TIMELINE
        else -> return
    }
    navigate("$collection/${Uri.encode(id)}")
}

internal fun NavGraphBuilder.legacyGameRedirects(navController: NavController) {
    val legacyRoutes = listOf(
        RoutePath.APP_GAMES,
        RoutePath.APP_GAMES_MEMORY_MATCH,
        RoutePath.APP_GAMES_WORD_SCRAMBLE,
        RoutePath.APP_GAMES_COUPLE_TRIVIA,
        RoutePath.APP_GAMES_WHO_KNOWS,
        RoutePath.APP_GAMES_WOULD_YOU_RATHER,
        RoutePath.APP_GAMES_THIS_OR_THAT,
        RoutePath.APP_GAMES_GUESS_MY_ANSWER,
        RoutePath.APP_GAMES_CASUAL_TRIVIA,
        RoutePath.APP_GAMES_RIDDLE_ROOM,
        RoutePath.APP_GAMES_RESULTS,
    )
    for (route in legacyRoutes) {
        composable(route) {
            LaunchedEffect(route) { navController.replaceLegacyGame(route) }
        }
    }
}

internal fun NavController.replaceLegacyGame(route: String) {
    if (currentDestination?.route != route) return
    navigate(RoutePath.APP_YUKI) {
        popUpTo(route) { inclusive = true }
        launchSingleTop = true
    }
}
