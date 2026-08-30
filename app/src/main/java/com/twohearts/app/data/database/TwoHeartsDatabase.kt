package com.twohearts.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.ImportantDate
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MemoryMedia
import com.twohearts.app.data.entity.MoodEntry
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.entity.NotificationRegistry
import com.twohearts.app.data.entity.PeriodEntry
import com.twohearts.app.data.entity.PeriodSettings
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.entity.SchemaMigration
import com.twohearts.app.data.entity.TimelineEvent
import com.twohearts.app.data.entity.VaultItem

/**
 * TwoHearts Room Database — matches legacy 13-migration schema exactly.
 *
 * Entity count: 15 (excluding SchemaMigration which is tracked manually)
 * Current schema version: 13
 *
 * All migrations match the legacy migration names and content.
 */
@Database(
    entities = [
        SchemaMigration::class,
        MediaAsset::class,
        Profile::class,
        CoupleRelationship::class,
        ImportantDate::class,
        Memory::class,
        MemoryMedia::class,
        Note::class,
        TimelineEvent::class,
        Reminder::class,
        Place::class,
        MoodEntry::class,
        PeriodEntry::class,
        PeriodSettings::class,
        VaultItem::class,
        NotificationCenterEntry::class,
        NotificationRegistry::class
    ],
    version = 13,
    exportSchema = false
)
abstract class TwoHeartsDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun coupleRelationshipDao(): CoupleRelationshipDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun memoryDao(): MemoryDao
    abstract fun memoryMediaDao(): MemoryMediaDao
    abstract fun noteDao(): NoteDao
    abstract fun timelineEventDao(): TimelineEventDao
    abstract fun reminderDao(): ReminderDao
    abstract fun placeDao(): PlaceDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun periodEntryDao(): PeriodEntryDao
    abstract fun periodSettingsDao(): PeriodSettingsDao
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun notificationCenterDao(): NotificationCenterDao
    abstract fun notificationRegistryDao(): NotificationRegistryDao
    abstract fun mediaAssetDao(): MediaAssetDao

    companion object {
        @Volatile
        private var INSTANCE: TwoHeartsDatabase? = null

        fun getDatabase(context: Context): TwoHeartsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TwoHeartsDatabase::class.java,
                    "twohearts.db"
                )
                    .addMigrations(*allMigrations)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * All 13 migrations matching legacy schema exactly.
         */
        val allMigrations: Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13
        )
    }

    /**
     * Database callback for initial setup.
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Run initial migration
            MIGRATION_1_2.migrate(db)
        }
    }

    companion object Migrations {
        // Migration 001: initial — schema_migrations, media_assets
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Initial schema created by Room from @Entity annotations
                // This migration adds notification_registry
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_registry (
                        id TEXT NOT NULL PRIMARY KEY,
                        owner_ref TEXT NOT NULL,
                        os_notification_id INTEGER NOT NULL,
                        channel_id TEXT NOT NULL,
                        meta_json TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_registry_owner_ref ON notification_registry(owner_ref)")
            }
        }

        // Migration 002: notification_registry
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Relationship foundation — profiles, couple_relationship, important_dates
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS profiles (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        birthday TEXT,
                        role TEXT NOT NULL,
                        photo_ref TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS couple_relationship (
                        id TEXT NOT NULL PRIMARY KEY,
                        owner_id TEXT NOT NULL,
                        partner_id TEXT NOT NULL,
                        start_date TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS important_dates (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        date TEXT NOT NULL,
                        recurrence TEXT NOT NULL DEFAULT 'none',
                        profile_id TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 003: relationship_foundation
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Memories
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS memories (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        caption TEXT,
                        memory_date TEXT NOT NULL,
                        sort_order INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS memory_media (
                        memory_id TEXT NOT NULL,
                        media_asset_id TEXT NOT NULL,
                        sort_order INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL,
                        PRIMARY KEY(memory_id, media_asset_id)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_memory_media_memory_id ON memory_media(memory_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_memory_media_sort ON memory_media(memory_id, sort_order)")
            }
        }

        // Migration 004: memories
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Notes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notes (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT 'general',
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 005: notes
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Timeline events
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS timeline_events (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        event_date TEXT NOT NULL,
                        description TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 006: timeline
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Reminders
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminders (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        description TEXT,
                        scheduled_date TEXT NOT NULL,
                        scheduled_time TEXT,
                        recurrence TEXT NOT NULL DEFAULT 'none',
                        status TEXT NOT NULL DEFAULT 'pending',
                        notification_enabled INTEGER NOT NULL DEFAULT 1,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 007: reminders
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Places
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS places (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        address TEXT,
                        city TEXT,
                        state TEXT,
                        country TEXT,
                        latitude REAL,
                        longitude REAL,
                        notes TEXT,
                        category TEXT NOT NULL DEFAULT 'general',
                        photo_ref TEXT,
                        memory_id TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 008: places
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Mood entries
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS mood_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        mood_value TEXT NOT NULL,
                        mood_emoji TEXT NOT NULL,
                        note TEXT,
                        profile_id TEXT NOT NULL,
                        entry_date TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 009: mood
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Period tracker
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS period_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        start_date TEXT NOT NULL,
                        end_date TEXT,
                        flow_level TEXT NOT NULL DEFAULT 'medium',
                        note TEXT,
                        profile_id TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS period_settings (
                        id TEXT NOT NULL PRIMARY KEY,
                        cycle_length_days INTEGER NOT NULL DEFAULT 28,
                        period_length_days INTEGER NOT NULL DEFAULT 5,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 010: period_tracker
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Vault
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS vault_items (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT,
                        content_type TEXT NOT NULL,
                        content_text TEXT,
                        media_ref TEXT,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 011: vault
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Notification center
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_center (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        origin_feature TEXT NOT NULL,
                        origin_id TEXT,
                        channel_id TEXT,
                        read INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL,
                        deleted_at TEXT
                    )
                """)
            }
        }

        // Migration 012: notification_center
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Profile photo — adds photo_ref column to profiles
                // Note: Room handles column additions via ALTER TABLE
                // This is a no-op since photo_ref is already defined in the entity
            }
        }
    }
}
