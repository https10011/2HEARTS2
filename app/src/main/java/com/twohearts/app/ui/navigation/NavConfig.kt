package com.twohearts.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * NavConfig — destination vocabulary matching legacy navConfig.
 *
 * Defines all navigation destinations with:
 * - Route path
 * - Display name
 * - Icon
 * - Group (for bottom nav)
 */
object NavConfig {

    /**
     * Bottom navigation destinations.
     */
    val bottomNavItems = listOf(
        NavItem(
            route = RoutePath.APP_HOME,
            label = "Home",
            icon = Icons.Default.Home
        ),
        NavItem(
            route = RoutePath.APP_NOTIFICATIONS,
            label = "Notifications",
            icon = Icons.Default.Notifications
        ),
        NavItem(
            route = RoutePath.APP_US,
            label = "Us",
            icon = Icons.Default.Favorite,
            isCenter = true
        ),
        NavItem(
            route = RoutePath.APP_NOTES,
            label = "Notes",
            icon = Icons.Default.Note
        ),
        NavItem(
            route = RoutePath.APP_MORE,
            label = "More",
            icon = Icons.Default.MoreHoriz
        )
    )

    /**
     * All navigation destinations.
     */
    val destinations = mapOf(
        // Home
        RoutePath.APP_HOME to Destination("Home", Icons.Default.Home),

        // Us / Relationship
        RoutePath.APP_US to Destination("Us", Icons.Default.Favorite),
        RoutePath.APP_MEMORIES to Destination("Memories", Icons.Default.PhotoLibrary),
        RoutePath.APP_TIMELINE to Destination("Timeline", Icons.Default.Timeline),
        RoutePath.APP_IMPORTANT_DATES to Destination("Important Dates", Icons.Default.CalendarToday),
        RoutePath.APP_PLACES to Destination("Places", Icons.Default.Place),
        RoutePath.APP_MOOD to Destination("Mood", Icons.Default.Mood),
        RoutePath.APP_PERIOD to Destination("Period Tracker", Icons.Default.CalendarMonth),
        RoutePath.APP_VAULT to Destination("Vault", Icons.Default.Lock),

        // Content
        RoutePath.APP_NOTES to Destination("Notes", Icons.Default.Note),
        RoutePath.APP_REMINDERS to Destination("Reminders", Icons.Default.Alarm),

        // Fun
        RoutePath.APP_YUKI to Destination("Yuki", Icons.Default.Pets),
        RoutePath.APP_GAMES to Destination("Games", Icons.Default.SportsEsports),

        // System
        RoutePath.APP_NOTIFICATIONS to Destination("Notifications", Icons.Default.Notifications),
        RoutePath.APP_SEARCH to Destination("Search", Icons.Default.Search),
        RoutePath.APP_MORE to Destination("More", Icons.Default.MoreHoriz),

        // Settings
        RoutePath.APP_SETTINGS to Destination("Settings", Icons.Default.Settings),
        RoutePath.APP_SETTINGS_PROFILE to Destination("Profile Settings", Icons.Default.Person),
        RoutePath.APP_SETTINGS_RELATIONSHIP to Destination("Relationship Settings", Icons.Default.FavoriteBorder),
        RoutePath.APP_SETTINGS_APPEARANCE to Destination("Appearance Settings", Icons.Default.Palette),
        RoutePath.APP_SETTINGS_NOTIFICATIONS to Destination("Notification Settings", Icons.Default.Notifications),
        RoutePath.APP_SETTINGS_SECURITY to Destination("Security Settings", Icons.Default.Security),
        RoutePath.APP_SETTINGS_STORAGE to Destination("Storage Settings", Icons.Default.Storage),
        RoutePath.APP_SETTINGS_IMPORT to Destination("Import", Icons.Default.FileUpload),
        RoutePath.APP_ABOUT to Destination("About", Icons.Default.Info)
    )

    /**
     * Get destination for a route.
     */
    fun getDestination(route: String): Destination? {
        return destinations[route]
    }
}

/**
 * NavItem — bottom navigation item.
 */
data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false
)

/**
 * Destination — navigation destination.
 */
data class Destination(
    val label: String,
    val icon: ImageVector
)
