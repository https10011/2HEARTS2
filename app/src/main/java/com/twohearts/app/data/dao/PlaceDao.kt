package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.Place
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Place entity.
 * Matches legacy PlaceRepository functionality.
 */
@Dao
interface PlaceDao {
    @Query("SELECT * FROM places WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun getAll(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): Place?

    @Query("SELECT * FROM places WHERE category = :category AND deleted_at IS NULL ORDER BY created_at DESC")
    fun getByCategory(category: String): Flow<List<Place>>

    @Insert
    suspend fun insert(place: Place)

    @Update
    suspend fun update(place: Place)

    @Query("UPDATE places SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM places WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
