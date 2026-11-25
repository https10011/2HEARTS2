package com.twohearts.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.DatabaseInitializer
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.data.repository.*
import com.twohearts.app.data.settings.PinMaterialStore
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.lifecycle.LifecycleService
import com.twohearts.app.services.media.FileService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.services.media.VaultStorage
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.security.AppLockState
import com.twohearts.app.ui.navigation.AppRouter
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.security.AppLockGate
import com.twohearts.app.ui.screens.security.SecuritySettingsScreen
import com.twohearts.app.ui.screens.vault.AddVaultContent
import com.twohearts.app.ui.screens.vault.VaultContentType
import com.twohearts.app.ui.screens.vault.VaultContentViewer
import com.twohearts.app.ui.screens.vault.VaultHome
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationPrivacyUiTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val stamp = "2026-11-25T00:00:00Z"
    private val privateNote = VaultItem("A letter kept for ourselves", "note", "The small things we wrote down.\n\nNothing here leaves this journal.",
        id = "private-note", createdAt = stamp, updatedAt = stamp)
    private val document = VaultItem("Scanned letter", "file", mediaRef = "document-asset", id = "private-file", createdAt = stamp, updatedAt = stamp)

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun appRouterRequiresThePinBeforeHomeAndRelocksAfterTheBackgroundTimeout() {
        val db = Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
        val settings = SettingsStorage.getInstance(app)
        val before = runBlocking { settings.settings.first() }
        val store = InMemoryPinMaterialStore()
        var time = 2_000L
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        val lifecycle = LifecycleService(owner.lifecycle) { time }
        val relationships = RelationshipService(ProfileRepository(db.profileDao()), CoupleRelationshipRepository(db.coupleRelationshipDao()))
        val appState = AppStateService(settings)
        try {
            val setup = AppLockService({ store }, settings, LifecycleService(owner.lifecycle))
            runBlocking {
                settings.setAppLockEnabled(false)
                settings.setOnboarded(true)
                settings.updateOnboardingStage("complete")
                setup.createPin("4973")
                val first = relationships.createOwner("Alex Rivera", null)
                val second = relationships.createPartner("Sam Rivera", null)
                relationships.createRelationship(first.id, second.id, "2022-05-04")
                appState.initialize()
            }
            var opens = 0
            val lock = AppLockService({ opens++; store }, settings, lifecycle)
            runBlocking { lock.initialize() }
            assertEquals(AppLockState.LOCKED, lock.lockState.value)
            assertEquals(0, opens)
            lifecycle.start()
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            val content = routerContent(db, settings, relationships, appState, lock)
            render("locked-router-dark-large", RoutePath.APP_HOME, dark = true, scale = 2f, content = content).use { scene ->
                assertTrue(scene.activityText("TwoHearts is locked"))
                assertFalse(scene.activityDescription("Home"))
                assertFalse(scene.click("Unlock"))
                assertTrue(scene.setText("PIN", "5555"))
                assertTrue(scene.click("Unlock"))
                await(scene) { scene.activityError("That PIN didn’t match.") }
                scene.capture(scene.activityView, "lock-rejected-dark-large")
                assertFalse(scene.activityDescription("Home"))
                assertTrue(scene.setText("PIN", "4973"))
                assertTrue(scene.click("Unlock"))
                await(scene) { scene.activityDescription("Home") }
                assertEquals(AppLockState.UNLOCKED, lock.lockState.value)
                assertEquals(1, opens)
                owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                time += before.lockTimeoutSeconds * 1_000L + 1L
                owner.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
                await(scene) { scene.activityText("TwoHearts is locked") }
                assertFalse(scene.activityDescription("Home"))
            }
        } finally {
            lifecycle.stop()
            runBlocking {
                settings.setAppLockEnabled(before.appLockEnabled)
                settings.setOnboarded(before.onboarded)
                settings.updateOnboardingStage(before.onboardingStage)
            }
            db.close()
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun nativePinDialogsConfirmCreationChangeAndDisableWithVolatileFields() {
        val enabled = mutableStateOf(false)
        val store = InMemoryPinMaterialStore()
        val settings = SettingsStorage.getInstance(app)
        val before = runBlocking { settings.settings.first() }
        val lock = AppLockService({ store }, settings, LifecycleService())
        try {
            render("security-dark-large", RoutePath.APP_SETTINGS_SECURITY, dark = true, scale = 2f) {
                SecuritySettingsScreen(
                    enabled.value,
                    onCreatePin = { lock.createPin(it); enabled.value = true },
                    onChangePin = lock::changePin,
                    onDisableLock = { pin ->
                        lock.verifyPin(pin).also { correct ->
                            if (correct) { lock.disable(); enabled.value = false }
                        }
                    },
                    onBack = {},
                )
            }.use { scene ->
                assertTrue(scene.click("App lock"))
                scene.captureDialog("pin-create-dark-large")
                assertTrue(scene.setDialogText("New PIN", "1234"))
                assertTrue(scene.setDialogText("Confirm new PIN", "9876"))
                assertTrue(scene.clickDialog("Enable lock"))
                assertTrue(scene.dialogText("The new PINs don’t match."))
                assertFalse(store.hasPin())
                assertTrue(scene.setDialogText("Confirm new PIN", "1234"))
                assertTrue(scene.clickDialog("Enable lock"))
                await(scene) { scene.activityText("Change PIN") }
                assertTrue(lock.verifyPin("1234"))

                assertTrue(scene.click("Change PIN"))
                scene.captureDialog("pin-change-dark-large")
                assertTrue(scene.clickDialog("Cancel"))
                assertTrue(lock.verifyPin("1234"))
                assertTrue(scene.click("Change PIN"))
                assertTrue(scene.setDialogText("Current PIN", "1234"))
                assertTrue(scene.setDialogText("New PIN", "8531"))
                assertTrue(scene.setDialogText("Confirm new PIN", "8531"))
                assertTrue(scene.clickDialog("Save PIN"))
                await(scene) { !lock.verifyPin("1234") }
                assertTrue(lock.verifyPin("8531"))
                assertTrue(scene.click("App lock"))
                scene.captureDialog("pin-disable-dark-large")
                assertTrue(scene.setDialogText("Current PIN", "8531"))
                assertTrue(scene.clickDialog("Turn off"))
                await(scene) { !enabled.value }
                assertFalse(store.hasPin())
            }
        } finally {
            runBlocking { settings.setAppLockEnabled(before.appLockEnabled) }
        }
    }

    @Test
    fun nativeVaultListsAllTypesAndCanSaveAPrivateNoteOrPickADocument() {
        val opened = mutableListOf<String>()
        val fixtures = listOf(privateNote, document, document.copy(id = "photo", title = "Private photo", contentType = "photo"))
        render("vault-light", RoutePath.APP_VAULT) {
            VaultHome(fixtures, { opened += it.id }, { opened += "add" }, onBack = {})
        }.use { scene ->
            listOf("Notes", "Files", "Photos").forEach { assertTrue(scene.activityText(it)) }
            assertFalse(scene.activityText(privateNote.contentText!!))
            assertTrue(scene.click(document.title!!))
            assertTrue(scene.click("Add to Vault"))
            assertEquals(listOf(document.id, "add"), opened)
        }
        val saved = mutableListOf<Triple<String, VaultContentType, String?>>()
        render("vault-add-light", RoutePath.APP_VAULT_ADD) {
            AddVaultContent({}, { title, type, payload -> saved += Triple(title, type, payload) })
        }.use { scene ->
            assertTrue(scene.setText("Title", " Private thought "))
            assertTrue(scene.setText("Private note", " For our quiet morning "))
            assertTrue(scene.click("Save to Vault"))
            assertEquals(Triple("Private thought", VaultContentType.NOTE, "For our quiet morning"), saved.single())
            assertTrue(scene.click("File"))
            assertTrue(scene.click("Choose file"))
            deliverPickerResult(scene, Intent.ACTION_OPEN_DOCUMENT, null)
            assertFalse(scene.click("Save to Vault"))
            val uri = Uri.parse("content://twohearts.vault.fixture/document")
            assertTrue(scene.click("Choose file"))
            deliverPickerResult(scene, Intent.ACTION_OPEN_DOCUMENT, uri)
            await(scene) { scene.activityText("File selected") }
            scene.capture(scene.activityView, "vault-file-selected-light")
            assertTrue(scene.click("Save to Vault"))
            assertEquals(Triple("Private thought", VaultContentType.FILE, uri.toString()), saved.last())
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativeVaultNoteAndDocumentConfirmDeleteAndExportAtLargeType() {
        var deleted: VaultItem? = null
        render("vault-note-dark-large", RoutePath.APP_VAULT_CONTENT, dark = true, scale = 2f) {
            VaultContentViewer(privateNote, {}, { deleted = it })
        }.use { scene ->
            assertTrue(scene.activityText(privateNote.contentText!!))
            assertTrue(scene.click("Delete from Vault"))
            scene.captureDialog("vault-delete-dark-large")
            assertTrue(scene.clickDialog("Cancel"))
            assertEquals(null, deleted)
            assertTrue(scene.click("Delete from Vault"))
            assertTrue(scene.clickDialog("Delete"))
            assertEquals(privateNote, deleted)
        }
        var exported: Uri? = null
        val asset = MediaAsset("/test/selected-document.bin", "application/pdf", 412,
            id = "document-asset", createdAt = stamp, updatedAt = stamp)
        render("vault-document-dark-large", RoutePath.APP_VAULT_CONTENT, dark = true, scale = 2f) {
            VaultContentViewer(document, {}, {}, asset = asset, onExport = { exported = it })
        }.use { scene ->
            assertTrue(scene.click("Save a copy"))
            scene.captureDialog("vault-export-confirm-dark-large")
            assertTrue(scene.clickDialog("Choose location"))
            val uri = Uri.parse("content://twohearts.vault.fixture/export")
            deliverPickerResult(scene, Intent.ACTION_CREATE_DOCUMENT, uri)
            await(scene) { scene.activityText("Copy saved.") }
            assertEquals(uri, exported)
        }
    }

    @Test
    fun nativeVaultPhotoVideoAndMissingMediaUseAccessibleViewerStates() {
        val photo = placeTestPhoto(app)
        val video = File.createTempFile("vault-video", ".mp4", app.cacheDir)
        checkNotNull(javaClass.getResourceAsStream("/memories/local-preview.mp4")).use { input -> video.outputStream().use { input.copyTo(it) } }
        try {
            val photoAsset = MediaAsset(photo.absolutePath, "image/jpeg", photo.length(), id = "photo", createdAt = stamp, updatedAt = stamp)
            runBlocking { checkNotNull(MediaThumbnails.load(photoAsset.filePath, photoAsset.updatedAt, 512, normalizeExif = true)) }
            render("vault-photo-light", RoutePath.APP_VAULT_CONTENT) {
                VaultContentViewer(document.copy(contentType = "photo", title = "Private photo"), {}, {}, asset = photoAsset)
            }.use { scene ->
                assertTrue(scene.clickDescription("Vault photo"))
                scene.captureDialog("vault-photo-preview-light")
                assertTrue(scene.clickDialog("Close"))
            }
            val videoAsset = photoAsset.copy(filePath = video.absolutePath, mimeType = "video/mp4", fileSize = video.length())
            render("vault-video-light", RoutePath.APP_VAULT_CONTENT) {
                VaultContentViewer(document.copy(contentType = "video", title = "Private clip"), {}, {}, asset = videoAsset)
            }.use { scene ->
                assertTrue(scene.click("Play video"))
                scene.captureDialog("vault-video-player-light")
                assertTrue(scene.clickDialog("Close"))
            }
            render("vault-missing-light", RoutePath.APP_VAULT_CONTENT) {
                VaultContentViewer(document.copy(contentType = "file"), {}, {})
            }.use { scene -> assertTrue(scene.activityText("This attachment is unavailable.")) }
        } finally {
            photo.delete()
            video.delete()
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun shortViewportKeepsThePrivateActionsReachableAndShowsEmptyAndErrorStates() {
        render("lock-light-short", RoutePath.APP_HOME) { AppLockGate({ true }) }.use { scene ->
            assertTrue(scene.activityText("TwoHearts is locked"))
            assertTrue(scene.setText("PIN", "4973"))
            assertTrue(scene.click("Unlock"))
        }
        render("vault-empty-dark-large", RoutePath.APP_VAULT, dark = true, scale = 2f) {
            VaultHome(emptyList(), {}, {}, onBack = {})
        }.use { scene ->
            assertTrue(scene.activityText("Your vault is empty"))
            assertTrue(scene.click("Add to Vault"))
        }
        render("vault-editor-error-dark-large", RoutePath.APP_VAULT_ADD, dark = true, scale = 2f) {
            AddVaultContent({}, { _, _, _ -> }, errorMessage = "Couldn’t save this vault item.")
        }.use { scene ->
            assertTrue(scene.activityText("Couldn’t save this vault item."))
            assertTrue(scene.activityText("Save to Vault"))
            assertFalse(scene.click("Save to Vault"))
        }
        val longNote = privateNote.copy(contentText = List(7) { "A quiet memory worth keeping to ourselves." }.joinToString("\n\n"))
        render("vault-long-note-light-large", RoutePath.APP_VAULT_CONTENT, scale = 2f) {
            VaultContentViewer(longNote, {}, {})
        }.use { scene ->
            assertTrue(scene.scrollActivityBy(5_000f))
            scene.capture(scene.activityView, "vault-long-note-bottom-light-large")
            assertTrue(scene.click("Delete from Vault"))
            assertTrue(scene.clickDialog("Cancel"))
        }
    }

    private fun routerContent(
        db: TwoHeartsDatabase, settings: SettingsStorage, relationships: RelationshipService, state: AppStateService, lock: AppLockService,
    ): @Composable () -> Unit {
        val notes = NoteRepository(db.noteDao())
        val memories = MemoryRepository(db.memoryDao(), db.memoryMediaDao())
        val timeline = TimelineEventRepository(db.timelineEventDao())
        val reminders = ReminderRepository(db.reminderDao())
        val places = PlaceRepository(db.placeDao())
        val moods = MoodEntryRepository(db.moodEntryDao())
        val periods = PeriodEntryRepository(db.periodEntryDao())
        val periodSettings = PeriodSettingsRepository(db.periodSettingsDao())
        val dates = ImportantDateRepository(db.importantDateDao())
        val alerts = NotificationCenterRepository(db.notificationCenterDao())
        val media = MediaStorage(app)
        val data = DataManagementService(app, DatabaseInitializer(app), media, FileService(app))
        val search = SearchEngine()
        val items = VaultItemRepository(db.vaultItemDao())
        val vault = VaultStorage(app, db, media, items)
        return {
            AppRouter(
                state, relationships, lock, notes, memories, timeline, reminders, places, moods, periods, periodSettings,
                dates, data, search, alerts, settings, db.mediaAssetDao(), media, items, vault,
            )
        }
    }

    private fun deliverPickerResult(scene: NativeComposeScene, action: String, uri: Uri?) {
        val activity = scene.controller.get()
        val request = checkNotNull(shadowOf(activity).nextStartedActivityForResult)
        assertEquals(action, request.intent.action)
        assertTrue(activity.activityResultRegistry.dispatchResult(
            request.requestCode, if (uri == null) Activity.RESULT_CANCELED else Activity.RESULT_OK, uri?.let { Intent().setData(it) },
        ))
    }

    private fun await(scene: NativeComposeScene, condition: () -> Boolean) {
        repeat(100) {
            scene.layout(scene.activityView)
            if (condition()) return
            Thread.sleep(12)
        }
        throw AssertionError("The expected view state did not appear. Visible text: ${scene.activityTexts()}")
    }

    private fun render(
        name: String, route: String, dark: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit,
    ): NativeComposeScene = NativeComposeScene(File("build/transformation-16")).apply {
        setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = scale, reduceMotion = true) { content() }
            }
        }
        capture(activityView, name)
    }

    private class InMemoryPinMaterialStore : PinMaterialStore {
        private var material: Pair<String, String>? = null
        override fun storePinMaterial(salt: String, verifier: String) { material = salt to verifier }
        override fun getPinSalt() = material?.first
        override fun getPinVerifier() = material?.second
        override fun hasPin() = material != null
        override fun clearPin() { material = null }
    }
}
