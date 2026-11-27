package com.twohearts.app

import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.CardTone
import com.twohearts.app.ui.components.ThConfirmDialog
import com.twohearts.app.ui.components.ThSectionHeader
import com.twohearts.app.ui.components.ThSurfaceCard
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w320dp-h420dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationProductAuditTest {
    private val out = File("build/transformation-20")

    @Test
    fun largeConfirmationKeepsBothActionsVisibleWhileTheExplanationScrolls() {
        val open = mutableStateOf(true)
        val busy = mutableStateOf(false)
        var actions = 0
        NativeComposeScene(out).use { scene ->
            scene.setContent {
                TwoHeartsTheme(darkTheme = true, textSizeScale = 2f, reduceMotion = true) {
                    Surface {
                        ThConfirmDialog(
                            open = open.value,
                            onClose = { open.value = false },
                            label = "Remove reminder",
                            title = "Remove this reminder?",
                            description = "Removing this reminder also clears scheduled alerts. ".repeat(10).trim(),
                            actionLabel = "Remove",
                            onAction = {
                                actions++
                                busy.value = true
                            },
                            busy = busy.value,
                            busyLabel = "Removing…",
                        )
                    }
                }
            }
            val view = scene.dialogView()
            scene.layout(view)
            val nodes = checkNotNull(view.findViewOfType(ViewRootForTest::class.java))
                .semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)
            assertTrue(nodes.any { it.config.getOrNull(SemanticsProperties.PaneTitle) == "Remove reminder" })
            for (label in listOf("Remove", "Cancel")) {
                val action = nodes.first {
                    it.config.getOrNull(SemanticsActions.OnClick) != null &&
                        it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == label } == true
                }
                assertTrue(action.boundsInRoot.height >= 48f)
                assertTrue(action.boundsInRoot.bottom <= view.height)
            }
            scene.captureDialog("confirmation-short-dark-200")
            assertTrue(scene.scrollDialogBy(500f))
            assertTrue(scene.clickDialog("Remove"))
            assertEquals(1, actions)
            scene.captureDialog("confirmation-busy-dark-200")
            assertFalse(scene.clickDialog("Removing…"))
            assertFalse(scene.clickDialog("Cancel"))
            (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed()
            assertTrue(open.value)
            busy.value = false
            scene.dialogView()
            assertTrue(scene.clickDialog("Cancel"))
            assertFalse(open.value)
            assertEquals(1, actions)
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w411dp-h891dp-mdpi")
    fun darkCardsSetReadableForegroundsAndOutlinesInheritTheirSurface() {
        NativeComposeScene(out).use { scene ->
            scene.setContent {
                TwoHeartsTheme(darkTheme = true) {
                    val scheme = MaterialTheme.colorScheme
                    val colors = LocalTwoHeartsColors.current
                    Surface {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ThSectionHeader("Dark surfaces")
                            CompositionLocalProvider(LocalContentColor provides Color.Black) {
                                val opaqueTones = mapOf(
                                    CardTone.PLAIN to scheme.surface,
                                    CardTone.WARM to colors.surfaceWarm,
                                    CardTone.BLUSH to colors.surfaceBlush,
                                    CardTone.GOLD to colors.goldBg,
                                    CardTone.BRAND to colors.primaryAction,
                                )
                                opaqueTones.forEach { (tone, background) ->
                                    ThSurfaceCard(tone = tone) {
                                        val foreground = LocalContentColor.current
                                        val luminances = listOf(foreground.luminance(), background.luminance())
                                        assertTrue(
                                            "$tone text contrast",
                                            (luminances.max() + 0.05f) / (luminances.min() + 0.05f) >= 4.5f,
                                        )
                                        Text("TwoHearts", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            ThSurfaceCard(tone = CardTone.OUTLINE) {
                                assertEquals(scheme.onSurface, LocalContentColor.current)
                                Text("TwoHearts")
                            }
                        }
                    }
                }
            }
            scene.capture(scene.activityView, "surface-contrast-dark")
            val nodes = checkNotNull(scene.activityView.findViewOfType(ViewRootForTest::class.java))
                .semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)
            assertTrue(nodes.any { it.config.getOrNull(SemanticsProperties.Heading) != null &&
                it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == "Dark surfaces" } == true })
        }
    }
}
