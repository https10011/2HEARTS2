package com.twohearts.app

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.ui.navigation.NavConfig
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.navigation.ShellLayout
import com.twohearts.app.ui.navigation.legacyGameRedirects
import com.twohearts.app.ui.navigation.openNotifications
import com.twohearts.app.ui.navigation.openSearchResult
import com.twohearts.app.ui.navigation.replaceLegacyGame
import com.twohearts.app.ui.navigation.selectPrimaryRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransformationNavigationTest {
    private val searchPatterns = mapOf(
        "note" to "${RoutePath.APP_NOTES}/{noteId}",
        "memory" to "${RoutePath.APP_MEMORIES}/{memoryId}",
        "reminder" to "${RoutePath.APP_REMINDERS}/{reminderId}",
        "place" to "${RoutePath.APP_PLACES}/{placeId}",
        "timeline" to "${RoutePath.APP_TIMELINE}/{eventId}",
    )

    @Test
    fun searchRecordLinksOpenAllTypesAndBackReturnsToSearch() {
        val nav = controller()
        nav.selectPrimaryRoute(RoutePath.APP_MORE)
        nav.navigate(RoutePath.APP_SEARCH)
        val id = "old/import #2? 你好"
        for ((type, pattern) in searchPatterns) {
            nav.openSearchResult(type, id)
            assertEquals(pattern, nav.currentDestination?.route)
            val key = pattern.substringAfter('{').substringBefore('}')
            assertEquals(id, nav.currentBackStackEntry?.arguments?.getString(key))
            assertTrue(nav.popBackStack())
            assertEquals(RoutePath.APP_SEARCH, nav.currentDestination?.route)
        }
        nav.openSearchResult("unsupported", id)
        assertEquals(RoutePath.APP_SEARCH, nav.currentDestination?.route)
        assertTrue(nav.popBackStack())
        assertEquals(RoutePath.APP_MORE, nav.currentDestination?.route)
    }

    @Test
    fun notificationEntryDeduplicatesAndRestoresTheSourceAfterRecreation() {
        val nav = controller()
        nav.selectPrimaryRoute(RoutePath.APP_NOTES)
        nav.openNotifications()
        nav.openNotifications()
        assertEquals(RoutePath.APP_NOTIFICATIONS, nav.currentDestination?.route)

        val recreated = controller(checkNotNull(nav.saveState()))
        assertEquals(RoutePath.APP_NOTIFICATIONS, recreated.currentDestination?.route)
        assertTrue(recreated.popBackStack())
        assertEquals(RoutePath.APP_NOTES, recreated.currentDestination?.route)
        assertTrue(recreated.popBackStack())
        assertEquals(RoutePath.APP_HOME, recreated.currentDestination?.route)
    }

    @Test
    fun selectingTabsRetainsSavedStateAndDoesNotPushDuplicateDestinations() {
        val nav = controller()
        nav.selectPrimaryRoute(RoutePath.APP_NOTES)
        checkNotNull(nav.currentBackStackEntry).savedStateHandle["filter"] = "love letter"
        nav.selectPrimaryRoute(RoutePath.APP_US)
        nav.selectPrimaryRoute(RoutePath.APP_NOTES)
        nav.selectPrimaryRoute(RoutePath.APP_NOTES)
        assertEquals("love letter", checkNotNull(nav.currentBackStackEntry).savedStateHandle.get<String>("filter"))
        assertTrue(nav.popBackStack())
        assertEquals(RoutePath.APP_HOME, nav.currentDestination?.route)
    }

    @Test
    fun allLegacyGameRoutesReplaceOnlyTheAliasAndLeaveTheOrigin() {
        val nav = controller()
        nav.selectPrimaryRoute(RoutePath.APP_US)
        val aliases = listOf(
            "", "/memory-match", "/word-scramble", "/couple-trivia", "/who-knows-who-better",
            "/would-you-rather", "/this-or-that", "/guess-my-answer", "/casual-trivia", "/riddle-room", "/results",
        )
        for (suffix in aliases) {
            val alias = "${RoutePath.APP_GAMES}$suffix"
            nav.navigate(alias)
            nav.replaceLegacyGame(alias)
            assertEquals(RoutePath.APP_YUKI, nav.currentDestination?.route)
            assertTrue(nav.popBackStack())
            assertEquals(RoutePath.APP_US, nav.currentDestination?.route)
        }
    }

    @Test
    fun keyboardOwnsBottomInsetOnTabsAndDetailsNeverShowTheBar() {
        for (tab in NavConfig.bottomNavItems) {
            assertTrue(ShellLayout.showsBottomNav(tab.route, keyboardVisible = false))
            assertFalse(ShellLayout.showsBottomNav(tab.route, keyboardVisible = true))
        }
        for (route in listOf(RoutePath.APP_NOTES_ADD, RoutePath.APP_SEARCH, RoutePath.APP_NOTIFICATIONS)) {
            assertFalse(ShellLayout.showsBottomNav(route, keyboardVisible = false))
            assertFalse(ShellLayout.showsBottomNav(route, keyboardVisible = true))
        }
    }

    private fun controller(savedState: Bundle? = null): NavHostController {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return NavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            setViewModelStore(ViewModelStore())
            if (savedState != null) restoreState(savedState)
            graph = createGraph(startDestination = RoutePath.APP_HOME) {
                NavConfig.bottomNavItems.forEach { composable(it.route) {} }
                composable(RoutePath.APP_SEARCH) {}
                composable(RoutePath.APP_NOTIFICATIONS) {}
                searchPatterns.values.forEach { composable(it) {} }
                legacyGameRedirects(this@apply)
            }
        }
    }
}
