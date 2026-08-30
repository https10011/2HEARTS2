package com.twohearts.app.data.repository

import com.twohearts.app.data.dao.PlaceDao
import com.twohearts.app.data.entity.Place
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Place entity.
 * Matches legacy PlaceRepository functionality exactly.
 */
class PlaceRepository(private val dao: PlaceDao) : BaseRepository<Place> {

    fun observeAll(): Flow<List<Place>> = dao.getAll()

    fun observeByCategory(category: String): Flow<List<Place>> = dao.getByCategory(category)

    override suspend fun getAll(): List<Place> = emptyList() // Use observeAll()

    override suspend fun getById(id: String): Place? = dao.getById(id)

    override suspend fun create(entity: Place): Place {
        dao.insert(entity)
        return entity
    }

    override suspend fun update(id: String, changes: Map<String, Any?>): Place? {
        val existing = dao.getById(id) ?: return null
        val updated = existing.copy(
            name = changes["name"] as? String ?: existing.name,
            address = changes["address"] as? String ?: existing.address,
            city = changes["city"] as? String ?: existing.city,
            state = changes["state"] as? String ?: existing.state,
            country = changes["country"] as? String ?: existing.country,
            latitude = changes["latitude"] as? Double ?: existing.latitude,
            longitude = changes["longitude"] as? Double ?: existing.longitude,
            notes = changes["notes"] as? String ?: existing.notes,
            category = changes["category"] as? String ?: existing.category,
            photoRef = changes["photoRef"] as? String ?: existing.photoRef,
            memoryId = changes["memoryId"] as? String ?: existing.memoryId,
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
