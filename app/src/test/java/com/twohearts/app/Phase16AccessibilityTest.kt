package com.twohearts.app

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ThHeader
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.navigation.BottomNav
import com.twohearts.app.ui.navigation.NavConfig
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TextScalingLevel
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w320dp-h740dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase16AccessibilityTest {
    @Test
    fun textFieldsAndNavigationExposeTheirPurposeAndHaveUsableTargets() {
        Robolectric.buildActivity(ComponentActivity::class.java).use { activity ->
            activity.setup().get().setContent {
                TwoHeartsTheme(darkMode = true, textScalingLevel = TextScalingLevel.EXTRA_LARGE, reduceMotion = true) {
                    Column {
                        ThHeader("Write a note")
                        ThInput("", {}, label = "Title", placeholder = "Something to remember", error = "Enter a title")
                        BottomNav(RoutePath.APP_US, {})
                    }
                }
            }
            val window = activity.get().window.decorView
            shadowOf(Looper.getMainLooper()).idle()
            val metrics = activity.get().resources.displayMetrics
            window.measure(
                View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY),
            )
            window.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
            val root = checkNotNull(findComposeRoot(window))
            val nodes = root.semanticsOwner.getAllSemanticsNodes(true)
            val header = nodes.single { node ->
                node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == "Write a note" } == true
            }
            assertTrue(SemanticsProperties.Heading in header.config)
            val field = nodes.single { it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains("Title") == true }
            assertTrue(SemanticsActions.SetText in field.config)
            assertEquals("Enter a title", field.config[SemanticsProperties.Error])
            assertTrue(field.boundsInRoot.height >= with(root.density) { 48.dp.toPx() })
            val us = NavConfig.bottomNavItems.single { it.route == RoutePath.APP_US }
            val tab = nodes.single {
                it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(us.accessibilityLabel) == true
            }
            assertEquals(Role.Tab, tab.config[SemanticsProperties.Role])
            assertTrue(tab.config[SemanticsProperties.Selected])
            assertTrue(tab.boundsInRoot.height >= with(root.density) { 48.dp.toPx() })
        }
    }

    @Test
    fun semanticTextAndInteractionTonesMeetContrastThresholds() {
        val checks = mutableListOf<Triple<String, Float, Float>>()
        Robolectric.buildActivity(ComponentActivity::class.java).use { activity ->
            activity.setup().get().setContent {
                for (dark in listOf(false, true)) {
                    TwoHeartsTheme(darkMode = dark, reduceMotion = true) {
                        val palette = MaterialTheme.colorScheme
                        val extended = LocalTwoHeartsColors.current
                        checks += Triple("primary, dark=$dark", ratio(palette.onPrimary, palette.primary), 4.5f)
                        checks += Triple("selected, dark=$dark", ratio(palette.onPrimaryContainer, palette.primaryContainer), 4.5f)
                        checks += Triple("secondary, dark=$dark", ratio(palette.onSecondary, palette.secondary), 4.5f)
                        checks += Triple("category, dark=$dark", ratio(palette.onSecondaryContainer, palette.secondaryContainer), 4.5f)
                        checks += Triple("body, dark=$dark", ratio(palette.onBackground, palette.background), 4.5f)
                        checks += Triple("hint, dark=$dark", ratio(extended.textTertiary, palette.surface), 4.5f)
                        checks += Triple("date, dark=$dark", ratio(extended.goldInk, extended.goldBg), 4.5f)
                        checks += Triple("success, dark=$dark", ratio(extended.success, extended.successBg), 4.5f)
                        checks += Triple("error, dark=$dark", ratio(extended.error, extended.errorBg), 4.5f)
                        checks += Triple("field outline, dark=$dark", ratio(extended.borderStrong, palette.surface), 3f)
                    }
                }
            }
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue("Both themes were composed", checks.map { it.first }.distinct().size == 20)
            checks.forEach { (pair, measured, floor) ->
                assertTrue("$pair measured $measured:1 against $floor:1", measured >= floor)
            }
        }
    }

    private fun findComposeRoot(view: View): ViewRootForTest? {
        if (view is ViewRootForTest) return view
        if (view !is ViewGroup) return null
        for (index in 0 until view.childCount) {
            findComposeRoot(view.getChildAt(index))?.let { return it }
        }
        return null
    }

    private fun ratio(foreground: Color, background: Color): Float {
        val a = foreground.luminance() + .05f
        val b = background.luminance() + .05f
        return maxOf(a, b) / minOf(a, b)
    }
}
