package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.Note
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Note entity.
 * Matches legacy NoteRepository functionality.
 */
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deleted_at IS NULL ORDER BY updated_at DESC")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): Note?

    @Query("SELECT * FROM notes WHERE category = :category AND deleted_at IS NULL ORDER BY updated_at DESC")
    fun getByCategory(category: String): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("UPDATE notes SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM notes WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
