package com.twohearts.app

import com.twohearts.app.ui.navigation.NavConfig
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.navigation.ShellSurfaces
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 2 shell and navigation invariants.
 *
 * These are the rules Phase 2 established, pinned so a later change cannot
 * quietly undo them. They test the classification logic the shell is built
 * on — the part that decides what a surface *is* — rather than the pixels,
 * which the render harness covers.
 */
class Phase2ShellNavigationTest {

    // ─── Navigation vocabulary ────────────────────────────────────────

    @Test
    fun `bottom bar holds Home Us Notes and More`() {
        val items = NavConfig.bottomNavItems
        assertEquals(4, items.size)
        assertEquals(
            listOf(
                RoutePath.APP_HOME,
                RoutePath.APP_US,
                RoutePath.APP_NOTES,
                RoutePath.APP_MORE,
            ),
            items.map { it.route },
        )
    }

    @Test
    fun `Us retains the branded tab`() {
        val branded = NavConfig.bottomNavItems.filter { it.isBrand }
        assertEquals(1, branded.size)
        assertEquals(RoutePath.APP_US, branded.single().route)
    }

    @Test
    fun `every tab carries a distinct label and icon`() {
        val items = NavConfig.bottomNavItems
        items.forEach { item ->
            assertTrue(
                "missing label for ${item.route}",
                item.label.isNotBlank(),
            )
            assertTrue(
                "missing accessibility label for ${item.route}",
                item.accessibilityLabel.isNotBlank(),
            )
        }
        assertEquals(
            "labels must be distinct",
            items.size,
            items.map { it.label }.distinct().size,
        )
        assertEquals(
            "glyphs must be distinct",
            items.size,
            items.map { it.icon }.distinct().size,
        )
    }

    // ─── Navigation role classification ───────────────────────────────

    @Test
    fun `all primary areas classify as root`() {
        NavConfig.bottomNavItems.forEach { item ->
            assertEquals(
                "${item.route} should be ROOT",
                ShellSurfaces.Role.ROOT,
                ShellSurfaces.roleOf(item.route),
            )
        }
    }

    @Test
    fun `root areas do not show a back affordance`() {
        // A primary destination has nothing above it and the bottom bar is
        // showing; a back arrow there is the double affordance the approved
        // reference screens avoid.
        NavConfig.bottomNavItems.forEach { item ->
            assertFalse(
                "${item.route} must not show back",
                ShellSurfaces.showsBackAffordance(item.route),
            )
        }
    }

    @Test
    fun `detail surfaces classify as detail and show back`() {
        val detailRoutes = listOf(
            RoutePath.APP_MEMORIES_DETAIL.replace(":memoryId", "m1"),
            RoutePath.APP_NOTES_DETAIL.replace(":noteId", "n1"),
            RoutePath.APP_TIMELINE_DETAIL.replace(":eventId", "e1"),
            RoutePath.APP_REMINDERS_DETAIL.replace(":reminderId", "r1"),
            RoutePath.APP_PLACES_DETAIL.replace(":placeId", "pl1"),
            RoutePath.APP_VAULT_CONTENT.replace(":contentId", "c1"),
            RoutePath.APP_MEMORIES,
            RoutePath.APP_TIMELINE,
            RoutePath.APP_REMINDERS,
            RoutePath.APP_PLACES,
            RoutePath.APP_MOOD,
            RoutePath.APP_PERIOD,
            RoutePath.APP_VAULT,
            RoutePath.APP_SETTINGS,
            RoutePath.APP_ABOUT,
            RoutePath.APP_SEARCH,
        )
        detailRoutes.forEach { route ->
            assertEquals(
                "$route should be DETAIL",
                ShellSurfaces.Role.DETAIL,
                ShellSurfaces.roleOf(route),
            )
            assertTrue(
                "$route should show back",
                ShellSurfaces.showsBackAffordance(route),
            )
        }
    }

    @Test
    fun `editor surfaces classify as modal and show back`() {
        val editorRoutes = listOf(
            RoutePath.APP_NOTES_ADD,
            RoutePath.APP_NOTES_EDIT.replace(":noteId", "n1"),
            RoutePath.APP_MEMORIES_ADD,
            RoutePath.APP_MEMORIES_EDIT.replace(":memoryId", "m1"),
            RoutePath.APP_TIMELINE_ADD,
            RoutePath.APP_REMINDERS_ADD,
            RoutePath.APP_PLACES_ADD,
            RoutePath.APP_MOOD_ADD,
            RoutePath.APP_PERIOD_LOG,
            RoutePath.APP_SETTINGS_IMPORT,
        )
        editorRoutes.forEach { route ->
            assertEquals(
                "$route should be MODAL",
                ShellSurfaces.Role.MODAL,
                ShellSurfaces.roleOf(route),
            )
            assertTrue(
                "$route should show back",
                ShellSurfaces.showsBackAffordance(route),
            )
        }
    }

    @Test
    fun `period settings is a detail surface, not a task`() {
        // Period settings configures a feature rather than completing a task,
        // so it takes the detail treatment (and keeps its back affordance).
        assertEquals(
            ShellSurfaces.Role.DETAIL,
            ShellSurfaces.roleOf(RoutePath.APP_PERIOD_SETTINGS),
        )
        assertTrue(ShellSurfaces.showsBackAffordance(RoutePath.APP_PERIOD_SETTINGS))
    }

    @Test
    fun `unrecognised routes default to a detail surface`() {
        // A new screen added later must get the safe treatment by default:
        // a back affordance, so the user is never stranded.
        val unknown = ShellSurfaces.roleOf("/app/something/unmapped")
        assertEquals(ShellSurfaces.Role.DETAIL, unknown)
        assertTrue(ShellSurfaces.showsBackAffordance("/app/something/unmapped"))
    }

    @Test
    fun `notifications retains its route as a detail with Back`() {
        assertEquals(
            ShellSurfaces.Role.DETAIL,
            ShellSurfaces.roleOf(RoutePath.APP_NOTIFICATIONS),
        )
        assertTrue(ShellSurfaces.showsBackAffordance(RoutePath.APP_NOTIFICATIONS))
    }

    @Test
    fun `shell route default resolves to a top-level area`() {
        // LocalShellRoute's default must stay valid, because previews and
        // isolated tests render screens without a shell around them.
        assertEquals(
            ShellSurfaces.Role.ROOT,
            ShellSurfaces.roleOf(RoutePath.APP_HOME),
        )
    }

    // ─── Yuki boundary ────────────────────────────────────────────────

    @Test
    fun `yuki is reachable and treated as a detail surface`() {
        // Phase 2 verifies the shell can navigate to and from Yuki without
        // redesigning it. It is a step in from a primary area, so it keeps
        // the standard back affordance.
        assertEquals(
            ShellSurfaces.Role.DETAIL,
            ShellSurfaces.roleOf(RoutePath.APP_YUKI),
        )
        assertTrue(ShellSurfaces.showsBackAffordance(RoutePath.APP_YUKI))
    }
}
