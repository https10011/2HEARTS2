package com.twohearts.app.data.database

import android.content.Context
import com.twohearts.app.data.dao.CoupleRelationshipDao
import com.twohearts.app.data.dao.ImportantDateDao
import com.twohearts.app.data.dao.MediaAssetDao
import com.twohearts.app.data.dao.MemoryDao
import com.twohearts.app.data.dao.MemoryMediaDao
import com.twohearts.app.data.dao.MoodEntryDao
import com.twohearts.app.data.dao.NoteDao
import com.twohearts.app.data.dao.NotificationCenterDao
import com.twohearts.app.data.dao.NotificationRegistryDao
import com.twohearts.app.data.dao.PeriodEntryDao
import com.twohearts.app.data.dao.PeriodSettingsDao
import com.twohearts.app.data.dao.PlaceDao
import com.twohearts.app.data.dao.ProfileDao
import com.twohearts.app.data.dao.ReminderDao
import com.twohearts.app.data.dao.TimelineEventDao
import com.twohearts.app.data.dao.VaultItemDao

/**
 * DatabaseInitializer — handles database creation and verification.
 *
 * Matches legacy initializeDatabase() + verifySchemaVersion() pattern.
 */
class DatabaseInitializer(private val context: Context) {

    private var database: TwoHeartsDatabase? = null

    /**
     * Initialize the database.
     * This runs all pending migrations.
     */
    suspend fun initialize(): TwoHeartsDatabase {
        val db = TwoHeartsDatabase.getDatabase(context)
        database = db
        return db
    }

    /**
     * Verify the schema version is correct.
     */
    suspend fun verifySchemaVersion(): Boolean {
        val db = database ?: return false
        // Room handles schema verification automatically
        // This is a no-op placeholder for the legacy verification step
        return true
    }

    /**
     * Get the database instance.
     */
    fun getDatabase(): TwoHeartsDatabase? = database

    // DAO accessors
    fun profileDao(): ProfileDao = database!!.profileDao()
    fun coupleRelationshipDao(): CoupleRelationshipDao = database!!.coupleRelationshipDao()
    fun importantDateDao(): ImportantDateDao = database!!.importantDateDao()
    fun memoryDao(): MemoryDao = database!!.memoryDao()
    fun memoryMediaDao(): MemoryMediaDao = database!!.memoryMediaDao()
    fun noteDao(): NoteDao = database!!.noteDao()
    fun timelineEventDao(): TimelineEventDao = database!!.timelineEventDao()
    fun reminderDao(): ReminderDao = database!!.reminderDao()
    fun placeDao(): PlaceDao = database!!.placeDao()
    fun moodEntryDao(): MoodEntryDao = database!!.moodEntryDao()
    fun periodEntryDao(): PeriodEntryDao = database!!.periodEntryDao()
    fun periodSettingsDao(): PeriodSettingsDao = database!!.periodSettingsDao()
    fun vaultItemDao(): VaultItemDao = database!!.vaultItemDao()
    fun notificationCenterDao(): NotificationCenterDao = database!!.notificationCenterDao()
    fun notificationRegistryDao(): NotificationRegistryDao = database!!.notificationRegistryDao()
    fun mediaAssetDao(): MediaAssetDao = database!!.mediaAssetDao()
}
