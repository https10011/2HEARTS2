package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.NotificationCenterDao
import com.twohearts.app.data.entity.NotificationCenterEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for NotificationCenterEntry entity.
 * Matches legacy NotificationCenterRepository functionality exactly.
 */
class NotificationCenterRepository(private val dao: NotificationCenterDao) : BaseRepository<NotificationCenterEntry> {

    fun observeAll(): Flow<List<NotificationCenterEntry>> = dao.getAll()

    fun observeUnread(): Flow<List<NotificationCenterEntry>> = dao.getUnread()

    fun observeUnreadCount(): Flow<Int> = dao.getUnreadCount()

    override suspend fun getAll(): List<NotificationCenterEntry> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): NotificationCenterEntry? = dao.getById(id)

    override suspend fun create(entity: NotificationCenterEntry): NotificationCenterEntry {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): NotificationCenterEntry? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            body = changes["body"] as? String ?: existing.body,
            kind = changes["kind"] as? String ?: existing.kind,
            originFeature = changes["originFeature"] as? String ?: existing.originFeature,
            originId = changes["originId"] as? String ?: existing.originId,
            channelId = changes["channelId"] as? String ?: existing.channelId,
            read = changes["read"] as? Boolean ?: existing.read,
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

    suspend fun markAsRead(id: String) {
        val now = nowUtc()
        dao.markAsRead(id, now)
    }

    suspend fun markAllAsRead() {
        val now = nowUtc()
        dao.markAllAsRead(now)
    }
}
