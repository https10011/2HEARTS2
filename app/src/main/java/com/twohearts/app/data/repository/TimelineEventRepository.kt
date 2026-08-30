package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.TimelineEventDao
import com.twohearts.app.data.entity.TimelineEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository for TimelineEvent entity.
 * Matches legacy TimelineEventRepository functionality exactly.
 */
class TimelineEventRepository(private val dao: TimelineEventDao) : BaseRepository<TimelineEvent> {

    fun observeAll(): Flow<List<TimelineEvent>> = dao.getAll()

    override suspend fun getAll(): List<TimelineEvent> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): TimelineEvent? = dao.getById(id)

    override suspend fun create(entity: TimelineEvent): TimelineEvent {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): TimelineEvent? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            eventDate = changes["eventDate"] as? String ?: existing.eventDate,
            description = changes["description"] as? String ?: existing.description,
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
