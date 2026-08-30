package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MoodEntry entity — represents a daily mood check-in.
 * Matches legacy mood_entries table (migration 009).
 */
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @ColumnInfo(name = "mood_value")
    val moodValue: String, // "happy", "love", "excited", "calm", "grateful", "neutral", "tired", "sad", "anxious", "stressed"

    @ColumnInfo(name = "mood_emoji")
    val moodEmoji: String,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "entry_date")
    val entryDate: String, // LOCAL calendar key (yyyy-mm-dd)

    @PrimaryKey


    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "deleted_at") val deletedAt: String? = null
)
