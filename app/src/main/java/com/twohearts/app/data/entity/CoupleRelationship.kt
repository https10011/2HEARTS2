package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

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

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
