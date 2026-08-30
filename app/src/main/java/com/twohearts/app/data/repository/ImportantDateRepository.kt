package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.ImportantDateDao
import com.twohearts.app.data.entity.ImportantDate
import kotlinx.coroutines.flow.Flow

/**
 * Repository for ImportantDate entity.
 * Matches legacy ImportantDateRepository functionality exactly.
 */
class ImportantDateRepository(private val dao: ImportantDateDao) : BaseRepository<ImportantDate> {

    fun observeAll(): Flow<List<ImportantDate>> = dao.getAll()

    override suspend fun getAll(): List<ImportantDate> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): ImportantDate? = dao.getById(id)

    override suspend fun create(entity: ImportantDate): ImportantDate {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): ImportantDate? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            date = changes["date"] as? String ?: existing.date,
            recurrence = changes["recurrence"] as? String ?: existing.recurrence,
            profileId = changes["profileId"] as? String ?: existing.profileId,
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
