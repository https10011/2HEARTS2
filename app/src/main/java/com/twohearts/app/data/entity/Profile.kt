package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Profile entity — represents a person in the couple.
 * Matches legacy profiles table (migration 003 + 013).
 */
@Entity(tableName = "profiles")
data class Profile(
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "birthday")
    val birthday: String? = null,

    @ColumnInfo(name = "role")
    val role: String, // "owner" or "partner"

    @ColumnInfo(name = "photo_ref")
    val photoRef: String? = null, // Media asset ID (added in migration 013)

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
