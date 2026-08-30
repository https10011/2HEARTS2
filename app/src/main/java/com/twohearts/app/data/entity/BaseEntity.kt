package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Base entity with UUID v4 primary key and ISO 8601 timestamps.
 * Matches legacy entity.ts conventions exactly.
 */
open class BaseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "created_at")
    val createdAt: String = java.time.Instant.now().toString(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: String = java.time.Instant.now().toString(),

    @ColumnInfo(name = "deleted_at")
    val deletedAt: String? = null
)

/**
 * Entity that supports soft deletes via tombstone.
 */
open class TombstonedEntity(
    id: String = UUID.randomUUID().toString(),
    createdAt: String = java.time.Instant.now().toString(),
    updatedAt: String = java.time.Instant.now().toString(),
    deletedAt: String? = null
) : BaseEntity(id, createdAt, updatedAt, deletedAt)
