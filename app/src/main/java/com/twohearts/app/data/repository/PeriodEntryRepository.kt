package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.PeriodEntryDao
import com.twohearts.app.data.entity.PeriodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for PeriodEntry entity.
 * Matches legacy PeriodEntryRepository functionality exactly.
 */
class PeriodEntryRepository(private val dao: PeriodEntryDao) : BaseRepository<PeriodEntry> {

    fun observeAll(): Flow<List<PeriodEntry>> = dao.getAll()

    fun observeByProfileId(profileId: String): Flow<List<PeriodEntry>> = dao.getByProfileId(profileId)

    override suspend fun getAll(): List<PeriodEntry> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): PeriodEntry? = dao.getById(id)

    override suspend fun create(entity: PeriodEntry): PeriodEntry {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): PeriodEntry? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            startDate = changes["startDate"] as? String ?: existing.startDate,
            endDate = changes["endDate"] as? String ?: existing.endDate,
            flowLevel = changes["flowLevel"] as? String ?: existing.flowLevel,
            note = changes["note"] as? String ?: existing.note,
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
