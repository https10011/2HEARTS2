package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.VaultItemDao
import com.twohearts.app.data.entity.VaultItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository for VaultItem entity.
 * Matches legacy VaultItemRepository functionality exactly.
 */
class VaultItemRepository(private val dao: VaultItemDao) : BaseRepository<VaultItem> {

    fun observeAll(): Flow<List<VaultItem>> = dao.getAll()

    fun observeByContentType(contentType: String): Flow<List<VaultItem>> = dao.getByContentType(contentType)

    override suspend fun getAll(): List<VaultItem> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): VaultItem? = dao.getById(id)

    override suspend fun create(entity: VaultItem): VaultItem {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): VaultItem? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            contentType = changes["contentType"] as? String ?: existing.contentType,
            contentText = changes["contentText"] as? String ?: existing.contentText,
            mediaRef = changes["mediaRef"] as? String ?: existing.mediaRef,
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
