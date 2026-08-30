package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * NotificationCenterEntry entity — represents a notification in the center.
 * Matches legacy notification_center table (migration 012).
 */
@Entity(tableName = "notification_center")
data class NotificationCenterEntry(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "kind")
    val kind: String, // "reminder", "anniversary", "mood", "general"

    @ColumnInfo(name = "origin_feature")
    val originFeature: String, // "reminders", "important-dates", "mood", "general"

    @ColumnInfo(name = "origin_id")
    val originId: String? = null, // ID of the originating entity

    @ColumnInfo(name = "channel_id")
    val channelId: String? = null,

    @ColumnInfo(name = "read")
    val read: Boolean = false,

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
