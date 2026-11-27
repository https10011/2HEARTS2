package com.twohearts.app

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.game.YukiAction
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.data.game.YukiToy
import com.twohearts.app.data.game.applyTimeDecay
import com.twohearts.app.data.game.nextUpdateDelayMillis
import com.twohearts.app.services.game.YukiService
import com.twohearts.app.ui.screens.yuki.YukiArt
import com.twohearts.app.ui.screens.yuki.YukiMotion
import com.twohearts.app.ui.screens.yuki.YukiMotionController
import com.twohearts.app.ui.screens.yuki.YukiPose
import com.twohearts.app.ui.screens.yuki.YukiRoom
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w360dp-h740dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationYukiEngineTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val epoch = Instant.parse("2026-11-26T06:00:00Z")
    private val default = YukiState.createDefault(now = epoch)
    private val clips by lazy { YukiArt.loadClips(app) }

    @Test
    fun activeCareUsesAuthoredMotionAndEasesMovementBetweenRegisteredPoses() {
        assertEquals(YukiMotion.entries.toSet(), clips.keys)
        val actions = listOf(
            Triple(YukiActivity.EATING, YukiMotion.FEED, YukiPose.EAT),
            Triple(YukiActivity.DRINKING, YukiMotion.DRINK, YukiPose.DRINK),
            Triple(YukiActivity.PLAYING, YukiMotion.CHASE, YukiPose.JUMP),
            Triple(YukiActivity.BEING_PETTED, YukiMotion.PET, YukiPose.ROLL),
            Triple(YukiActivity.GROOMING, YukiMotion.GROOM, YukiPose.GROOM_ALT),
            Triple(YukiActivity.SLEEPING, YukiMotion.NAP, YukiPose.SLEEP),
        )
        for ((activity, motion, expectedPose) in actions) {
            if (activity.durationMs > 0) assertEquals(activity.durationMs, motion.duration)
            val engine = controller()
            engine.update(default, 0, 0, Long.MAX_VALUE)
            engine.sample(0)
            engine.update(default.copy(activity = activity, activityStartedAt = epoch.toString()), 0, 100, 0)
            val frames = (100L until 100L + motion.duration step 20).map(engine::sample)
            assertTrue(frames.all { it.focused == motion })
            assertTrue(frames.any { it.pose == expectedPose })
            assertTrue(frames.any { it.pose != it.nextPose && it.blend in .08f..0.92f })
            assertTrue(frames.zipWithNext().all { (a, b) -> abs(a.x - b.x) <= .03f && abs(a.lift - b.lift) <= .03f })
            if (activity == YukiActivity.PLAYING) {
                assertTrue(frames.any { it.facesRight && it.lift >= .08f })
                val airborneIndex = frames.indexOfFirst { it.pose == YukiPose.JUMP }
                val landingIndex = frames.indexOfFirst { it.pose == YukiPose.WALK_ALT }
                val reachIndex = frames.indexOfFirst { it.pose == YukiPose.PAW }
                assertTrue(airborneIndex < landingIndex && landingIndex < reachIndex)
                assertEquals(0f, frames[reachIndex].lift, .0001f)
            }
        }
    }

    @Test
    fun feedOverridesIdleAndRejectionsNeverInterruptCare() {
        val engine = controller(19)
        engine.update(default, 0, 0, Long.MAX_VALUE)
        val activeIdle = (0L..7_000L step 25).first { engine.sample(it).focused != null }
        val eating = default.copy(activity = YukiActivity.EATING, activityStartedAt = epoch.toString())
        engine.update(eating, 0, activeIdle + 1, 0)
        assertEquals(YukiMotion.FEED, engine.sample(activeIdle + 60).focused)
        engine.update(eating, 1, activeIdle + 80, 79)
        assertEquals(YukiMotion.FEED, engine.sample(activeIdle + 350).focused)
        engine.update(default, 1, activeIdle + 2_450, Long.MAX_VALUE)
        engine.sample(activeIdle + 2_450)
        val settled = engine.sample(activeIdle + 2_700)
        assertEquals(YukiPose.SIT, settled.pose)
        assertEquals(0f, settled.x, .0001f)
        engine.update(default, 2, activeIdle + 2_800, Long.MAX_VALUE)
        assertEquals(YukiMotion.INTERRUPTED, engine.sample(activeIdle + 3_050).focused)
    }

    @Test
    fun wakeAllowsCareAndResumingAnActionSamplesItsRemainingAnimation() {
        val nowAsleep = default.copy(activity = YukiActivity.SLEEPING, activityStartedAt = epoch.toString())
        val engine = controller()
        engine.update(nowAsleep, 0, 0, 12_000)
        for (t in 0L..30_000L step 100) {
            val frame = engine.sample(t)
            assertEquals(YukiPose.SLEEP, frame.pose)
            assertNull(frame.focused)
        }
        engine.update(default, 0, 31_000, Long.MAX_VALUE)
        assertEquals(YukiMotion.WAKE, engine.sample(31_400).focused)
        engine.update(default.copy(activity = YukiActivity.DRINKING, activityStartedAt = epoch.toString()), 0, 31_500, 0)
        assertEquals(YukiMotion.DRINK, engine.sample(31_700).focused)

        val resumed = controller()
        val eating = default.copy(activity = YukiActivity.EATING, activityStartedAt = epoch.toString())
        resumed.update(eating, 0, 800, 1_100)
        val frame = resumed.sample(800)
        assertEquals(YukiMotion.FEED, frame.focused)
        assertEquals(YukiPose.EAT, frame.pose)
        assertEquals(-.14f, frame.x, .03f)
    }

    @Test
    fun seededIdleVariesOverFifteenMinutesAndKeepsActivityBounds() {
        fun cues(seed: Long): List<YukiMotion> {
            val engine = controller(seed)
            engine.update(default, 0, 0, Long.MAX_VALUE)
            var previous: YukiMotion? = null
            val events = mutableListOf<YukiMotion>()
            for (time in 0L..900_000L step 33) {
                val frame = engine.sample(time)
                assertTrue(frame.x in -.15f..0.12f)
                assertTrue(frame.stretch in .987f..1.013f)
                assertEquals(0f, frame.lift, .0001f)
                if (previous != frame.focused) {
                    frame.focused?.let(events::add)
                    previous = frame.focused
                }
            }
            return events
        }
        val seeded = cues(2026)
        assertEquals(seeded, cues(2026))
        assertNotEquals(seeded, cues(2027))
        assertTrue(seeded.size in 70..200)
        assertTrue(seeded.containsAll(listOf(YukiMotion.BLINK, YukiMotion.EAR, YukiMotion.TAIL, YukiMotion.WALK)))
        assertTrue(seeded.zipWithNext().none { (a, b) -> a == b })
    }

    @Test
    fun tiredIdleCanDrowseAndAPlayfulCareActionTakesPriority() {
        val engine = controller()
        val tired = default.copy(energy = 20)
        engine.update(tired, 0, 0, Long.MAX_VALUE)
        val beganDozing = (0L..25_000L step 25).first { engine.sample(it).focused == YukiMotion.DROWSE }
        assertEquals(YukiPose.SLEEP, engine.sample(beganDozing + 1_000).pose)
        assertEquals(YukiActivity.IDLE, tired.activity)
        val petted = tired.copy(activity = YukiActivity.BEING_PETTED, activityStartedAt = epoch.toString())
        engine.update(petted, 0, beganDozing + 1_100, 0)
        assertEquals(YukiMotion.PET, engine.sample(beganDozing + 1_300).focused)
    }

    @Test
    fun differentServiceInstancesAcceptOneConcurrentCareActionAndKeepSavedProgress() {
        val service = YukiService(app)
        service.saveYukiState(default.copy(hunger = 51, level = 2, experience = 12, selectedToy = YukiToy.BALL))
        val workers = Executors.newFixedThreadPool(2)
        try {
            val ready = CountDownLatch(1)
            val futures = List(2) {
                workers.submit<YukiService.ActionResult> {
                    check(ready.await(5, TimeUnit.SECONDS))
                    YukiService(app).perform(YukiAction.FEED, epoch)
                }
            }
            ready.countDown()
            val results = futures.map { it.get(5, TimeUnit.SECONDS) }
            assertEquals(1, results.count { it.accepted })
            val reopened = YukiService(app).loadYukiState(epoch)
            assertEquals(1, reopened.totalFeedings)
            assertEquals(1, reopened.totalInteractions)
            assertEquals(81, reopened.hunger)
            assertEquals(27, reopened.experience)
            assertEquals(YukiToy.BALL, reopened.selectedToy)
            assertFalse(YukiService(app).perform(YukiAction.PET, epoch.plusMillis(700)).accepted)
            assertTrue(YukiService(app).perform(YukiAction.PET, epoch.plusSeconds(3)).accepted)
        } finally {
            workers.shutdownNow()
        }
    }

    @Test
    fun timeAdvanceIsConsistentAcrossRefreshesAndOnlyCreditsSleepingHours() {
        val starting = default.copy(hunger = 80, energy = 15, happiness = 77, cleanliness = 82)
        val later = applyTimeDecay(starting, epoch.plusSeconds(6 * 3600L))
        val hourly = (1..6).fold(starting) { state, hours -> applyTimeDecay(state, epoch.plusSeconds(hours * 3600L)) }
        assertEquals(later, hourly)
        val sleeping = starting.copy(
            activity = YukiActivity.SLEEPING, activityStartedAt = epoch.toString(), updatedAt = epoch.plusSeconds(7L * 3600).toString(),
        )
        val wake = applyTimeDecay(sleeping, epoch.plusSeconds(11L * 3600))
        assertEquals(YukiActivity.IDLE, wake.activity)
        assertEquals(33, wake.energy)
        assertEquals(68, wake.hunger)
        assertEquals(1_200L, starting.copy(activity = YukiActivity.EATING, activityStartedAt = epoch.toString())
            .nextUpdateDelayMillis(epoch.plusMillis(1_200)))
        assertEquals(80, applyTimeDecay(starting, epoch.minusSeconds(4)).hunger)
        assertEquals(24, applyTimeDecay(starting, epoch.plusSeconds(365L * 86_400)).hunger)
    }

    @Test
    fun unreadableSaveIsKeptUntilAnExplicitCareActionAndOldSavesReopen() {
        val prefs = app.getSharedPreferences("twohearts_yuki", Context.MODE_PRIVATE)
        val saved = """{"name":"Maple","level":3,"experience":31,"totalPets":11,"updatedAt":"bad","future":"ignored"}"""
        assertTrue(prefs.edit().putString("yuki_state", saved).commit())
        val restored = YukiService(app).refresh(epoch)
        assertEquals("Maple", restored.name)
        assertEquals(11, restored.totalPets)
        assertEquals(31, restored.experience)
        assertEquals(restored, YukiService(app).loadYukiState(epoch))
        val damaged = "{offline incomplete JSON"
        assertTrue(prefs.edit().putString("yuki_state", damaged).commit())
        assertEquals("Yuki", YukiService(app).refresh(epoch).name)
        assertEquals(damaged, prefs.getString("yuki_state", null))
        assertTrue(YukiService(app).perform(YukiAction.PET, epoch).accepted)
        assertEquals(1, YukiService(app).loadYukiState(epoch).totalPets)
    }

    @Test
    fun nativeCanvasRendersCareAndStaysStillWhenInactiveOrReduced() {
        val captures = File("build/transformation-18")
        val state = mutableStateOf(default)
        val active = mutableStateOf(true)
        val reduced = mutableStateOf(false)
        YukiPose.entries.forEach { YukiArt.load(app, it) }
        NativeComposeScene(captures).use { scene ->
            scene.setContent {
                TwoHeartsTheme(darkTheme = true, reduceMotion = reduced.value) {
                    Column { YukiRoom(state.value, active.value) }
                }
            }
            repeat(8) { scene.layout(scene.activityView); Thread.sleep(12) }
            assertTrue(scene.activityDescription("Yuki, an orange tabby"))
            scene.capture(scene.activityView, "before-care")
            state.value = default.copy(activity = YukiActivity.EATING, activityStartedAt = Instant.now().toString())
            repeat(10) { index -> scene.capture(scene.activityView, "meal-${index + 1}"); Thread.sleep(14) }
            assertFalse(File(captures, "before-care.png").readBytes().contentEquals(File(captures, "meal-6.png").readBytes()))
            assertFalse(File(captures, "meal-2.png").readBytes().contentEquals(File(captures, "meal-8.png").readBytes()))

            active.value = false
            repeat(3) { scene.layout(scene.activityView); Thread.sleep(12) }
            scene.capture(scene.activityView, "paused")
            repeat(10) { scene.layout(scene.activityView) }
            scene.capture(scene.activityView, "paused-later")
            assertTrue(File(captures, "paused.png").readBytes().contentEquals(File(captures, "paused-later.png").readBytes()))

            active.value = true
            reduced.value = true
            repeat(3) { scene.layout(scene.activityView); Thread.sleep(12) }
            scene.capture(scene.activityView, "reduced-motion")
            repeat(10) { scene.layout(scene.activityView) }
            scene.capture(scene.activityView, "reduced-motion-later")
            assertTrue(File(captures, "reduced-motion.png").readBytes().contentEquals(File(captures, "reduced-motion-later.png").readBytes()))
        }
    }

    private fun controller(seed: Long = 26L) = YukiMotionController(clips, seed)
}
