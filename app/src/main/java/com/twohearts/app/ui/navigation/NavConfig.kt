package com.twohearts.app.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.twohearts.app.ui.components.ThIcons

/** Display names, icons, and navigation roles for app destinations. */
object NavConfig {

    /** The four peer destinations, with a branded entry for the relationship hub. */
    val bottomNavItems = listOf(
        NavItem(
            id = "home",
            route = RoutePath.APP_HOME,
            label = "Home",
            icon = ThIcons.Home,
            isPrimary = true,
        ),
        NavItem(
            id = "us",
            route = RoutePath.APP_US,
            label = "Us",
            accessibilityLabel = "Us — your relationship space",
            icon = ThIcons.HeartFilled,
            isBrand = true,
            isPrimary = true,
        ),
        NavItem(
            id = "notes",
            route = RoutePath.APP_NOTES,
            label = "Notes",
            icon = ThIcons.Note,
            isPrimary = true,
        ),
        NavItem(
            id = "more",
            route = RoutePath.APP_MORE,
            label = "More",
            icon = ThIcons.More,
            isPrimary = true,
        ),
    )

    /**
     * Routes that are *primary* areas of the app.
     *
     * The shell shows the bottom navigation on these and only these. Built
     * from [bottomNavItems] so the bar and the primary-area concept cannot
     * drift apart.
     */
    val primaryRoutes: Set<String> = bottomNavItems.map { it.route }.toSet()

    /** True when [route] is a primary area (bottom bar visible). */
    fun isPrimaryRoute(route: String): Boolean = route in primaryRoutes

    /**
     * Secondary destinations, keyed by route. Used for labels and glyphs
     * outside the bottom bar (search results, deep links, future hubs).
     */
    val destinations = mapOf(
        // Primary
        RoutePath.APP_HOME to Destination("Home", ThIcons.Home),
        RoutePath.APP_US to Destination("Us", ThIcons.HeartFilled),
        RoutePath.APP_NOTES to Destination("Notes", ThIcons.Note),
        RoutePath.APP_MORE to Destination("More", ThIcons.More),

        // Relationship
        RoutePath.APP_MEMORIES to Destination("Memories", ThIcons.PhotoLibrary),
        RoutePath.APP_TIMELINE to Destination("Timeline", ThIcons.Timeline),
        RoutePath.APP_IMPORTANT_DATES to Destination("Important Dates", ThIcons.Calendar),
        RoutePath.APP_PLACES to Destination("Places", ThIcons.Place),
        RoutePath.APP_MOOD to Destination("Mood", ThIcons.Smile),
        RoutePath.APP_PERIOD to Destination("Period Tracker", ThIcons.CalendarMonth),
        RoutePath.APP_VAULT to Destination("Vault", ThIcons.Lock),

        // Content
        RoutePath.APP_REMINDERS to Destination("Reminders", ThIcons.Alarm),

        // Companion
        RoutePath.APP_YUKI to Destination("Yuki", ThIcons.Cat),

        // System
        RoutePath.APP_NOTIFICATIONS to Destination("Notifications", ThIcons.Bell),
        RoutePath.APP_SEARCH to Destination("Search", ThIcons.Search),

        // Settings
        RoutePath.APP_SETTINGS to Destination("Settings", ThIcons.Settings),
        RoutePath.APP_SETTINGS_PROFILE to Destination("Profile Settings", ThIcons.Person),
        RoutePath.APP_SETTINGS_RELATIONSHIP to Destination("Relationship Settings", ThIcons.Heart),
        RoutePath.APP_SETTINGS_APPEARANCE to Destination("Appearance Settings", ThIcons.Palette),
        RoutePath.APP_SETTINGS_NOTIFICATIONS to Destination("Notification Settings", ThIcons.Bell),
        RoutePath.APP_SETTINGS_SECURITY to Destination("Security Settings", ThIcons.Security),
        RoutePath.APP_SETTINGS_STORAGE to Destination("Storage Settings", ThIcons.Storage),
        RoutePath.APP_SETTINGS_IMPORT to Destination("Import and export", ThIcons.File),
        RoutePath.APP_ABOUT to Destination("About", ThIcons.Info),
    )

    /** Resolve a route's destination, if it has one. */
    fun getDestination(route: String): Destination? = destinations[route]
}

/**
 * NavItem — one bottom-navigation destination.
 *
 * @param id stable semantic key (used for test tags and keys).
 * @param route the route this destination opens.
 * @param label the full display name, and the accessible name unless
 *   [accessibilityLabel] overrides it.
 * @param accessibilityLabel spoken name, when it should read differently
 *   from the visible text.
 * @param icon glyph from the canonical [ThIcons] vocabulary.
 * @param isBrand true for the branded relationship tab.
 * @param isPrimary true for top-level areas where the bar is visible.
 */
data class NavItem(
    val id: String,
    val route: String,
    val label: String,
    val accessibilityLabel: String = label,
    val icon: ImageVector,
    val isBrand: Boolean = false,
    val isPrimary: Boolean = false,
)

/** Destination — a route's display name and glyph. */
data class Destination(
    val label: String,
    val icon: ImageVector,
)
