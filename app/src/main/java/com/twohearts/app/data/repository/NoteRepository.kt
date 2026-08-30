package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.NoteDao
import com.twohearts.app.data.entity.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Note entity.
 * Matches legacy NoteRepository functionality exactly.
 */
class NoteRepository(private val dao: NoteDao) : BaseRepository<Note> {

    fun observeAll(): Flow<List<Note>> = dao.getAll()

    fun observeByCategory(category: String): Flow<List<Note>> = dao.getByCategory(category)

    override suspend fun getAll(): List<Note> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): Note? = dao.getById(id)

    override suspend fun create(entity: Note): Note {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): Note? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            title = changes["title"] as? String ?: existing.title,
            content = changes["content"] as? String ?: existing.content,
            category = changes["category"] as? String ?: existing.category,
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
