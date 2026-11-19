package com.twohearts.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.NotificationRegistry
import com.twohearts.app.data.entity.Profile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase20DataRetentionTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun anExistingLocalRecordSurvivesAReopen() = runBlocking {
        val name = "phase20-current.db"
        val time = "2026-11-19T00:00:00Z"
        val written = TwoHeartsDatabase.buildDatabase(context, name)
        try {
            written.noteDao().insert(Note("Existing note", "Local content", "general", id = "note", createdAt = time, updatedAt = time))
        } finally {
            written.close()
        }
        val reopened = TwoHeartsDatabase.buildDatabase(context, name)
        try {
            assertEquals("Local content", reopened.noteDao().getById("note")?.content)
        } finally {
            reopened.close()
            context.deleteDatabase(name)
        }
        Unit
    }

    @Test
    fun anUnsupportedVersionKeepsItsOriginalTablesAndRows() {
        val name = "phase20-retention.db"
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { existing ->
            existing.execSQL("CREATE TABLE retained (value TEXT NOT NULL)")
            existing.execSQL("INSERT INTO retained (value) VALUES ('existing record')")
            existing.version = 15
        }
        val attempted = TwoHeartsDatabase.buildDatabase(context, name)
        try {
            val problem = assertThrows(IllegalStateException::class.java) { attempted.openHelper.writableDatabase }
            assertTrue(problem.message.orEmpty().contains("15 to 14"))
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { preserved ->
                assertEquals(15, preserved.version)
                preserved.rawQuery("SELECT value FROM retained", null).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("existing record", cursor.getString(0))
                }
            }
        } finally {
            attempted.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun versionTwelveWithoutPhotoColumnKeepsTheProfileAndAddsTheColumn() = runBlocking {
        val name = "profile-no-photo-v12.db"
        val profile = existingProfile()
        try {
            val written = TwoHeartsDatabase.buildDatabase(context, name)
            try {
                written.profileDao().insert(profile)
            } finally {
                written.close()
            }
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { existing ->
                existing.execSQL(
                    """
                    CREATE TABLE profiles_v12 (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        birthday TEXT,
                        role TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                    """.trimIndent()
                )
                existing.execSQL(
                    """
                    INSERT INTO profiles_v12
                    SELECT id, name, birthday, role, created_at, updated_at, deleted_at FROM profiles
                    """.trimIndent()
                )
                existing.execSQL("DROP TABLE profiles")
                existing.execSQL("ALTER TABLE profiles_v12 RENAME TO profiles")
                existing.version = 12
            }

            val upgraded = TwoHeartsDatabase.buildDatabase(context, name)
            try {
                assertEquals(profile, upgraded.profileDao().getById(profile.id))
                assertEquals(14, upgraded.openHelper.writableDatabase.version)
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test
    fun versionTwelveWithPhotoColumnKeepsTheExistingReference() = runBlocking {
        val name = "profile-with-photo-v12.db"
        val profile = existingProfile().copy(photoRef = "existing-media")
        try {
            val written = TwoHeartsDatabase.buildDatabase(context, name)
            try {
                written.profileDao().insert(profile)
            } finally {
                written.close()
            }
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { existing ->
                existing.version = 12
            }

            val upgraded = TwoHeartsDatabase.buildDatabase(context, name)
            try {
                assertEquals(profile, upgraded.profileDao().getById(profile.id))
                assertEquals(14, upgraded.openHelper.writableDatabase.version)
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test
    fun shippedVersionThirteenKeepsLocalRecordsDuringIndexMigration() = runBlocking {
        val name = "shipped-v13.db"
        val profile = existingProfile().copy(photoRef = "stored-photo")
        val note = Note("Saved note", "Local content", "general", id = "note",
            createdAt = profile.createdAt, updatedAt = profile.updatedAt)
        val notification = NotificationRegistry(
            ownerRef = "reminder:record",
            osNotificationId = 42,
            channelId = "reminders",
            id = "notification",
            createdAt = profile.createdAt,
            updatedAt = profile.updatedAt,
        )
        try {
            val written = TwoHeartsDatabase.buildDatabase(context, name)
            try {
                written.profileDao().insert(profile)
                written.noteDao().insert(note)
                written.notificationRegistryDao().insert(notification)
            } finally {
                written.close()
            }
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use { existing ->
                existing.execSQL("DROP INDEX idx_memory_media_memory_id")
                existing.execSQL("DROP INDEX idx_memory_media_sort")
                existing.version = 13
            }

            val upgraded = TwoHeartsDatabase.buildDatabase(context, name)
            try {
                assertEquals(profile, upgraded.profileDao().getById(profile.id))
                assertEquals(note, upgraded.noteDao().getById(note.id))
                assertEquals(notification, upgraded.notificationRegistryDao().getById(notification.id))
                assertEquals(14, upgraded.openHelper.writableDatabase.version)
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private fun existingProfile() = Profile(
        name = "Jamie",
        birthday = "1994-02-10",
        role = "owner",
        id = "profile",
        createdAt = "2026-11-19T00:00:00Z",
        updatedAt = "2026-11-19T01:00:00Z",
    )
}
