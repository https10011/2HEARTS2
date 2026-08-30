package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * NotificationRegistry entity — bridges domain identity to OS notification ID.
 * Matches legacy notification_registry table (migration 002).
 */
@Entity(tableName = "notification_registry")
data class NotificationRegistry(
    @ColumnInfo(name = "owner_ref")
    val ownerRef: String, // Unique — one pending notification per logical event

    @ColumnInfo(name = "os_notification_id")
    val osNotificationId: Int,

    @ColumnInfo(name = "channel_id")
    val channelId: String,

    @ColumnInfo(name = "meta_json")
    val metaJson: String? = null, // Scheduling metadata

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
