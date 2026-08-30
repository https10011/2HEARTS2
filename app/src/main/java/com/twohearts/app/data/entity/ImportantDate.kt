package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ImportantDate entity — represents anniversaries, birthdays, etc.
 * Matches legacy important_dates table (migration 003).
 */
@Entity(tableName = "important_dates")
data class ImportantDate(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "date")
    val date: String, // LOCAL calendar key (yyyy-mm-dd)

    @ColumnInfo(name = "recurrence")
    val recurrence: String, // "none", "yearly", "monthly", "weekly", "daily"

    @ColumnInfo(name = "profile_id")
    val profileId: String? = null, // Optional association to a profile

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
