package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import com.twohearts.app.data.entity.MemoryMedia
import com.twohearts.app.data.entity.MediaAsset
import kotlinx.coroutines.flow.Flow

data class MemoryAttachment(val memoryId: String, @Embedded val asset: MediaAsset)

/**
 * Data Access Object for MemoryMedia join entity.
 * Matches legacy MemoryRepository media association functionality.
 */
@Dao
interface MemoryMediaDao {
    @Query("""
        SELECT links.memory_id AS memoryId, assets.* FROM memory_media AS links
        JOIN media_assets AS assets ON assets.id = links.media_asset_id
        JOIN memories ON memories.id = links.memory_id
        WHERE memories.deleted_at IS NULL AND assets.deleted_at IS NULL
        ORDER BY links.memory_id, links.sort_order, links.media_asset_id
    """)
    fun observeAttachments(): Flow<List<MemoryAttachment>>

    @Query("""
        SELECT assets.* FROM memory_media AS links
        JOIN media_assets AS assets ON assets.id = links.media_asset_id
        WHERE links.memory_id = :memoryId AND assets.deleted_at IS NULL
        ORDER BY links.sort_order, links.media_asset_id
    """)
    suspend fun getAssets(memoryId: String): List<MediaAsset>

    @Query("SELECT * FROM memory_media WHERE memory_id = :memoryId ORDER BY sort_order ASC")
    suspend fun getByMemoryId(memoryId: String): List<MemoryMedia>

    @Query("SELECT * FROM memory_media WHERE media_asset_id = :mediaAssetId")
    suspend fun getByMediaAssetId(mediaAssetId: String): List<MemoryMedia>

    @Insert
    suspend fun insert(memoryMedia: MemoryMedia)

    @Query("DELETE FROM memory_media WHERE memory_id = :memoryId AND media_asset_id = :mediaAssetId")
    suspend fun delete(memoryId: String, mediaAssetId: String)

    @Query("DELETE FROM memory_media WHERE memory_id = :memoryId")
    suspend fun deleteByMemoryId(memoryId: String)
}
