package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PeriodEntry entity — represents a menstrual cycle entry.
 * Matches legacy period_entries table (migration 010).
 */
@Entity(tableName = "period_entries")
data class PeriodEntry(
    @ColumnInfo(name = "start_date")
    val startDate: String, // LOCAL calendar key (yyyy-mm-dd)

    @ColumnInfo(name = "end_date")
    val endDate: String? = null, // LOCAL calendar key (yyyy-mm-dd)

    @ColumnInfo(name = "flow_level")
    val flowLevel: String = "medium", // "light", "medium", "heavy"

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
