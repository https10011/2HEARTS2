package com.twohearts.app.data.repository

import java.time.Instant
import java.util.UUID

/**
 * Base repository interface providing common CRUD operations.
 * Matches legacy BaseRepository<T> pattern exactly.
 */
interface BaseRepository<T> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: String): T?
    suspend fun create(entity: T): T
    suspend fun update(id: String, changes: Map<String, Any?>): T?
    suspend fun softDelete(id: String)
    suspend fun count(): Int
}

/**
 * Helper function to generate UUID v4.
 */
fun generateId(): String = UUID.randomUUID().toString()

/**
 * Helper function to get current ISO 8601 UTC timestamp.
 */
fun nowUtc(): String = Instant.now().toString()
