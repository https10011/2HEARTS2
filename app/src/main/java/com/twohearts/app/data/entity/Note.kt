package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Note entity — represents a love letter, gratitude, idea, etc.
 * Matches legacy notes table (migration 005).
 */
@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "category")
    val category: String, // "general", "shared", "private", "love-letter", "gratitude", "idea", "reminder"

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
