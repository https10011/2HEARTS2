package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * TimelineEvent entity — represents a milestone or event in the couple's story.
 * Matches legacy timeline_events table (migration 006).
 */
@Entity(tableName = "timeline_events")
data class TimelineEvent(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "event_date")
    val eventDate: String, // LOCAL calendar key (yyyy-mm-dd)

    @ColumnInfo(name = "description")
    val description: String? = null,

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
