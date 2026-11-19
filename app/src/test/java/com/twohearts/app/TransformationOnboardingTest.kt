package com.twohearts.app

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.data.settings.OnboardingDraft
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.media.AvatarCrop
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.ProfilePhotoStaging
import com.twohearts.app.services.media.ProfilePhotoWriter
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.ui.components.NotificationPermissionAction
import com.twohearts.app.ui.components.NotificationPermissionControl
import com.twohearts.app.ui.components.notificationPermissionAction
import com.twohearts.app.ui.onboarding.OnboardingDomainWriter
import com.twohearts.app.ui.onboarding.CapabilitiesSetupScreen
import com.twohearts.app.ui.onboarding.OnboardingStage
import com.twohearts.app.ui.onboarding.resumeOnboardingStage
import com.twohearts.app.ui.onboarding.shouldShowApp
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationOnboardingTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val staging = ProfilePhotoStaging(app)
    private val media = MediaStorage(app)
    private val answers = OnboardingDraft(
        ownerName = "Alex",
        partnerName = "Avery",
        startDate = "2020-06-15",
        themeMode = "dark",
        textSize = "large",
    )

    @Test
    fun capabilitiesKeepLegacyCheckpointsAndRespectAndroidNotificationPolicy() {
        assertEquals(OnboardingStage.CAPABILITIES, OnboardingStage.PERSONALIZATION.next())
        assertEquals(OnboardingStage.APP_LOCK, OnboardingStage.CAPABILITIES.next())
        assertEquals(OnboardingStage.CAPABILITIES, OnboardingStage.APP_LOCK.previous())
        assertEquals(OnboardingStage.APP_LOCK, OnboardingStage.fromStorageKey("app-lock"))
        assertEquals(OnboardingStage.COMPLETE, OnboardingStage.fromStorageKey("complete"))
        assertTrue(shouldShowApp(true, "complete", true))
        assertFalse(shouldShowApp(false, "capabilities", true))
        assertEquals(OnboardingStage.CAPABILITIES, resumeOnboardingStage(OnboardingStage.CAPABILITIES, answers))
        assertEquals(OnboardingStage.OWNER, resumeOnboardingStage(OnboardingStage.APP_LOCK, OnboardingDraft()))
        assertEquals(
            OnboardingStage.RELATIONSHIP,
            resumeOnboardingStage(OnboardingStage.APP_LOCK, answers.copy(startDate = "2999-01-01")),
        )

        assertEquals(NotificationPermissionAction.ALLOWED, notificationPermissionAction(34, true, false))
        assertEquals(NotificationPermissionAction.REQUEST, notificationPermissionAction(33, false, false))
        assertEquals(NotificationPermissionAction.SETTINGS, notificationPermissionAction(34, false, true))
        assertEquals(NotificationPermissionAction.SETTINGS, notificationPermissionAction(32, false, false))
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-mdpi")
    fun continueAndBackDoNotRequestPermissionAndDeniedStateProvidesSettings() {
        var requests = 0
        var settings = 0
        var next = 0
        var back = 0
        Robolectric.buildActivity(ComponentActivity::class.java).use { controller ->
            val activity = controller.setup().get()
            activity.setContent {
                TwoHeartsTheme(darkTheme = true, reduceMotion = true) {
                    CapabilitiesSetupScreen(
                        NotificationPermissionControl(NotificationPermissionAction.SETTINGS, { requests++ }, { settings++ }),
                        onBack = { back++ },
                        onNext = { next++ },
                    )
                }
            }
            shadowOf(Looper.getMainLooper()).idle()
            val window = activity.window.decorView
            val metrics = activity.resources.displayMetrics
            window.measure(
                View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.EXACTLY),
            )
            window.layout(0, 0, metrics.widthPixels, metrics.heightPixels)
            val nodes = checkNotNull(composeRoot(window)).semanticsOwner.getAllSemanticsNodes(mergingEnabled = true)
            fun clickText(text: String) {
                val node = nodes.single { it.config.getOrNull(SemanticsProperties.Text)?.any { label -> label.text == text } == true }
                assertTrue(checkNotNull(node.config.getOrNull(SemanticsActions.OnClick)?.action).invoke())
            }
            val backNode = nodes.single { node ->
                node.config.getOrNull(SemanticsProperties.ContentDescription)?.any { it.contains("back", ignoreCase = true) } == true
            }
            assertTrue(checkNotNull(backNode.config.getOrNull(SemanticsActions.OnClick)?.action).invoke())
            clickText("Android settings")
            clickText("Continue without alerts")
            assertEquals(0, requests)
            assertEquals(1, settings)
            assertEquals(1, next)
            assertEquals(1, back)
        }
    }

    @Test
    fun savedPhotoDraftAndCapabilitiesResumeAndCompletionClearsAtomically() = runBlocking {
        val storage = SettingsStorage.getInstance(app)
        val earlier = OnboardingDraft.fromJson("""{"ownerName":"Alex","startDate":"2020-06-15"}""")
        assertNull(earlier?.ownerPhotoDraft)
        assertFalse(checkNotNull(earlier).notificationPrompted)
        val pending = answers.copy(ownerPhotoDraft = "draft-a", partnerPhotoDraft = "draft-b", notificationPrompted = true)
        val session = AppStateService(storage)
        try {
            session.saveOnboardingStep(pending, OnboardingStage.CAPABILITIES.storageKey)
            val resumed = AppStateService(storage).also { it.initialize() }
            assertEquals(pending, storage.loadOnboardingDraft())
            assertEquals("capabilities", resumed.onboardingStage.value)
            assertFalse(resumed.isOnboarded.value)

            resumed.finishOnboarding(pending)
            val nextLaunch = AppStateService(storage).also { it.initialize() }
            assertTrue(shouldShowApp(nextLaunch.isOnboarded.value, nextLaunch.onboardingStage.value, true))
            assertEquals("dark", nextLaunch.themeMode.value)
            assertEquals("large", nextLaunch.textSize.value)
            assertNull(storage.loadOnboardingDraft())
        } finally {
            storage.reset()
        }
    }

    @Test
    fun retriesKeepProfileAndRelationshipIdentityAfterBothPhotosAreCommitted() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
        try {
            val service = relationships(db)
            val writer = OnboardingDomainWriter(ProfilePhotoWriter(db, media, staging), staging, service)
            val pending = answers.copy(ownerPhotoDraft = crop(Color.RED), partnerPhotoDraft = crop(Color.BLUE))
            writer.save(pending)
            val owner = checkNotNull(service.getOwner())
            val partner = checkNotNull(service.getPartner())
            val relationship = checkNotNull(service.getRelationship())
            assertNotNull(media.resolveUrl(checkNotNull(owner.photoRef)))
            assertNotNull(media.resolveUrl(checkNotNull(partner.photoRef)))
            assertTrue(owner.photoRef != partner.photoRef)

            writer.save(pending)
            assertEquals(owner.id, service.getOwner()?.id)
            assertEquals(owner.photoRef, service.getOwner()?.photoRef)
            assertEquals(partner.id, service.getPartner()?.id)
            assertEquals(partner.photoRef, service.getPartner()?.photoRef)
            assertEquals(relationship.id, service.getRelationship()?.id)
            assertEquals(2, db.profileDao().count())
            assertEquals(1, db.coupleRelationshipDao().count())
            assertEquals(2, db.mediaAssetDao().count())
        } finally {
            db.close()
        }
    }

    @Test
    fun aFailedSecondPhotoRollsBackAndSetupCanContinueWithoutIt() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
        try {
            val service = relationships(db)
            val writer = OnboardingDomainWriter(ProfilePhotoWriter(db, media, staging), staging, service)
            val ownerDraft = crop(Color.RED)
            val badPartnerDraft = crop(Color.BLUE)
            File(checkNotNull(staging.croppedUri(badPartnerDraft)?.path)).writeText("unreadable image")
            val pending = answers.copy(ownerPhotoDraft = ownerDraft, partnerPhotoDraft = badPartnerDraft)
            assertTrue(runCatching { writer.save(pending) }.exceptionOrNull() is IOException)
            assertEquals(0, db.profileDao().count())
            assertEquals(0, db.coupleRelationshipDao().count())
            assertEquals(0, db.mediaAssetDao().count())
            assertTrue(media.getAllMediaIds().isEmpty())
            assertNotNull(staging.croppedUri(ownerDraft))

            staging.discard(badPartnerDraft)
            writer.save(pending)
            assertNotNull(service.getOwner()?.photoRef)
            assertNull(service.getPartner()?.photoRef)
            assertNotNull(service.getRelationship())
            assertEquals(1, db.mediaAssetDao().count())
        } finally {
            db.close()
        }
    }

    private fun relationships(db: TwoHeartsDatabase) = RelationshipService(
        ProfileRepository(db.profileDao()), CoupleRelationshipRepository(db.coupleRelationshipDao()),
    )

    private fun composeRoot(view: View): ViewRootForTest? {
        if (view is ViewRootForTest) return view
        if (view is ViewGroup) {
            repeat(view.childCount) { index -> composeRoot(view.getChildAt(index))?.let { return it } }
        }
        return null
    }

    private suspend fun crop(color: Int): String {
        val image = Bitmap.createBitmap(80, 60, Bitmap.Config.ARGB_8888)
        try {
            image.eraseColor(color)
            return staging.crop(image, AvatarCrop())
        } finally {
            image.recycle()
        }
    }
}
