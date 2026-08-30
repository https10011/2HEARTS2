package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.MemoryDao
import com.twohearts.app.data.dao.MemoryMediaDao
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.MemoryMedia
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Memory entity.
 * Matches legacy MemoryRepository functionality exactly.
 */
class MemoryRepository(
    private val dao: MemoryDao,
    private val memoryMediaDao: MemoryMediaDao
) : BaseRepository<Memory> {

    fun observeAll(): Flow<List<Memory>> = dao.getAll()

    override suspend fun getAll(): List<Memory> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): Memory? = dao.getById(id)

    override suspend fun create(entity: Memory): Memory {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): Memory? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            caption = changes["caption"] as? String ?: existing.caption,
            memoryDate = changes["memoryDate"] as? String ?: existing.memoryDate,
            sortOrder = changes["sortOrder"] as? Int ?: existing.sortOrder,
            updatedAt = nowUtc()
        )
        dao.update(updated)
        return updated
    }

    override suspend fun softDelete(id: String) {
        val now = nowUtc()
        dao.softDelete(id, now, now)
        // Cascade: remove media associations
        memoryMediaDao.deleteByMemoryId(id)
    }

    override suspend fun count(): Int = dao.count()

    suspend fun addMedia(memoryId: String, mediaAssetId: String, sortOrder: Int = 0) {
        val memoryMedia = MemoryMedia(
            memoryId = memoryId,
            mediaAssetId = mediaAssetId,
            sortOrder = sortOrder
        )
        memoryMediaDao.insert(memoryMedia)
    }

    suspend fun removeMedia(memoryId: String, mediaAssetId: String) {
        memoryMediaDao.delete(memoryId, mediaAssetId)
    }

    suspend fun getMedia(memoryId: String): List<MemoryMedia> {
        return memoryMediaDao.getByMemoryId(memoryId)
    }
}
