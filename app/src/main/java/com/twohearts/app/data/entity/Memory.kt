package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Memory entity — represents a photo/video memory with caption.
 * Matches legacy memories table (migration 004).
 */
@Entity(tableName = "memories")
data class Memory(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "caption")
    val caption: String? = null,

    @ColumnInfo(name = "memory_date")
    val memoryDate: String, // LOCAL calendar key (yyyy-mm-dd)

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
