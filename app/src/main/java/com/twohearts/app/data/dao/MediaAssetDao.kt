package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.twohearts.app.data.entity.MediaAsset
import kotlinx.coroutines.flow.Flow

data class PlaceAttachment(val placeId: String, @Embedded val asset: MediaAsset)

/**
 * Data Access Object for MediaAsset entity.
 * Matches legacy MediaAssetRepository functionality.
 */
@Dao
interface MediaAssetDao {
    @Query("""
        SELECT places.id AS placeId, assets.* FROM places
        JOIN media_assets AS assets ON assets.id = places.photo_ref
        WHERE places.deleted_at IS NULL AND assets.deleted_at IS NULL
    """)
    fun observePlaceAttachments(): Flow<List<PlaceAttachment>>

    @Query("SELECT * FROM media_assets WHERE deleted_at IS NULL ORDER BY created_at DESC")
    fun getAll(): Flow<List<MediaAsset>>

    @Query("SELECT * FROM media_assets WHERE id = :id AND deleted_at IS NULL")
    suspend fun getById(id: String): MediaAsset?

    @Insert
    suspend fun insert(mediaAsset: MediaAsset)

    @Update
    suspend fun update(mediaAsset: MediaAsset)

    @Query("UPDATE media_assets SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: String, updatedAt: String)

    @Query("SELECT COUNT(*) FROM media_assets WHERE deleted_at IS NULL")
    suspend fun count(): Int
}
