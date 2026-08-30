package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.ImportantDate
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ImportantDate entity.
 * Matches legacy ImportantDateRepository functionality.
 */
@Dao
interface ImportantDateDao {
    @Query("SELECT * FROM important_dates WHERE deleted_at IS NULL ORDER BY date ASC")
    fun getAll(): Flow<List<ImportantDate>>

    @Query("SELECT * FROM important_dates WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): ImportantDate?

    @Insert
    suspend fun insert(importantDate: ImportantDate)

    @Update
    suspend fun update(importantDate: ImportantDate)

    @Query("UPDATE important_dates SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM important_dates WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
