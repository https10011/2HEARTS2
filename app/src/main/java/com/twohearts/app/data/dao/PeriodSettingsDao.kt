package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.PeriodSettings
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PeriodSettings entity.
 * Matches legacy PeriodSettingsRepository functionality.
 */
@Dao
interface PeriodSettingsDao {
    @Query("SELECT * FROM period_settings WHERE deleted_at IS NULL LIMIT 1")
    fun get(): Flow<PeriodSettings?>

    @Query("SELECT * FROM period_settings WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): PeriodSettings?

    @Insert
    suspend fun insert(periodSettings: PeriodSettings)

    @Update
    suspend fun update(periodSettings: PeriodSettings)

    @Query("UPDATE period_settings SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM period_settings WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
