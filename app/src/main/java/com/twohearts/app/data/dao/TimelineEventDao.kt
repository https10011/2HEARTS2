package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.TimelineEvent
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TimelineEvent entity.
 * Matches legacy TimelineEventRepository functionality.
 */
@Dao
interface TimelineEventDao {
    @Query("SELECT * FROM timeline_events WHERE deleted_at IS NULL ORDER BY event_date DESC")
    fun getAll(): Flow<List<TimelineEvent>>

    @Query("SELECT * FROM timeline_events WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): TimelineEvent?

    @Insert
    suspend fun insert(event: TimelineEvent)

    @Update
    suspend fun update(event: TimelineEvent)

    @Query("UPDATE timeline_events SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM timeline_events WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
