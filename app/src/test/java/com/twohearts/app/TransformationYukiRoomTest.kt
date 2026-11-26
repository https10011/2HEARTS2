package com.twohearts.app

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.game.YukiAction
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.data.game.YukiToy
import com.twohearts.app.services.game.YukiService
import com.twohearts.app.ui.navigation.AppShell
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.home.HomeContent
import com.twohearts.app.ui.screens.home.yukiPresenceFor
import com.twohearts.app.ui.screens.yuki.YukiArt
import com.twohearts.app.ui.screens.yuki.YukiHub
import com.twohearts.app.ui.screens.yuki.YukiPose
import com.twohearts.app.ui.screens.yuki.YukiRoom
import com.twohearts.app.ui.screens.yuki.YukiScreen
import com.twohearts.app.ui.screens.yuki.YukiViewModel
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationYukiRoomTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val out = File("build/transformation-19")
    private val now = Instant.now()

    @Test
    fun lightRoomProvidesBothPetActionsAndPersistsToyAccessoryAndMilestones() {
        val service = YukiService(app)
        val current = mutableStateOf(
            YukiState.createDefault(now = now).copy(
                level = 2, experience = 27, totalFeedings = 2, totalPlays = 1, totalPets = 4,
                ownedAccessories = listOf("none", "bow-tie"),
            ),
        )
        service.saveYukiState(current.value)
        assertNotNull(YukiArt.load(app, YukiPose.SIT))
        val actions = mutableListOf<YukiAction>()
        render("room-light", reduce = true) {
            YukiHub(
                current.value, null, working = false, failed = false, animate = true,
                onBack = {}, onAction = actions::add,
                onToy = { current.value = service.changeToy(it) },
                onAccessory = { current.value = service.changeAccessory(it) },
            )
        }.use { scene ->
            assertTrue(scene.activityText("Well fed"))
            assertTrue(scene.click("Pet"))
            assertTrue(scene.clickDescription("Yuki, an orange tabby"))
            assertEquals(listOf(YukiAction.PET, YukiAction.PET), actions)
            scene.scrollActivityBy(1_000f)
            assertTrue(scene.click("Toy basket and keepsakes"))
            scene.captureDialog("keepsakes-light")
            assertTrue(scene.clickDialog("Rolling ball"))
            assertTrue(scene.scrollDialogBy(1_000f))
            assertTrue(scene.clickDialog("Bow tie"))
            assertTrue(scene.dialogText("Meals together"))
            scene.captureDialog("milestones-light")
            assertTrue(scene.clickDialog("Done"))
            assertTrue(scene.activityText("Wearing bow tie"))
            scene.scrollActivityBy(-3_000f)
            scene.capture(scene.activityView, "room-bow-tie-light")
        }
        val restored = YukiService(app).loadYukiState(now)
        assertEquals(YukiToy.BALL, restored.selectedToy)
        assertEquals("bow-tie", restored.accessory)
        assertEquals(2, restored.totalFeedings)
        assertEquals(1, restored.totalPlays)
        assertEquals(4, restored.totalPets)
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun darkLargeRoomKeepsCareAndSheetControlsAccessibleOnAShortScreen() {
        val state = YukiState.createDefault(now = now)
        assertNotNull(YukiArt.load(app, YukiPose.SIT))
        val care = mutableListOf<YukiAction>()
        val choices = mutableListOf<YukiToy>()
        render("room-dark-large", dark = true, scale = 2f, reduce = true) {
            YukiHub(
                state, null, working = false, failed = false, animate = false,
                onBack = {}, onAction = care::add, onToy = choices::add, onAccessory = {},
            )
        }.use { scene ->
            scene.scrollActivityBy(380f)
            scene.capture(scene.activityView, "care-dark-large")
            for (label in listOf("Feed", "Play", "Pet", "Water", "Brush", "Nap")) assertTrue(scene.click(label))
            assertEquals(listOf(YukiAction.FEED, YukiAction.PLAY, YukiAction.PET, YukiAction.DRINK, YukiAction.CLEAN, YukiAction.SLEEP), care)
            scene.scrollActivityBy(3_000f)
            assertTrue(scene.click("Toy basket and keepsakes"))
            scene.captureDialog("keepsakes-dark-large")
            assertTrue(scene.dialogText("Done"))
            assertTrue(scene.scrollDialogBy(340f))
            assertTrue(scene.dialogText("Unlocks at level 2"))
            assertFalse(scene.clickDialog("Rolling ball"))
            assertTrue(scene.clickDialog("Feather wand"))
            assertEquals(listOf(YukiToy.FEATHER), choices)
            assertTrue(scene.clickDialog("Done"))
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h740dp-mdpi")
    fun homeUsesOnlyPersistedStatusAndItsPortraitOpensTheRoom() {
        val prefs = app.getSharedPreferences("twohearts_yuki", Context.MODE_PRIVATE)
        assertTrue(prefs.edit().remove("yuki_state").commit())
        assertNull(yukiPresenceFor(app, now))
        val started = YukiState.createDefault("Yuki", now).copy(energy = 22, level = 3)
        YukiService(app).saveYukiState(started)
        assertTrue(YukiService(app).perform(YukiAction.SLEEP, now).accepted)
        val presence = requireNotNull(yukiPresenceFor(app, now.plusSeconds(2)))
        assertEquals(YukiActivity.SLEEPING, presence.activity)
        assertEquals("Taking a nap", presence.statusLine)
        assertNotNull(YukiArt.load(app, YukiPose.SLEEP))

        val visit = mutableListOf<String>()
        val timestamp = now.toString()
        render("home-sleeping-before-scroll", RoutePath.APP_HOME, dark = true, scale = 2f, reduce = true) {
            AppShell(
                RoutePath.APP_HOME, {}, {}, canNavigateBack = false,
                keyboardInsets = WindowInsets(bottom = 0),
            ) {
                HomeContent(
                    owner = Profile("Alex", null, "owner", id = "owner", createdAt = timestamp, updatedAt = timestamp),
                    partner = Profile("Sam", null, "partner", id = "partner", createdAt = timestamp, updatedAt = timestamp),
                    relationship = CoupleRelationship("owner", "partner", "2022-01-15", "pair", timestamp, timestamp),
                    notes = emptyList(), reminders = emptyList(), memories = emptyList(), moods = emptyList(), dates = emptyList(),
                    onNavigate = visit::add, yukiPresence = presence,
                )
            }
        }.use { scene ->
            assertTrue(scene.scrollActivityBy(8_000f))
            scene.capture(scene.activityView, "home-sleeping-dark-large")
            assertTrue(scene.activityText("Taking a nap"))
            assertTrue(scene.activityText("Level 3"))
            assertTrue(scene.clickDescription("Yuki, Taking a nap"))
            assertEquals(listOf(RoutePath.APP_YUKI), visit)
        }
        assertTrue(YukiService(app).perform(YukiAction.WAKE, now.plusSeconds(3)).accepted)
        assertEquals(YukiActivity.IDLE, yukiPresenceFor(app, now.plusSeconds(3))?.activity)
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun activityForegroundRestoresTheNapAndWakeUpdatesTheHomeSnapshot() {
        val service = YukiService(app)
        service.saveYukiState(YukiState.createDefault(now = now))
        val viewModel = YukiViewModel(app)
        render("route-loading", reduce = true) { YukiScreen(viewModel, onBack = {}) }.use { scene ->
            waitFor(scene) { scene.activityText("Nap") && viewModel.presentation.value.state != null }
            assertTrue(scene.click("Nap"))
            waitFor(scene) { scene.activityText("Wake Yuki") && service.loadYukiState().activity == YukiActivity.SLEEPING }
            scene.controller.pause().stop()
            assertEquals(YukiActivity.SLEEPING, yukiPresenceFor(app)?.activity)
            scene.controller.restart().start().resume().visible()
            waitFor(scene) { viewModel.presentation.value.state?.activity == YukiActivity.SLEEPING && scene.activityText("Wake Yuki") }
            scene.capture(scene.activityView, "resumed-nap")
            assertTrue(scene.click("Wake Yuki"))
            waitFor(scene) { viewModel.presentation.value.state?.activity == YukiActivity.IDLE && yukiPresenceFor(app)?.activity == YukiActivity.IDLE }
            scene.capture(scene.activityView, "woke-from-nap")
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativeRoomRecordsIdleAndCareSequences() {
        val stage = mutableStateOf(YukiState.createDefault(now = Instant.parse("2026-11-01T00:00:00Z")).copy(
            accessory = "bandana", ownedAccessories = listOf("none", "bandana"),
        ))
        val sequences = listOf(
            "feed" to YukiActivity.EATING, "water" to YukiActivity.DRINKING, "play" to YukiActivity.PLAYING,
            "pet" to YukiActivity.BEING_PETTED, "groom" to YukiActivity.GROOMING, "sleep" to YukiActivity.SLEEPING,
            "wake" to YukiActivity.IDLE,
        )
        YukiPose.entries.forEach { YukiArt.load(app, it) }
        render("motion-start", dark = true) { Column { YukiRoom(stage.value, animate = true) } }.use { scene ->
            waitFor(scene) { scene.activityDescription("Yuki, an orange tabby") }
            record(scene, "idle", 51)
            for ((name, activity) in sequences) {
                val startedAt = if (activity == YukiActivity.IDLE) null else Instant.now().toString()
                stage.value = stage.value.copy(activity = activity, activityStartedAt = startedAt)
                record(scene, name, if (activity == YukiActivity.PLAYING) 25 else 20)
                if (activity.durationMs > 0) {
                    stage.value = stage.value.copy(activity = YukiActivity.IDLE, activityStartedAt = null)
                    repeat(3) { scene.layout(scene.activityView) }
                }
            }
        }
    }

    @Test
    fun repeatedDecodingKeepsTheSharedBitmapCacheBounded() {
        val cacheSizes = (0..3).map {
            YukiPose.entries.forEach { pose -> assertNotNull(YukiArt.load(app, pose)) }
            YukiPose.entries.mapNotNull(YukiArt::cached).sumOf { bitmap -> bitmap.allocationByteCount }
        }
        assertTrue(cacheSizes.all { it in 2 * 1024 * 1024..5 * 1024 * 1024 })
        assertEquals(1, cacheSizes.distinct().size)
    }

    private fun record(scene: NativeComposeScene, name: String, count: Int) {
        repeat(count) { frame ->
            scene.capture(scene.activityView, "sequence-$name-${frame.toString().padStart(2, '0')}")
            if (frame % 3 == 0) Thread.sleep(8)
        }
    }

    private fun waitFor(scene: NativeComposeScene, predicate: () -> Boolean) {
        repeat(100) {
            scene.layout(scene.activityView)
            if (predicate()) return
            Thread.sleep(12)
        }
        throw AssertionError("Room state did not appear. Visible text: ${scene.activityTexts()}")
    }

    private fun render(
        name: String, route: String = RoutePath.APP_YUKI, dark: Boolean = false, scale: Float = 1f,
        reduce: Boolean = false, content: @Composable () -> Unit,
    ) = NativeComposeScene(out).apply {
        setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = scale, reduceMotion = reduce, content = content)
            }
        }
        capture(activityView, name)
    }
}
