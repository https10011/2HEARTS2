package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
