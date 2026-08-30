package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
