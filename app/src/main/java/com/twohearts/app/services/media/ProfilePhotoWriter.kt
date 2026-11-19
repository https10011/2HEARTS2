package com.twohearts.app.services.media

import androidx.room.withTransaction
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.services.datetime.DateTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.IOException

/** Saves the asset and the profile reference in the same Room transaction. */
internal class ProfilePhotoWriter(
    private val database: TwoHeartsDatabase,
    private val media: MediaStorage,
    private val staging: ProfilePhotoStaging,
) {
    /** [edit] writes the profile and returns its previous photo reference if it was detached. */
    suspend fun commit(change: ProfilePhotoChange, edit: suspend (replacementId: String?) -> String?) {
        val assets = database.mediaAssetDao()
        var copied: MediaAssetInfo? = null
        var detached: String? = null
        try {
            database.withTransaction {
                if (change is ProfilePhotoChange.Replace) {
                    val cropped = staging.croppedUri(change.draftId) ?: throw IOException("Photo draft is missing")
                    withContext(Dispatchers.IO) { copied = media.store(cropped, "image/jpeg") }
                    val replacement = copied ?: throw IOException("Could not store photo")
                    val width = replacement.width
                    if (width == null || width !in 1..1024 || replacement.height != width) {
                        throw IOException("Photo draft must be a bounded square")
                    }
                    val now = DateTimeHelper.nowUtc()
                    assets.insert(MediaAsset(
                        filePath = replacement.filePath, mimeType = replacement.mimeType,
                        fileSize = replacement.fileSize, width = replacement.width, height = replacement.height,
                        id = replacement.id, createdAt = now, updatedAt = now,
                    ))
                }
                detached = edit(copied?.id)
            }
        } finally {
            // Cancellation can arrive after SQLite commits. A reference check
            // also protects a photo shared by the second profile or a record.
            withContext(NonCancellable) {
                for (id in listOfNotNull(copied?.id, detached).distinct()) {
                    runCatching { removeIfUnreferenced(id) }
                }
            }
        }
        if (change is ProfilePhotoChange.Replace) staging.discard(change.draftId)
    }

    private suspend fun removeIfUnreferenced(id: String) {
        if (!localMediaId.matches(id)) return
        val assets = database.mediaAssetDao()
        database.withTransaction {
            if (!assets.isReferenced(id)) {
                val now = DateTimeHelper.nowUtc()
                assets.softDelete(id, now, now)
                withContext(Dispatchers.IO) { media.delete(id) }
            }
        }
    }
}

internal val localMediaId = Regex("[A-Za-z0-9_-]{1,80}")
