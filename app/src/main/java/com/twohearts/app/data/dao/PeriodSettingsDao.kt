package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.Update
import com.twohearts.app.data.entity.PeriodSettings
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PeriodSettings entity.
 * Matches legacy PeriodSettingsRepository functionality.
 */
@Dao
interface PeriodSettingsDao {
    @Query("SELECT * FROM period_settings WHERE deleted_at IS NULL ORDER BY updated_at DESC, id LIMIT 1")
    fun get(): Flow<PeriodSettings?>

    @Query("SELECT * FROM period_settings WHERE deleted_at IS NULL ORDER BY updated_at DESC, id LIMIT 1")
    suspend fun current(): PeriodSettings?

    @Upsert
    suspend fun upsert(settings: PeriodSettings)

    @Transaction
    suspend fun saveLengths(settings: PeriodSettings) {
        val existing = current()
        upsert(if (existing == null) settings else existing.copy(
            cycleLengthDays = settings.cycleLengthDays,
            periodLengthDays = settings.periodLengthDays,
            updatedAt = settings.updatedAt,
        ))
    }

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
