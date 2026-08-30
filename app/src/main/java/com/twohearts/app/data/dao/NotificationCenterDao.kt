package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.NotificationCenterEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NotificationCenterEntry entity.
 * Matches legacy NotificationCenterRepository functionality.
 */
@Dao
interface NotificationCenterDao {
    @Query("SELECT * FROM notification_center WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun getAll(): Flow<List<NotificationCenterEntry>>

    @Query("SELECT * FROM notification_center WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): NotificationCenterEntry?

    @Query("SELECT * FROM notification_center WHERE read = 0 AND deleted_at IS NULL ORDER BY created_at DESC")
    fun getUnread(): Flow<List<NotificationCenterEntry>>

    @Query("SELECT COUNT(*) FROM notification_center WHERE read = 0 AND deleted_at IS NULL")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(entry: NotificationCenterEntry)

    @Update
    suspend fun update(entry: NotificationCenterEntry)

    @Query("UPDATE notification_center SET read = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun markAsRead(id: String, updatedAt: String)

    @Query("UPDATE notification_center SET read = 1, updated_at = :updatedAt WHERE read = 0")
    suspend fun markAllAsRead(updatedAt: String)

    @Query("UPDATE notification_center SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM notification_center WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
