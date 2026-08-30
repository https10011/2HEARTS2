package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.MediaAssetDao
import com.twohearts.app.data.entity.MediaAsset
import kotlinx.coroutines.flow.Flow

/**
 * Repository for MediaAsset entity.
 * Matches legacy MediaAssetRepository functionality exactly.
 */
class MediaAssetRepository(private val dao: MediaAssetDao) : BaseRepository<MediaAsset> {

    fun observeAll(): Flow<List<MediaAsset>> = dao.getAll()

    override suspend fun getAll(): List<MediaAsset> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): MediaAsset? = dao.getById(id)

    override suspend fun create(entity: MediaAsset): MediaAsset {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): MediaAsset? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            filePath = changes["filePath"] as? String ?: existing.filePath,
            mimeType = changes["mimeType"] as? String ?: existing.mimeType,
            fileSize = changes["fileSize"] as? Long ?: existing.fileSize,
            width = changes["width"] as? Int ?: existing.width,
            height = changes["height"] as? Int ?: existing.height,
            durationMs = changes["durationMs"] as? Long ?: existing.durationMs,
            updatedAt = nowUtc()
        )
        dao.update(updated)
        return updated
    }

    override suspend fun softDelete(id: String) {
        val now = nowUtc()
        dao.softDelete(id, now, now)
    }

    override suspend fun count(): Int = dao.count()
}
