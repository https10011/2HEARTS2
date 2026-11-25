package com.twohearts.app

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.game.*
import com.twohearts.app.services.game.YukiService
import com.twohearts.app.ui.screens.yuki.YukiArt
import com.twohearts.app.ui.screens.yuki.YukiPose
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Phase13YukiTest {
    private val now = Instant.parse("2026-11-18T12:00:00Z")
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test fun returningAfterWeeksKeepsNeedsInRangeAndRecoversEnergy() {
        val before = YukiState.createDefault(now = now).copy(energy = 20)
        val after = applyTimeDecay(before, now.plusSeconds(90 * 86_400L))
        assertEquals(24, after.hunger)
        assertEquals(100, after.energy)
        assertTrue(after.happiness >= 45)
        assertEquals(before.hunger, applyTimeDecay(before, now.minusSeconds(60)).hunger)
    }

    @Test fun sleepingAndWakingPersistAcrossServiceInstances() {
        val service = YukiService(app)
        val start = YukiState.createDefault(now = now).copy(energy = 20)
        val nap = service.processAction(start, YukiAction.SLEEP, now)
        assertEquals(YukiActivity.SLEEPING, nap.state.activity)
        val later = now.plusSeconds(3_600)
        val restored = YukiService(app).loadYukiState(later)
        val advanced = service.applyDecay(restored, later)
        assertEquals(YukiActivity.SLEEPING, advanced.activity)
        assertTrue(advanced.energy > nap.state.energy)
        val wake = service.processAction(advanced, YukiAction.WAKE, later)
        assertEquals(YukiActivity.IDLE, wake.state.activity)
        assertEquals(wake.state, YukiService(app).loadYukiState(later))
    }

    @Test fun careRespectsLimitsAndUnlocksPlayableInventory() {
        val service = YukiService(app)
        val initial = YukiState.createDefault(now = now).copy(experience = 92, totalInteractions = 5)
        assertEquals(initial, service.equipAccessory(initial, "crown"))
        assertEquals(initial, service.selectToy(initial, YukiToy.BALL))
        val fed = service.processAction(initial, YukiAction.FEED, now).state
        assertEquals(100, fed.hunger)
        assertEquals(2, fed.level)
        assertTrue("bow-tie" in fed.ownedAccessories)
        assertEquals(1, fed.totalFeedings)
        val full = service.processAction(fed, YukiAction.FEED, now.plusSeconds(4)).state
        assertEquals(fed.totalFeedings, full.totalFeedings)

        val ball = service.selectToy(full, YukiToy.BALL)
        assertEquals(YukiToy.BALL, YukiService(app).loadYukiState(now).selectedToy)
        val play = service.processAction(ball, YukiAction.PLAY, now.plusSeconds(4)).state
        assertEquals(ball.energy - YukiToy.BALL.energyCost, play.energy)
        assertEquals(YukiActivity.PLAYING, play.activity)
        assertEquals(YukiActivity.IDLE, applyTimeDecay(play, now.plusSeconds(9)).activity)
    }

    @Test fun legacySaveRetainsProgressAndIgnoresUnknownFieldsAndItems() {
        app.getSharedPreferences("twohearts_yuki", Context.MODE_PRIVATE).edit()
            .putString("yuki_state", """{"name":"Yuki","level":3,"experience":32,"totalPets":7,"accessory":"bandana","ownedAccessories":["bandana","unknown"],"activity":"OLD","updatedAt":"bad"}""")
            .commit()
        val state = YukiService(app).loadYukiState(now)
        assertEquals(3, state.level)
        assertEquals(7, state.totalPets)
        assertEquals(32, state.experience)
        assertEquals("bandana", state.accessory)
        assertEquals(setOf("none", "bandana"), state.ownedAccessories.toSet())
        assertEquals(YukiActivity.IDLE, state.activity)
        assertEquals(now.toString(), applyTimeDecay(state, now).updatedAt)
        app.getSharedPreferences("twohearts_yuki", Context.MODE_PRIVATE).edit()
            .putString("yuki_state", "{broken").commit()
        assertEquals("Yuki", YukiService(app).loadYukiState(now).name)
    }

    @Test fun allSuppliedPosesLoadAndCheckerRemovalKeepsWhiteFur() {
        YukiPose.entries.forEach { pose ->
            val art = requireNotNull(YukiArt.load(app, pose)) { "No packaged artwork for ${pose.name}" }
            assertTrue(art.width <= 512 && art.height <= 512)
            assertEquals(Color.TRANSPARENT, art.getPixel(0, 0))
            assertEquals(Color.TRANSPARENT, art.getPixel(art.width - 1, art.height - 1))
        }
        val sitting = requireNotNull(YukiArt.load(app, YukiPose.SIT))
        assertEquals(Color.rgb(252, 240, 225), sitting.getPixel(180, 190))
        assertEquals(Color.rgb(252, 240, 225), sitting.getPixel(193, 169))
    }
}
