package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CoupleRelationship entity — represents the couple bond.
 * Matches legacy couple_relationship table (migration 003).
 */
@Entity(tableName = "couple_relationship")
data class CoupleRelationship(
    @ColumnInfo(name = "owner_id")
    val ownerId: String,

    @ColumnInfo(name = "partner_id")
    val partnerId: String,

    @ColumnInfo(name = "start_date")
    val startDate: String, // LOCAL calendar key (yyyy-mm-dd)

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
