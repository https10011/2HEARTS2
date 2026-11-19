package com.twohearts.app.services.datamanagement

import android.content.Context
import androidx.room.withTransaction
import com.twohearts.app.data.database.DatabaseInitializer
import com.twohearts.app.services.media.FileService
import com.twohearts.app.services.media.MediaStorage
import android.app.ActivityManager
import com.twohearts.app.services.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DataManagementService — data management operations.
 *
 * Matches legacy DataManagementService exactly:
 * - Storage report (row counts, media bytes)
 * - Cache clear (orphan media sweep)
 * - Full reset (cancel notifications → delete domain rows → sweep media → remove PIN → reset settings)
 */
class DataManagementService(
    private val context: Context,
    private val databaseInitializer: DatabaseInitializer,
    private val mediaStorage: MediaStorage,
    private val fileService: FileService,
) {

    private val logger = Logger("DataManagement")

    /**
     * Get storage report.
     */
    suspend fun getStorageReport(): StorageReport {
        val db = databaseInitializer.getDatabase()

        return StorageReport(
            profileCount = db?.profileDao()?.count() ?: 0,
            memoryCount = db?.memoryDao()?.count() ?: 0,
            noteCount = db?.noteDao()?.count() ?: 0,
            timelineEventCount = db?.timelineEventDao()?.count() ?: 0,
            reminderCount = db?.reminderDao()?.count() ?: 0,
            placeCount = db?.placeDao()?.count() ?: 0,
            moodEntryCount = db?.moodEntryDao()?.count() ?: 0,
            periodEntryCount = db?.periodEntryDao()?.count() ?: 0,
            vaultItemCount = db?.vaultItemDao()?.count() ?: 0,
            notificationCount = db?.notificationCenterDao()?.count() ?: 0,
            mediaAssetCount = db?.mediaAssetDao()?.count() ?: 0,
            mediaBytes = mediaStorage.getTotalMediaSize(),
            totalAppSize = fileService.getTotalAppSize()
        )
    }

    /**
     * Clear cache (orphan media sweep).
     */
    suspend fun clearCache(): CacheClearResult {
        val db = databaseInitializer.getDatabase()
        val cleaned = db?.withTransaction {
            val knownMediaIds = db.mediaAssetDao().retainedIds().toSet()
            withContext(Dispatchers.IO) { mediaStorage.cleanupOrphans(knownMediaIds) }
        } ?: 0

        logger.info("Cache cleared: $cleaned orphan files removed")
        return CacheClearResult(orphanFilesRemoved = cleaned)
    }

    /** Android erases this package's app data and terminates its process. */
    fun requestFullReset(): Boolean = try {
        context.getSystemService(ActivityManager::class.java)?.clearApplicationUserData() == true
    } catch (failure: SecurityException) {
        logger.error("Android declined the app data reset", failure)
        false
    }
}

/**
 * Storage report data class.
 */
data class StorageReport(
    val profileCount: Int,
    val memoryCount: Int,
    val noteCount: Int,
    val timelineEventCount: Int,
    val reminderCount: Int,
    val placeCount: Int,
    val moodEntryCount: Int,
    val periodEntryCount: Int,
    val vaultItemCount: Int,
    val notificationCount: Int,
    val mediaAssetCount: Int,
    val mediaBytes: Long,
    val totalAppSize: Long
)

/**
 * Cache clear result data class.
 */
data class CacheClearResult(
    val orphanFilesRemoved: Int
)
