package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

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

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
