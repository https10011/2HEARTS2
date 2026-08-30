package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.MoodEntryDao
import com.twohearts.app.data.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for MoodEntry entity.
 * Matches legacy MoodEntryRepository functionality exactly.
 */
class MoodEntryRepository(private val dao: MoodEntryDao) : BaseRepository<MoodEntry> {

    fun observeAll(): Flow<List<MoodEntry>> = dao.getAll()

    fun observeByProfileId(profileId: String): Flow<List<MoodEntry>> = dao.getByProfileId(profileId)

    override suspend fun getAll(): List<MoodEntry> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): MoodEntry? = dao.getById(id)

    suspend fun getByDate(date: String): MoodEntry? = dao.getByDate(date)

    override suspend fun create(entity: MoodEntry): MoodEntry {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): MoodEntry? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            moodValue = changes["moodValue"] as? String ?: existing.moodValue,
            moodEmoji = changes["moodEmoji"] as? String ?: existing.moodEmoji,
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
