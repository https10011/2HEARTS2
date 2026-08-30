package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.ReminderDao
import com.twohearts.app.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Reminder entity.
 * Matches legacy ReminderRepository functionality exactly.
 */
class ReminderRepository(private val dao: ReminderDao) : BaseRepository<Reminder> {

    fun observeAll(): Flow<List<Reminder>> = dao.getAll()

    fun observePending(): Flow<List<Reminder>> = dao.getPending()

    override suspend fun getAll(): List<Reminder> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): Reminder? = dao.getById(id)

    override suspend fun create(entity: Reminder): Reminder {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): Reminder? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            description = changes["description"] as? String ?: existing.description,
            scheduledDate = changes["scheduledDate"] as? String ?: existing.scheduledDate,
            scheduledTime = changes["scheduledTime"] as? String ?: existing.scheduledTime,
            recurrence = changes["recurrence"] as? String ?: existing.recurrence,
            status = changes["status"] as? String ?: existing.status,
            notificationEnabled = changes["notificationEnabled"] as? Boolean ?: existing.notificationEnabled,
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
