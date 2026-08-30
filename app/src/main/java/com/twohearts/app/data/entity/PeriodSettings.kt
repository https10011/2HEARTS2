package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * PeriodSettings entity — stores user's cycle/period length configuration.
 * Matches legacy period_settings table (migration 010).
 */
@Entity(tableName = "period_settings")
data class PeriodSettings(
    @ColumnInfo(name = "cycle_length_days")
    val cycleLengthDays: Int = 28,

    @ColumnInfo(name = "period_length_days")
    val periodLengthDays: Int = 5,

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
