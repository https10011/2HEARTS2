package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
