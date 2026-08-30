package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Profile entity.
 * Matches legacy ProfileRepository functionality.
 */
@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE deleted_at IS NULL ORDER BY created_at ASC")
    fun getAll(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): Profile?

    @Query("SELECT * FROM profiles WHERE role = :role AND deleted_at IS NULL")
    suspend fun getByRole(role: String): Profile?

    @Insert
    suspend fun insert(profile: Profile)

    @Update
    suspend fun update(profile: Profile)

    @Query("UPDATE profiles SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM profiles WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
