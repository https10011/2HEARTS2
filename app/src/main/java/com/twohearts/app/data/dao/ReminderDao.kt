package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Reminder entity.
 * Matches legacy ReminderRepository functionality.
 */
@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE deleted_at IS NULL ORDER BY scheduled_date ASC, scheduled_time ASC")
    fun getAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): Reminder?

    @Query("SELECT * FROM reminders WHERE status = 'pending' AND deleted_at IS NULL ORDER BY scheduled_date ASC, scheduled_time ASC")
    fun getPending(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder)

    @Update
    suspend fun update(reminder: Reminder)

    @Query("UPDATE reminders SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM reminders WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
