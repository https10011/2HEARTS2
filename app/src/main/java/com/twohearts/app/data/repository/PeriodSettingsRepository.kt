package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.PeriodSettingsDao
import com.twohearts.app.data.entity.PeriodSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository for PeriodSettings entity.
 * Matches legacy PeriodSettingsRepository functionality exactly.
 */
class PeriodSettingsRepository(private val dao: PeriodSettingsDao) : BaseRepository<PeriodSettings> {

    fun observe(): Flow<PeriodSettings?> = dao.get()

    override suspend fun getAll(): List<PeriodSettings> = emptyList() // Use observe()

    override suspend fun getById(id: String): PeriodSettings? = dao.getById(id)

    override suspend fun create(entity: PeriodSettings): PeriodSettings {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): PeriodSettings? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            cycleLengthDays = changes["cycleLengthDays"] as? Int ?: existing.cycleLengthDays,
            periodLengthDays = changes["periodLengthDays"] as? Int ?: existing.periodLengthDays,
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
