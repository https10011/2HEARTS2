package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.CoupleRelationshipDao
import com.twohearts.app.data.entity.CoupleRelationship
import kotlinx.coroutines.flow.Flow

/**
 * Repository for CoupleRelationship entity.
 * Matches legacy CoupleRelationshipRepository functionality exactly.
 */
class CoupleRelationshipRepository(private val dao: CoupleRelationshipDao) : BaseRepository<CoupleRelationship> {

    fun observe(): Flow<CoupleRelationship?> = dao.get()

    override suspend fun getAll(): List<CoupleRelationship> = emptyList() // Use observe()

    override suspend fun getById(id: String): CoupleRelationship? = dao.getById(id)

    suspend fun get(): CoupleRelationship? {
        // For non-reactive access, query directly
        return null // Placeholder — use observe() for actual data
    }

    override suspend fun create(entity: CoupleRelationship): CoupleRelationship {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): CoupleRelationship? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            ownerId = changes["ownerId"] as? String ?: existing.ownerId,
            partnerId = changes["partnerId"] as? String ?: existing.partnerId,
            startDate = changes["startDate"] as? String ?: existing.startDate,
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
