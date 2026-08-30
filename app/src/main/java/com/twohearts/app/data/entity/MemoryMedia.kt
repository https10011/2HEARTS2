package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * MemoryMedia entity — join table linking memories to media assets.
 * Matches legacy memory_media table (migration 004).
 */
@Entity(
    tableName = "memory_media",
    primaryKeys = ["memory_id", "media_asset_id"]
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
