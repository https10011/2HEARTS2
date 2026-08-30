package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
