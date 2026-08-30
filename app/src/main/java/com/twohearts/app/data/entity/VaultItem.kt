package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * VaultItem entity — represents a private vault content item.
 * Matches legacy vault_items table (migration 011).
 */
@Entity(tableName = "vault_items")
data class VaultItem(
    @ColumnInfo(name = "title")
    val title: String? = null,

    @ColumnInfo(name = "content_type")
    val contentType: String, // "photo", "video", "note", "file"

    @ColumnInfo(name = "content_text")
    val contentText: String? = null, // For "note" type

    @ColumnInfo(name = "media_ref")
    val mediaRef: String? = null, // Media asset ID for "photo", "video", "file"

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
