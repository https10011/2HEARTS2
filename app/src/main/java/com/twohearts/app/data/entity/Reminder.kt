package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Reminder entity — represents a scheduled reminder for the partner.
 * Matches legacy reminders table (migration 007).
 */
@Entity(tableName = "reminders")
data class Reminder(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "scheduled_date")
    val scheduledDate: String, // LOCAL calendar key (yyyy-mm-dd)

    @ColumnInfo(name = "scheduled_time")
    val scheduledTime: String? = null, // HH:mm format

    @ColumnInfo(name = "recurrence")
    val recurrence: String = "none", // "none", "daily", "weekly", "monthly", "yearly"

    @ColumnInfo(name = "status")
    val status: String = "pending", // "pending", "completed", "missed"

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = true,

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
