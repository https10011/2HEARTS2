package com.twohearts.app

import android.app.Application
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.DatabaseInitializer
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.datamanagement.encodeTransferDocument
import com.twohearts.app.services.datamanagement.prepareTransfer
import com.twohearts.app.services.datamanagement.writeTransfer
import com.twohearts.app.services.media.FileService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.ui.screens.settings.validProfileDate
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationUtilityDataTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val stamp = "2026-11-25T00:00:00Z"
    private val legacy = """
        {
          "notes": [
            {"title":"Café evening","content":"Lights across the river.\nOur walk home.","category":"love-letter"},
            {"title":"Missing content","content":null}
          ],
          "reminders": [
            {"title":"Dinner","scheduledDate":"2028-02-29","scheduledTime":"19:30","description":null,
             "recurrence":"yearly","status":"completed","notificationEnabled":false},
            {"title":"Bad date","scheduledDate":"2027-02-29"}
          ]
        }
    """.trimIndent()

    @Test
    fun legacyAndVersionedTransferRoundTripPreservesContentAndDefaults() {
        val first = prepareTransfer(legacy.reader(), stamp)
        assertEquals(2, first.skipped)
        assertEquals(2, first.examples.size)
        assertEquals("Lights across the river.\nOur walk home.", first.notes.single().content)
        assertEquals("love-letter", first.notes.single().category)
        val reminder = first.reminders.single()
        assertEquals(null, reminder.description)
        assertEquals("2028-02-29", reminder.scheduledDate)
        assertEquals("19:30", reminder.scheduledTime)
        assertEquals("yearly", reminder.recurrence)
        assertEquals("completed", reminder.status)
        assertFalse(reminder.notificationEnabled)
        val document = encodeTransferDocument(first.notes, first.reminders)
        val reopened = prepareTransfer(document.reader(), stamp)
        assertEquals(0, reopened.skipped)
        assertNotEquals(first.notes.single().id, reopened.notes.single().id)
        assertEquals(first.notes.single().copy(id = reopened.notes.single().id), reopened.notes.single())
        assertEquals(reminder.copy(id = reopened.reminders.single().id), reopened.reminders.single())
        assertFalse(document.contains("profiles"))
        assertFalse(document.contains("vault"))
    }

    @Test
    fun invalidDocumentsAndEmptyPreviewDoNotCreateAnImportCandidate() {
        val invalid = listOf(
            """{"version":2,"notes":[]}""", """{"format":"something-else","reminders":[]}""",
            """{"profiles":[]}""", """{"notes":[]""",
        )
        for (document in invalid) assertTrue(runCatching { prepareTransfer(document.reader(), stamp) }.isFailure)
        val empty = prepareTransfer("""{"notes":[null,{},{"title":7,"content":"Text"}],"reminders":[]}""".reader(), stamp)
        assertFalse(empty.canImport)
        assertEquals(3, empty.skipped)
        val cutoff = """{"notes":[]}""" + " ".repeat(4_194_304)
        assertThrows(IllegalArgumentException::class.java) { prepareTransfer(cutoff.reader(), stamp) }
    }

    @Test
    fun transferCommitRollsBackBothRecordTypesOnFailureAndRetainsRowsAfterReopen() = runBlocking {
        val fileName = "stage15-transfer"
        val batch = prepareTransfer(legacy.reader(), stamp)
        try {
            val db = Room.databaseBuilder(app, TwoHeartsDatabase::class.java, fileName).build()
            try {
                val notes = NoteRepository(db.noteDao())
                val reminders = ReminderRepository(db.reminderDao())
                val existing = batch.reminders.single().copy(title = "Existing record")
                reminders.create(existing)
                assertTrue(runCatching { writeTransfer(db, batch, notes, reminders) }.isFailure)
                assertEquals(0, notes.count())
                assertEquals(existing, reminders.getById(existing.id))
                val next = prepareTransfer(legacy.reader(), stamp)
                writeTransfer(db, next, notes, reminders)
                assertEquals(1, notes.count())
                assertEquals(2, reminders.count())
            } finally {
                db.close()
            }
            val reopened = Room.databaseBuilder(app, TwoHeartsDatabase::class.java, fileName).build()
            try {
                assertEquals("Café evening", reopened.noteDao().getAll().first().single().title)
                assertEquals(listOf("Dinner", "Existing record"), reopened.reminderDao().getAll().first().map { it.title }.sorted())
            } finally {
                reopened.close()
            }
        } finally {
            app.deleteDatabase(fileName)
        }
    }

    @Test
    fun clearingTemporaryFilesRetainsRegisteredMedia() = runBlocking {
        val initializer = DatabaseInitializer(app)
        val db = initializer.initialize()
        val media = MediaStorage(app)
        val service = DataManagementService(app, initializer, media, FileService(app))
        val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        val saved = checkNotNull(media.storeBitmap(bitmap))
        val orphan = checkNotNull(media.storeBitmap(bitmap))
        bitmap.recycle()
        try {
            db.mediaAssetDao().insert(MediaAsset(
                saved.filePath, saved.mimeType, saved.fileSize, width = saved.width, height = saved.height,
                id = saved.id, createdAt = stamp, updatedAt = stamp,
            ))
            assertTrue(service.getStorageReport().mediaBytes >= saved.fileSize + orphan.fileSize)
            assertTrue(service.clearCache().orphanFilesRemoved >= 1)
            assertTrue(saved.id in media.getAllMediaIds())
            assertFalse(orphan.id in media.getAllMediaIds())
            assertEquals(saved.filePath, db.mediaAssetDao().getById(saved.id)?.filePath)
        } finally {
            db.mediaAssetDao().softDelete(saved.id, stamp, stamp)
            media.delete(saved.id)
            media.delete(orphan.id)
        }
    }

    @Test
    fun appearanceAndNotificationChoicesAreAvailableToARecreatedService() = runBlocking {
        val storage = SettingsStorage.getInstance(app)
        val before = storage.settings.first()
        try {
            val service = AppStateService(storage)
            service.setThemeMode("dark")
            service.setTextSize("extra_large")
            service.setReduceMotion(true)
            storage.setNotificationsEnabled(false)
            storage.setRemindersEnabled(false)
            val recreated = AppStateService(storage)
            recreated.initialize()
            assertEquals("dark", recreated.themeMode.value)
            assertEquals("extra_large", recreated.textSize.value)
            assertTrue(recreated.reduceMotion.value)
            assertFalse(storage.settings.first().notificationsEnabled)
            assertFalse(storage.settings.first().remindersEnabled)
            assertFalse(validProfileDate("2024-02-30", LocalDate.parse("2024-03-01")))
            assertFalse(validProfileDate("2024-03-02", LocalDate.parse("2024-03-01")))
            assertTrue(validProfileDate("2024-02-29", LocalDate.parse("2024-03-01")))
        } finally {
            storage.updateThemeMode(before.themeMode)
            storage.updateTextSize(before.textSize)
            storage.setReduceMotion(before.reduceMotion)
            storage.setNotificationsEnabled(before.notificationsEnabled)
            storage.setRemindersEnabled(before.remindersEnabled)
        }
    }
}
