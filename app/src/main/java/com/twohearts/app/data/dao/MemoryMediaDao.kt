package com.twohearts.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.twohearts.app.data.entity.MemoryMedia

/**
 * Data Access Object for MemoryMedia join entity.
 * Matches legacy MemoryRepository media association functionality.
 */
@Dao
interface MemoryMediaDao {
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
