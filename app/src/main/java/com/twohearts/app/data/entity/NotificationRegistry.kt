package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
