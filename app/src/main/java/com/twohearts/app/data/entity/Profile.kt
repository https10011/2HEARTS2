package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
