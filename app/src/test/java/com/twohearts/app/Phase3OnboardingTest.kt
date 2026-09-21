package com.twohearts.app

import com.twohearts.app.data.settings.OnboardingDraft
import com.twohearts.app.ui.onboarding.OnboardingData
import com.twohearts.app.ui.onboarding.OnboardingStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Phase 3 onboarding invariants.
 *
 * Phase 3 made first launch a resumable, credential-safe sequence rather than
 * a set of screens that only exist while the process does. The render harness
 * covers what the steps *look* like; these tests pin the rules underneath —
 * progression, resume, and the guarantee that a PIN never reaches disk. They
 * exist so a later change cannot silently reintroduce a flow that skips a step
 * after a crash or writes a lock code into unencrypted settings.
 *
 * Robolectric is required rather than optional: the draft serialises through
 * `org.json`, whose methods on the plain JVM classpath are android.jar stubs
 * that throw. Running the real implementation is the point of the test, so the
 * runner is what makes these assertions mean anything.
 */
@RunWith(RobolectricTestRunner::class)
class Phase3OnboardingTest {

    // ─── Progression ──────────────────────────────────────────────────

    @Test
    fun `stages run in one linear order from first launch to complete`() {
        val order = OnboardingStage.entries.map { it.order }
        assertEquals(order.sorted(), order)
        assertEquals(order.distinct(), order)
    }

    @Test
    fun `every stage but the last has a next and every stage but the first has a previous`() {
        OnboardingStage.entries.forEach { stage ->
            if (stage.isLast()) assertNull(stage.next()) else assertEquals(stage.order + 1, stage.next()?.order)
            if (stage.isFirst()) {
                assertNull(stage.previous())
            } else {
                assertEquals(stage.order - 1, stage.previous()?.order)
            }
        }
    }

    @Test
    fun `the progress indicator counts the setup steps, not the welcome or completion screens`() {
        // Welcome is the front door and completion is the exit; neither is a
        // numbered step. Deriving the denominator keeps a new stage from
        // silently desyncing the indicator.
        assertEquals(4, OnboardingStage.SETUP_STEP_COUNT)
        assertEquals(OnboardingStage.APP_LOCK.order, OnboardingStage.LAST_SETUP_ORDER)
        assertFalse(OnboardingStage.FRESH.order in 1..OnboardingStage.SETUP_STEP_COUNT)
        assertFalse(OnboardingStage.COMPLETE.order in 1..OnboardingStage.SETUP_STEP_COUNT)
    }

    // ─── Resume across restarts ───────────────────────────────────────

    @Test
    fun `every stage round-trips through its storage key`() {
        OnboardingStage.entries.forEach { stage ->
            assertEquals(stage, OnboardingStage.fromStorageKey(stage.storageKey))
        }
    }

    @Test
    fun `storage keys are the legacy strings so a mid-setup upgrade keeps its place`() {
        assertEquals("app-lock", OnboardingStage.APP_LOCK.storageKey)
        assertEquals(OnboardingStage.APP_LOCK, OnboardingStage.fromStorageKey("app-lock"))
    }

    @Test
    fun `an empty or unknown stored stage falls back to first launch`() {
        assertEquals(OnboardingStage.FRESH, OnboardingStage.fromStorageKey(null))
        assertEquals(OnboardingStage.FRESH, OnboardingStage.fromStorageKey(""))
        assertEquals(OnboardingStage.FRESH, OnboardingStage.fromStorageKey("nonsense"))
        assertEquals(OnboardingStage.FRESH, OnboardingStage.fromStorageKey("APP_LOCK"))
    }

    @Test
    fun `a saved draft resumes the answers recorded so far`() {
        val draft = OnboardingDraft(
            ownerName = "Ada",
            ownerBirthday = "1994-05-01",
            partnerName = "Grace",
            partnerBirthday = "1993-11-09",
            startDate = "2019-06-14",
            themeMode = "dark",
            textSize = "large",
        )

        val restored = OnboardingDraft.fromJson(draft.toJson())

        assertEquals(draft, restored)
    }

    @Test
    fun `a malformed or absent draft degrades to a fresh draft rather than throwing`() {
        assertNull(OnboardingDraft.fromJson(null))
        assertNull(OnboardingDraft.fromJson(""))
        assertNull(OnboardingDraft.fromJson("not json at all"))
        assertNull(OnboardingDraft.fromJson("[1,2,3]"))
    }

    @Test
    fun `missing draft fields fall back to their defaults`() {
        val restored = OnboardingDraft.fromJson("""{"ownerName":"Ada"}""")

        assertEquals("Ada", restored?.ownerName)
        assertNull(restored?.ownerBirthday)
        assertEquals("", restored?.partnerName)
        assertEquals("system", restored?.themeMode)
        assertEquals("default", restored?.textSize)
    }

    @Test
    fun `a JSON null birthday is read back as absent, not as the string null`() {
        val restored = OnboardingDraft.fromJson(
            """{"ownerName":"Ada","ownerBirthday":null,"partnerName":"Grace"}"""
        )

        assertNull(restored?.ownerBirthday)
        assertEquals("Grace", restored?.partnerName)
    }

    // ─── Credential safety ────────────────────────────────────────────

    @Test
    fun `the draft has no pin field, so a lock code cannot be persisted`() {
        // DataStore Preferences is not encrypted. Keeping the PIN out of the
        // draft entirely is what makes "the code only ever lives in secure
        // storage" a structural fact rather than a convention to remember.
        val draftFields = OnboardingDraft::class.java.declaredFields.map { it.name }
        assertFalse(draftFields.any { it.contains("pin", ignoreCase = true) })

        val serialised = OnboardingDraft(ownerName = "Ada", partnerName = "Grace").toJson()
        assertFalse(serialised.contains("pin", ignoreCase = true))
    }

    @Test
    fun `a pin is at least four digits and only counts once it is confirmed`() {
        val typed = OnboardingData(pin = "1234", confirmPin = "1234")
        assertTrue(typed.isPinValid())

        val mismatched = OnboardingData(pin = "1234", confirmPin = "1235")
        assertFalse(mismatched.isPinValid())

        val tooShort = OnboardingData(pin = "123", confirmPin = "123")
        assertFalse(tooShort.isPinValid())

        val tooLong = OnboardingData(pin = "123456789", confirmPin = "123456789")
        assertFalse(tooLong.isPinValid())
    }

    // ─── Step validity ────────────────────────────────────────────────

    @Test
    fun `a name is required for both people and is capped at fifty characters`() {
        assertTrue(OnboardingData(ownerName = "Ada").isOwnerValid())
        assertFalse(OnboardingData(ownerName = "").isOwnerValid())
        assertFalse(OnboardingData(ownerName = "   ").isOwnerValid())
        assertFalse(OnboardingData(ownerName = "x".repeat(51)).isOwnerValid())

        assertTrue(
            OnboardingData(partnerName = "Grace", startDate = "2019-06-14").isRelationshipValid()
        )
        assertFalse(
            OnboardingData(partnerName = "", startDate = "2019-06-14").isRelationshipValid()
        )
        assertFalse(
            OnboardingData(partnerName = "Grace", startDate = "").isRelationshipValid()
        )
    }
}
