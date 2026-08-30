package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

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

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
