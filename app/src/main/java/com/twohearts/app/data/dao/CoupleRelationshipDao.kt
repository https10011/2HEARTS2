package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.CoupleRelationship
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for CoupleRelationship entity.
 * Matches legacy CoupleRelationshipRepository functionality.
 */
@Dao
interface CoupleRelationshipDao {
    @Query("SELECT * FROM couple_relationship WHERE deleted_at IS NULL LIMIT 1")
    fun get(): Flow<CoupleRelationship?>

    @Query("SELECT * FROM couple_relationship WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): CoupleRelationship?

    @Insert
    suspend fun insert(relationship: CoupleRelationship)

    @Update
    suspend fun update(relationship: CoupleRelationship)

    @Query("UPDATE couple_relationship SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM couple_relationship WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
