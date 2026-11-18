package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("""
        SELECT * FROM mood_entries
        WHERE entry_date = :date AND profile_id IN (:profileId, 'owner') AND deleted_at IS NULL
        ORDER BY (profile_id = :profileId) DESC, updated_at DESC, id DESC LIMIT 1
    """)
    suspend fun getOwnerCheckIn(date: String, profileId: String): MoodEntry?

    @Transaction
    suspend fun saveOwnerCheckIn(entry: MoodEntry, keepNote: Boolean) {
        val existing = getOwnerCheckIn(entry.entryDate, entry.profileId)
        if (existing == null) {
            insert(entry)
        } else {
            update(existing.copy(
                profileId = entry.profileId,
                moodValue = entry.moodValue,
                moodEmoji = entry.moodEmoji,
                note = if (keepNote) existing.note else entry.note,
                updatedAt = entry.updatedAt,
            ))
        }
    }

    @Insert
    suspend fun insert(moodEntry: MoodEntry)

    @Update
    suspend fun update(moodEntry: MoodEntry)

    @Query("UPDATE mood_entries SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM mood_entries WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
