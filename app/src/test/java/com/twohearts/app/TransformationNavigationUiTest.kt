package com.twohearts.app

import android.os.Looper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.rememberTextMeasurer
import com.twohearts.app.ui.navigation.AppShell
import com.twohearts.app.ui.navigation.NavConfig
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.navigation.bottomNavLabelStyle
import com.twohearts.app.ui.screens.home.HomeContent
import com.twohearts.app.ui.screens.more.MoreScreen
import com.twohearts.app.ui.theme.TwoHeartsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h780dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationNavigationUiTest {
    @Test
    fun homeAndMoreExposeSearchAndNotifications() {
        val opened = mutableListOf<String>()
        renderShell(
            route = RoutePath.APP_HOME,
            onTabClick = { opened += it },
            content = {
                HomeContent(
                    owner = null, partner = null, relationship = null, notes = emptyList(),
                    reminders = emptyList(), memories = emptyList(), moods = emptyList(), dates = emptyList(),
                    onNavigate = { opened += it },
                )
            },
        ) { nodes ->
            nodes.single { it.hasDescription("Notifications") }.click()
            nodes.single { it.hasDescription("Search all records") }.click()
            nodes.single { it.hasDescription("More") }.click()
        }
        assertEquals(listOf(RoutePath.APP_NOTIFICATIONS, RoutePath.APP_SEARCH, RoutePath.APP_MORE), opened)

        opened.clear()
        renderShell(route = RoutePath.APP_MORE, content = { MoreScreen(onNavigate = { opened += it }) }) { nodes ->
            nodes.single { it.hasText("Notifications") }.click()
            nodes.single { it.hasText("Search") }.click()
        }
        assertEquals(listOf(RoutePath.APP_NOTIFICATIONS, RoutePath.APP_SEARCH), opened)
    }

    @Test
    fun actualBarAppearsOnlyWhenKeyboardInsetsAreEmpty() {
        for (keyboardHeight in listOf(0, 250)) {
            renderShell(RoutePath.APP_NOTES, keyboardHeight = keyboardHeight) { nodes ->
                assertEquals(keyboardHeight == 0, nodes.any { it.hasDescription("Notes") })
            }
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h480dp-mdpi")
    fun narrowLargeTypeKeepsFourSeparatedTabsInReadingOrder() {
        val opened = mutableListOf<String>()
        val labelWidths = mutableMapOf<String, Int>()
        renderShell(RoutePath.APP_US, onTabClick = { opened += it }, content = {
            val measurer = rememberTextMeasurer()
            val labelStyle = bottomNavLabelStyle()
            NavConfig.bottomNavItems.forEach { item ->
                labelWidths[item.accessibilityLabel] =
                    measurer.measure(item.label, labelStyle, maxLines = 1).size.width
            }
        }) { nodes ->
            val tabs = nodes.filter { it.config.getOrNull(SemanticsProperties.Role) == Role.Tab }
            assertEquals(NavConfig.bottomNavItems.map { it.accessibilityLabel }, tabs.map {
                it.config[SemanticsProperties.ContentDescription].single()
            })
            assertEquals(listOf(false, true, false, false), tabs.map { it.config[SemanticsProperties.Selected] })
            assertTrue(tabs.zipWithNext().all { (a, b) -> b.boundsInRoot.left - a.boundsInRoot.right >= 4f })
            tabs.forEach { tab ->
                val label = tab.config[SemanticsProperties.ContentDescription].single()
                val required = labelWidths.getValue(label)
                assertTrue("$label requires ${required}px in ${tab.boundsInRoot.width}px", tab.boundsInRoot.width >= required)
                tab.click()
            }
        }
        assertEquals(NavConfig.bottomNavItems.map { it.route }, opened)
    }

    private fun renderShell(
        route: String,
        keyboardHeight: Int = 0,
        onTabClick: (String) -> Unit = {},
        content: @Composable () -> Unit = {},
        assertNodes: (List<SemanticsNode>) -> Unit,
    ) {
        Robolectric.buildActivity(ComponentActivity::class.java).use { controller ->
            val activity = controller.setup().get()
            activity.enableEdgeToEdge()
            activity.setContent {
                TwoHeartsTheme(darkTheme = true, textSizeScale = 2f, reduceMotion = true) {
                    AppShell(
                        route, onTabClick, {}, canNavigateBack = false,
                        keyboardInsets = WindowInsets(bottom = keyboardHeight),
                    ) { content() }
                }
            }
            assertNodes(layoutNodes(activity))
        }
    }

    private fun layoutNodes(activity: ComponentActivity): List<SemanticsNode> {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(48))
        val view = activity.window.decorView
        val metrics = activity.resources.displayMetrics
        view.measure(
            View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        try {
            view.draw(Canvas(bitmap))
        } finally {
            bitmap.recycle()
        }
        return checkNotNull(composeRoot(view)).semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)
    }

    private fun composeRoot(view: View): ViewRootForTest? {
        if (view is ViewRootForTest) return view
        if (view is ViewGroup) {
            repeat(view.childCount) { index -> composeRoot(view.getChildAt(index))?.let { return it } }
        }
        return null
    }

    private fun SemanticsNode.hasDescription(label: String) =
        config.getOrNull(SemanticsProperties.ContentDescription)?.contains(label) == true

    private fun SemanticsNode.hasText(label: String) =
        config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true

    private fun SemanticsNode.click() {
        assertTrue("Control ${config} has bounds $boundsInRoot", boundsInRoot.width >= 48f && boundsInRoot.height >= 48f)
        assertTrue(checkNotNull(config.getOrNull(SemanticsActions.OnClick)?.action).invoke())
    }
}
