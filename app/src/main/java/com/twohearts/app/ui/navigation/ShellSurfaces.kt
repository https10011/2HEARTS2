package com.twohearts.app.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The route currently shown by the shell.
 *
 * Phase 2: navigation surfaces need to know where they are without every
 * screen being threaded a route parameter. The shell publishes the current
 * route here so a shared component — most importantly the screen header —
 * can classify itself (top-level area, detail, or editing surface) and adopt
 * the treatment that matches its navigation role.
 *
 * Defaults to the start destination so previews and tests that render a
 * screen in isolation still resolve to a sane value.
 */
val LocalShellRoute = staticCompositionLocalOf { RoutePath.APP_HOME }

/**
 * ShellSurfaces — pure classification of a route's navigation role.
 *
 * The directive asks that "headers should not all become identical if their
 * navigation role differs", distinguishing top-level screens, secondary
 * screens, modal screens and detail screens. Deriving that role from the
 * route — rather than asking every screen to declare it — means a screen
 * cannot forget, and a new secondary screen gets the right treatment
 * automatically.
 */
object ShellSurfaces {

    /** The navigation role a surface plays. */
    enum class Role {
        /** A top-level area: one of the five primary destinations. */
        ROOT,

        /** A read-only step down from a primary area (a "detail" page). */
        DETAIL,

        /**
         * A task surface: creating, editing or logging something. The screen
         * is a thing to finish rather than a place to be, so it carries a
         * subtly different header.
         */
        MODAL,
    }

    /** Route suffixes that mean "this is a task in progress". */
    private val editorSuffixes = listOf("/add", "/edit", "/log", "/new")

    /** Extra task routes that do not follow the add/edit suffix convention. */
    private val editorRoutes = setOf(
        RoutePath.APP_PERIOD_LOG,
        RoutePath.APP_SETTINGS_IMPORT,
    )

    /** Classify [route]. */
    fun roleOf(route: String): Role = when {
        NavConfig.isPrimaryRoute(route) -> Role.ROOT
        route in editorRoutes -> Role.MODAL
        editorSuffixes.any { route.endsWith(it) } -> Role.MODAL
        else -> Role.DETAIL
    }

    /** True when the surface should show a back affordance. */
    fun showsBackAffordance(route: String): Boolean = roleOf(route) != Role.ROOT
}