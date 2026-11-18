package com.twohearts.app

import android.app.ActivityManager
import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.DatabaseInitializer
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.datamanagement.encodeTransferDocument
import com.twohearts.app.services.datamanagement.readTransferDocument
import com.twohearts.app.services.media.FileService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.services.search.SearchResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase12UtilitiesTest {
    @Test fun searchFindsAccentsInTitlesAndDetails() {
        val engine = SearchEngine()
        val results = engine.searchRecords("CAFE", listOf(
            SearchResult("1", "note", "A Saturday walk", "We stopped at the café.", 0),
            SearchResult("2", "place", "Café du Port", "Harbour", 0),
            SearchResult("3", "memory", "Sunday", "The coast", 0),
        ))
        assertEquals(listOf("2", "1"), results.map { it.id })
        assertTrue(engine.searchRecords(" \u0301 ", results).isEmpty())
    }

    @Test fun exportUsesTheSameVersionedFormatAsImport() {
        val note = Note("Café", "A quiet evening", "shared", "n", "x", "x")
        val reminder = Reminder(
            title = "Call later", scheduledDate = "2026-11-18", notificationEnabled = false,
            status = "completed", id = "r", createdAt = "x", updatedAt = "x",
        )
        val doc = readTransferDocument(encodeTransferDocument(listOf(note), listOf(reminder)).reader())
        val savedNote = doc.getJSONArray("notes").getJSONObject(0)
        val savedReminder = doc.getJSONArray("reminders").getJSONObject(0)
        assertEquals("Café", savedNote.getString("title"))
        assertEquals("shared", savedNote.getString("category"))
        assertEquals("completed", savedReminder.getString("status"))
        assertFalse(savedReminder.getBoolean("notificationEnabled"))
        assertTrue(savedReminder.isNull("description"))
        assertFalse(doc.has("profiles"))
        assertThrows(IllegalArgumentException::class.java) {
            readTransferDocument("""{"format":"unrecognized","notes":[]}""".reader())
        }
    }

    @Test fun savingOwnerAllowsRemovingABirthdayAndKeepsThePhoto() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, TwoHeartsDatabase::class.java).build()
        try {
            val profiles = ProfileRepository(db.profileDao())
            val relationship = RelationshipService(profiles, CoupleRelationshipRepository(db.coupleRelationshipDao()))
            val owner = relationship.createOwner("Alex", "1991-02-03")
            relationship.saveOwnerDetails(owner.id, "Alex", "1991-02-03", "saved-photo")
            relationship.saveOwnerDetails(owner.id, "Avery", null, null)
            val saved = requireNotNull(relationship.getOwner())
            assertEquals(owner.id, saved.id)
            assertEquals("Avery", saved.name)
            assertNull(saved.birthday)
            assertEquals("saved-photo", saved.photoRef)
        } finally {
            db.close()
        }
    }

    @Test fun resetDelegatesToAndroidsPackageDataClear() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val service = DataManagementService(app, DatabaseInitializer(app), MediaStorage(app), FileService(app))
        val manager = shadowOf(app.getSystemService(ActivityManager::class.java))
        assertFalse(manager.isApplicationUserDataCleared)
        assertTrue(service.requestFullReset())
        assertTrue(manager.isApplicationUserDataCleared)
    }
}
