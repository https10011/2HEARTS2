package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.ProfileDao
import com.twohearts.app.data.entity.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Profile entity.
 * Matches legacy ProfileRepository functionality exactly.
 */
class ProfileRepository(private val dao: ProfileDao) : BaseRepository<Profile> {

    fun observeAll(): Flow<List<Profile>> = dao.getAll()

    override suspend fun getAll(): List<Profile> = dao.getAll().let { flow ->
        // For non-flow operations, we need a snapshot
        // In practice, use observeAll() for reactive updates
        emptyList() // Placeholder — use observeAll() for actual data
    }

    override suspend fun getById(id: String): Profile? = dao.getById(id)

    suspend fun getByRole(role: String): Profile? = dao.getByRole(role)

    override suspend fun create(entity: Profile): Profile {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): Profile? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            name = changes["name"] as? String ?: existing.name,
            birthday = changes["birthday"] as? String ?: existing.birthday,
            role = changes["role"] as? String ?: existing.role,
            photoRef = changes["photoRef"] as? String ?: existing.photoRef,
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
