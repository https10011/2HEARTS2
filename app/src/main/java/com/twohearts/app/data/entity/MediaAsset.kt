package com.twohearts.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * MediaAsset entity — represents a photo, video, or file.
 * Matches legacy media_assets table (migration 001).
 */
@Entity(tableName = "media_assets")
data class MediaAsset(
    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "width")
    val width: Int? = null,

    @ColumnInfo(name = "height")
    val height: Int? = null,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long? = null,

    id: String,
    createdAt: String,
    updatedAt: String,
    deletedAt: String? = null
) : TombstonedEntity(id, createdAt, updatedAt, deletedAt)
