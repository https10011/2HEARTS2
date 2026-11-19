package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * MemoryMedia entity — join table linking memories to media assets.
 * Matches legacy memory_media table (migration 004).
 */
@Entity(
    tableName = "memory_media",
    primaryKeys = ["memory_id", "media_asset_id"],
    indices = [
        Index(value = ["memory_id"], name = "idx_memory_media_memory_id"),
        Index(value = ["memory_id", "sort_order"], name = "idx_memory_media_sort"),
    ],
)
data class MemoryMedia(
    @ColumnInfo(name = "memory_id")
    val memoryId: String,

    @ColumnInfo(name = "media_asset_id")
    val mediaAssetId: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: String = java.time.Instant.now().toString()
)
