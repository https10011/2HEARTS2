package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.Memory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Memory entity.
 * Matches legacy MemoryRepository functionality.
 */
@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE deleted_at IS NULL ORDER BY memory_date DESC")
    fun getAll(): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): Memory?

    @Insert
    suspend fun insert(memory: Memory)

    @Update
    suspend fun update(memory: Memory)

    @Query("UPDATE memories SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM memories WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
