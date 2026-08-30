package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.PeriodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PeriodEntry entity.
 * Matches legacy PeriodRepository functionality.
 */
@Dao
interface PeriodEntryDao {
    @Query("SELECT * FROM period_entries WHERE deleted_at IS NULL ORDER BY start_date DESC")
    fun getAll(): Flow<List<PeriodEntry>>

    @Query("SELECT * FROM period_entries WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): PeriodEntry?

    @Query("SELECT * FROM period_entries WHERE profile_id = :profileId AND deleted_at IS NULL ORDER BY start_date DESC")
    fun getByProfileId(profileId: String): Flow<List<PeriodEntry>>

    @Insert
    suspend fun insert(periodEntry: PeriodEntry)

    @Update
    suspend fun update(periodEntry: PeriodEntry)

    @Query("UPDATE period_entries SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM period_entries WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
