package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Place entity — represents a meaningful place in the couple's story.
 * Matches legacy places table (migration 008).
 */
@Entity(tableName = "places")
data class Place(
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "city")
    val city: String? = null,

    @ColumnInfo(name = "state")
    val state: String? = null,

    @ColumnInfo(name = "country")
    val country: String? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "category")
    val category: String = "general", // "restaurant", "park", "home", "travel", "other", "general"

    @ColumnInfo(name = "photo_ref")
    val photoRef: String? = null, // Media asset ID

    @ColumnInfo(name = "memory_id")
    val memoryId: String? = null, // Optional associated memory

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
