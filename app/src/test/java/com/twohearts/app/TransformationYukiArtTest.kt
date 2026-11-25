package com.twohearts.app

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.ui.screens.yuki.YukiArt
import com.twohearts.app.ui.screens.yuki.YukiHub
import com.twohearts.app.ui.screens.yuki.YukiPose
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationYukiArtTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun packagedAtlasMapsEverySourceAndDerivedPoseAndKeepsItsGroundRegistration() {
        val atlas = app.assets.open("yuki/atlas.json").bufferedReader().use { JSONObject(it.readText()) }
        val poses = atlas.getJSONObject("poses")
        val clips = atlas.getJSONObject("clips")
        val names = YukiPose.entries.map { it.assetName.removeSuffix(".png") }.toSet()
        assertEquals(1, atlas.getInt("version"))
        assertEquals(names, poses.keys().asSequence().toSet())
        assertEquals(names.map { "$it.png" }.toSet() + "atlas.json", app.assets.list("yuki").orEmpty().toSet())
        val referenced = clips.keys().asSequence().flatMap { name ->
            val frames = clips.getJSONArray(name)
            (0 until frames.length()).asSequence().map(frames::getString)
        }.toSet()
        assertEquals(names, referenced)
        val supplied = names.map { poses.getJSONObject(it) }.filter { it.has("source") }
        assertEquals(21, supplied.map { it.getString("source") }.distinct().size)
        assertTrue(supplied.all { it.getString("sha256").matches(Regex("[0-9a-f]{64}")) })

        val tile = atlas.getJSONArray("tile")
        val ground = atlas.getJSONArray("ground")
        val groundX = ground.getInt(0)
        val groundY = ground.getInt(1)
        for (pose in YukiPose.entries) {
            val sprite = requireNotNull(YukiArt.load(app, pose)) { "Missing ${pose.name}" }
            assertEquals(tile.getInt(0), sprite.width)
            assertEquals(tile.getInt(1), sprite.height)
            val colors = pixels(sprite)
            val alpha = colors.map(Color::alpha)
            val silhouette = alpha.indices.filter { alpha[it] >= 128 }
            assertTrue(silhouette.size > 20_000)
            assertTrue(alpha.any { it in 1..127 })
            assertTrue(alpha.count { it == 0 } > 90_000)
            assertTrue(silhouette.any { it % sprite.width == groundX })
            assertTrue(silhouette.minOf { it % sprite.width } >= 4)
            assertTrue(silhouette.maxOf { it % sprite.width } <= sprite.width - 5)
            assertTrue(silhouette.maxOf { it / sprite.width } in groundY - 1..groundY)
        }
    }

    @Test
    fun authoredBlinkEarAndTailFramesLeaveTheOriginalBodyAndFloorInPlace() {
        val base = pixels(requireNotNull(YukiArt.load(app, YukiPose.SIT)))
        val anchors = listOf(
            Triple(YukiPose.SIT_HALF_BLINK, 150..228, 114..157),
            Triple(YukiPose.SIT_BLINK, 150..228, 114..157),
            Triple(YukiPose.SIT_EAR_FLICK, 200..260, 40..120),
            Triple(YukiPose.SIT_TAIL_LEFT, 275..360, 119..279),
            Triple(YukiPose.SIT_TAIL_RIGHT, 275..360, 119..279),
        )
        for ((pose, columns, rows) in anchors) {
            val derived = pixels(requireNotNull(YukiArt.load(app, pose)))
            val changed = base.indices.filter { base[it] != derived[it] }
            assertTrue("${pose.name} needs visible motion", changed.size >= 40)
            assertTrue("${pose.name} should preserve the art outside its feature", changed.all {
                it % YukiArt.WIDTH in columns && it / YukiArt.WIDTH in rows
            })
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w411dp-h891dp-mdpi")
    fun nativeRoomDisplaysRegisteredSittingAndGroomingPosesInLight() {
        renderRoom("light-sitting", YukiActivity.IDLE, YukiPose.SIT, dark = false, scale = 1f)
        renderRoom("light-grooming", YukiActivity.GROOMING, YukiPose.GROOM, dark = false, scale = 1f)
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun shortDarkRoomDisplaysRegisteredSleepingAndDrinkingPosesAtLargeType() {
        renderRoom("dark-sleeping", YukiActivity.SLEEPING, YukiPose.SLEEP, dark = true, scale = 2f)
        renderRoom("dark-drinking", YukiActivity.DRINKING, YukiPose.DRINK, dark = true, scale = 2f)
    }

    private fun renderRoom(name: String, activity: YukiActivity, pose: YukiPose, dark: Boolean, scale: Float) {
        requireNotNull(YukiArt.load(app, pose))
        NativeComposeScene(File("build/transformation-17")).use { scene ->
            scene.setContent {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = scale, reduceMotion = true) {
                    YukiHub(
                        YukiState.createDefault().copy(activity = activity), null,
                        working = false, failed = false, animate = false,
                        onBack = {}, onAction = {}, onToy = {}, onAccessory = {},
                    )
                }
            }
            scene.capture(scene.activityView, name)
            assertTrue(scene.activityDescription("Yuki, an orange tabby"))
            assertTrue(scene.activityText("Yuki’s room"))
        }
    }

    private fun pixels(bitmap: Bitmap): IntArray = IntArray(bitmap.width * bitmap.height).apply {
        bitmap.getPixels(this, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    }
}
