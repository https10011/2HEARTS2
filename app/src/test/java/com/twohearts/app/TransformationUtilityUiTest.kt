package com.twohearts.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.data.settings.AppSettings
import com.twohearts.app.services.datamanagement.StorageReport
import com.twohearts.app.services.datamanagement.prepareTransfer
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.ui.components.NotificationPermissionAction
import com.twohearts.app.ui.navigation.LocalShellRoute
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.screens.about.AboutScreen
import com.twohearts.app.ui.screens.more.MoreScreen
import com.twohearts.app.ui.screens.search.SearchScreen
import com.twohearts.app.ui.screens.settings.AppearanceSettingsContent
import com.twohearts.app.ui.screens.settings.ImportScreen
import com.twohearts.app.ui.screens.settings.NotificationSettingsContent
import com.twohearts.app.ui.screens.settings.ProfileSettingsScreen
import com.twohearts.app.ui.screens.settings.RelationshipSettingsScreen
import com.twohearts.app.ui.screens.settings.SettingsHomeScreen
import com.twohearts.app.ui.screens.settings.StorageSettingsContent
import com.twohearts.app.ui.theme.TwoHeartsTheme
import java.io.ByteArrayOutputStream
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
class TransformationUtilityUiTest {
    private val stamp = "2026-11-25T00:00:00Z"
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val owner = Profile("Alex Rivera", "1995-03-05", "owner", id = "owner", createdAt = stamp, updatedAt = stamp)
    private val partner = Profile("Sam Rivera", role = "partner", id = "partner", createdAt = stamp, updatedAt = stamp)
    private val relationship = CoupleRelationship(owner.id, partner.id, "2021-05-04", "couple", stamp, stamp)

