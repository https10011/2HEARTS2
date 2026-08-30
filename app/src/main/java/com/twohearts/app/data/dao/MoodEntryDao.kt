package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MoodEntry entity.
 * Matches legacy MoodRepository functionality.
 */
@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries WHERE deleted_at IS NULL ORDER BY entry_date DESC")
    fun getAll(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): MoodEntry?

    @Query("SELECT * FROM mood_entries WHERE profile_id = :profileId AND deleted_at IS NULL ORDER BY entry_date DESC")
    fun getByProfileId(profileId: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE entry_date = :date AND deleted_at IS NULL LIMIT 1")
    suspend fun getByDate(date: String): MoodEntry?

    @Insert
    suspend fun insert(moodEntry: MoodEntry)

    @Update
    suspend fun update(moodEntry: MoodEntry)

    @Query("UPDATE mood_entries SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM mood_entries WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