    @Test
    fun nativeUtilityIndexesRetainDirectRoutesAndPackageInformation() {
        val opened = mutableListOf<String>()
        render("more-light", RoutePath.APP_MORE) { MoreScreen { opened += it } }.use { scene ->
            assertTrue(scene.click("Search"))
            assertTrue(scene.click("Notifications"))
            assertTrue(scene.click("Notes and reminders transfer"))
            assertEquals(listOf(RoutePath.APP_SEARCH, RoutePath.APP_NOTIFICATIONS, RoutePath.APP_SETTINGS_IMPORT), opened)
        }
        render("settings-light", RoutePath.APP_SETTINGS) { SettingsHomeScreen({ opened += it }, {}) }.use { scene ->
            assertTrue(scene.click("Your profile"))
            assertTrue(scene.click("Relationship"))
            assertTrue(scene.click("Security"))
            assertEquals(listOf(RoutePath.APP_SETTINGS_PROFILE, RoutePath.APP_SETTINGS_RELATIONSHIP, RoutePath.APP_SETTINGS_SECURITY), opened.takeLast(3))
        }
        render("about-light", RoutePath.APP_ABOUT) { AboutScreen {} }.use { scene ->
            assertTrue(scene.activityText("Version 1.0.0 · Build 1"))
            assertTrue(scene.activityText("Yuki"))
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativeProfileEditorsKeepLocalPhotoChoiceAndSaveReachableAtLargeText() {
        var saved: List<Any?>? = null
        render("profile-dark-large", RoutePath.APP_SETTINGS_PROFILE, dark = true, scale = 2f) {
            ProfileSettingsScreen({}, profile = owner, onSave = { name, date, photo -> saved = listOf(name, date, photo) })
        }.use { scene ->
            assertTrue(scene.setText("Your name", " Alex and a longer saved family name "))
            assertTrue(scene.clickDescription("Birthday (optional): 5 March 1995"))
            scene.captureDialog("profile-birthday-dark-large")
            assertTrue(scene.clickDialog("Clear date"))
            assertTrue(scene.click("Save profile"))
            assertEquals(listOf("Alex and a longer saved family name", null, ProfilePhotoChange.Keep), saved)
        }
        render("relationship-dark-large", RoutePath.APP_SETTINGS_RELATIONSHIP, dark = true, scale = 2f) {
            RelationshipSettingsScreen({}, partner, relationship, onSave = { name, date, photo -> saved = listOf(name, date, photo) })
        }.use { scene ->
            assertTrue(scene.setText("Partner’s name", " Sam Bell "))
            assertTrue(scene.click("Save relationship"))
            assertEquals(listOf("Sam Bell", "2021-05-04", ProfilePhotoChange.Keep), saved)
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h618dp-mdpi")
    fun nativeAppearanceAndDeniedNotificationSettingsWrapTheirControls() {
        val values = mutableListOf<String>()
        render("appearance-dark-large", RoutePath.APP_SETTINGS_APPEARANCE, dark = true, scale = 2f) {
            AppearanceSettingsContent("dark", "extra_large", false, {}, { values += it }, { values += it }, { values += "motion=$it" })
        }.use { scene ->
            assertTrue(scene.click("Light"))
            assertTrue(scene.click("Large"))
            assertTrue(scene.scrollActivityBy(700f))
            assertTrue(scene.click("Reduce motion"))
            assertEquals(listOf("light", "large", "motion=true"), values)
            scene.capture(scene.activityView, "appearance-motion-dark-large")
        }

        render("notification-settings-denied-dark-large", RoutePath.APP_SETTINGS_NOTIFICATIONS, dark = true, scale = 2f) {
            NotificationSettingsContent(
                AppSettings(notificationsEnabled = false), NotificationPermissionAction.SETTINGS, {},
                { values += "system" }, { values += "request" }, { values += "allowed=$it" }, { values += "reminders=$it" },
            )
        }.use { scene ->
            assertTrue(scene.activityText("Android alerts are off"))
            assertFalse(scene.activityText("Allow Android alerts"))
            assertTrue(scene.click("Android notification settings"))
            assertTrue(scene.scrollActivityBy(650f))
            assertFalse(scene.click("Reminders"))
            assertTrue(scene.click("TwoHearts notifications"))
            assertEquals(listOf("system", "allowed=true"), values.takeLast(2))
            assertFalse(values.any { it.startsWith("reminders=") })
            scene.capture(scene.activityView, "notification-settings-toggles-dark-large")
        }
    }

    @Test
    fun nativeStorageSeparatesTemporaryFileCleanupFromResetConfirmation() {
        val report = StorageReport(2, 4, 7, 3, 5, 2, 1, 4, 2, 7, 9, 2_470_000, 5_100_000)
        val actions = mutableListOf<String>()
        render("storage-light", RoutePath.APP_SETTINGS_STORAGE) {
            StorageSettingsContent(report, false, false, null, {}, {}, { actions += "clear" }, { actions += "reset" })
        }.use { scene ->
            assertTrue(scene.click("Show record counts"))
            assertTrue(scene.activityText("Notes"))
            scene.capture(scene.activityView, "storage-counts-light")
            assertTrue(scene.click("Hide record counts"))
            assertTrue(scene.click("Clear temporary files"))
            scene.captureDialog("storage-clear-dialog-light")
            assertTrue(scene.clickDialog("Clear files"))
            assertEquals(listOf("clear"), actions)
            assertTrue(scene.click("Reset all app data"))
            assertTrue(scene.dialogText("Erase all TwoHearts data?"))
            scene.captureDialog("storage-reset-dialog-light")
            assertTrue(scene.clickDialog("Cancel"))
            assertEquals(listOf("clear"), actions)
            assertTrue(scene.click("Reset all app data"))
            assertTrue(scene.clickDialog("Erase all data"))
            assertEquals(listOf("clear", "reset"), actions)
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativeSearchUpdatesFromRoomAndDoesNotSurfaceAVaultMatch() {
        val db = Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
        val notes = NoteRepository(db.noteDao())
        val engine = SearchEngine()
        val memories = MemoryRepository(db.memoryDao(), db.memoryMediaDao())
        val reminders = ReminderRepository(db.reminderDao())
        val places = PlaceRepository(db.placeDao())
        val timeline = TimelineEventRepository(db.timelineEventDao())

        val note = Note(
            "An evening at the café by the river", "The tide came in while the lamps went on.", "shared",
            "saved-note", stamp, stamp,
        )
        var opened: Pair<String, String>? = null
        try {
            runBlocking {
                notes.create(note)
                db.vaultItemDao().insert(VaultItem("Private café record", "note", "A private note", id = "vault", createdAt = stamp, updatedAt = stamp))
            }
            render("search-empty-dark-large", RoutePath.APP_SEARCH, dark = true, scale = 2f) {
                SearchScreen(
                    engine, notes, memories, reminders, places, timeline, { type, id -> opened = type to id }, {},
                )
            }.use { scene ->
                assertTrue(scene.setText("Search TwoHearts", "  CAFE  "))
                awaitText(scene, "1 result")
                scene.capture(scene.activityView, "search-results-dark-large")
                assertFalse(scene.activityDescription("Private café record, Vault"))
                assertTrue(scene.clickDescription("${note.title}, Notes"))
                assertEquals("note" to note.id, opened)
                runBlocking { notes.softDelete(note.id) }
                awaitText(scene, "No matches")
                scene.capture(scene.activityView, "search-no-match-dark-large")
                assertTrue(scene.clickDescription("Clear search"))
                awaitText(scene, "Find something you saved")
            }
        } finally {
            db.close()
        }
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h740dp-mdpi")
    fun nativeTransferPickerCancellationInvalidFilePreviewAndExportUseLocalDocuments() {
        val db = Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
        val notes = NoteRepository(db.noteDao())
        val reminders = ReminderRepository(db.reminderDao())
        try {
            render("transfer-idle-dark-large", RoutePath.APP_SETTINGS_IMPORT, dark = true, scale = 2f) {
                ImportScreen(notes, reminders, {}, db)
            }.use { scene ->
                val uri = Uri.parse("content://twohearts.transfer.fixture/input")
                val outputUri = Uri.parse("content://twohearts.transfer.fixture/output")
                val resolver = shadowOf(scene.controller.get().contentResolver)
                assertTrue(scene.click("Choose JSON file"))
                deliverPickerResult(scene, Intent.ACTION_OPEN_DOCUMENT, null)
                awaitText(scene, "Choose JSON file")

                var source = """{"profiles":[]}"""
                resolver.registerInputStreamSupplier(uri) { source.byteInputStream() }
                assertTrue(scene.click("Choose JSON file"))
                deliverPickerResult(scene, Intent.ACTION_OPEN_DOCUMENT, uri)
                awaitText(scene, "Couldn’t read a TwoHearts Notes and Reminders JSON file from that location.")
                scene.capture(scene.activityView, "transfer-error-dark-large")

                source = """{"notes":[{"title":"An evening at the café","content":"The train ride home"}],"reminders":[{"title":"Invalid date","scheduledDate":"2026-02-29"}]}"""
                assertTrue(scene.click("Choose JSON file"))
                deliverPickerResult(scene, Intent.ACTION_OPEN_DOCUMENT, uri)
                awaitText(scene, "1 note and 0 reminders ready to add")
                assertTrue(scene.activityText("1 invalid entry"))
                scene.capture(scene.activityView, "transfer-preview-dark-large")
                source = """{"notes":[{"title":"Changed document","content":"Other content"}]}"""
                assertTrue(scene.click("Add records"))
                awaitText(scene, "Imported 1 note and 0 reminders. Skipped 1 invalid entry.")
                assertEquals("An evening at the café", runBlocking { notes.observeAll().first().single().title })
                scene.capture(scene.activityView, "transfer-result-dark-large")

                val output = ByteArrayOutputStream()
                resolver.registerOutputStream(outputUri, output)
                assertTrue(scene.click("Export Notes and Reminders"))
                deliverPickerResult(scene, Intent.ACTION_CREATE_DOCUMENT, outputUri)
                awaitText(scene, "Exported 1 note and 0 reminders.")
                val exported = prepareTransfer(output.toString(Charsets.UTF_8.name()).reader(), stamp)
                assertEquals("An evening at the café", exported.notes.single().title)
                assertEquals(0, exported.reminders.size)
            }
        } finally {
            db.close()
        }
    }

    private fun deliverPickerResult(scene: NativeComposeScene, expectedAction: String, uri: Uri?) {
        val activity = scene.controller.get()
        val request = checkNotNull(shadowOf(activity).nextStartedActivityForResult)
        assertEquals(expectedAction, request.intent.action)
        assertTrue(activity.activityResultRegistry.dispatchResult(
            request.requestCode, if (uri == null) Activity.RESULT_CANCELED else Activity.RESULT_OK,
            uri?.let { Intent().setData(it) },
        ))
    }

    private fun awaitText(scene: NativeComposeScene, label: String) {
        repeat(100) {
            scene.layout(scene.activityView)
            if (scene.activityText(label)) return
            Thread.sleep(15)
        }
        scene.capture(scene.activityView, "await-failure")
        throw AssertionError("Did not find “$label” in the rendered screen. Text nodes: ${scene.activityTexts()}")
    }

    private fun render(
        name: String, route: String, dark: Boolean = false, scale: Float = 1f, content: @Composable () -> Unit,
    ): NativeComposeScene = NativeComposeScene(File("build/transformation-15")).apply {
        setContent {
            CompositionLocalProvider(LocalShellRoute provides route) {
                TwoHeartsTheme(darkTheme = dark, textSizeScale = scale, reduceMotion = true) { content() }
            }
        }
        capture(activityView, name)
    }
}
