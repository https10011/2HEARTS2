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
    /** [edit] writes a profile and returns the previous photo reference if it was detached. */
    suspend fun commit(change: ProfilePhotoChange, edit: suspend (replacementId: String?) -> String?) {
        commit(listOf(change)) { replacements -> listOfNotNull(edit(replacements.single())) }
    }

    /** Copies each selected draft and supplies its replacement ID in the same order. */
    suspend fun commit(changes: List<ProfilePhotoChange>, edit: suspend (List<String?>) -> List<String>) {
        val assets = database.mediaAssetDao()
        val copied = mutableListOf<MediaAssetInfo>()
        var detached = emptyList<String>()
        try {
            database.withTransaction {
                val replacements = changes.map { change ->
                    if (change !is ProfilePhotoChange.Replace) return@map null
                    val cropped = staging.croppedUri(change.draftId) ?: throw IOException("Photo draft is missing")
                    val replacement = withContext(Dispatchers.IO) {
                        media.store(cropped, "image/jpeg")?.also { copied += it }
                    } ?: throw IOException("Could not store photo")
                    val width = replacement.width
                    if (width == null || width !in 1..1024 || replacement.height != width) {
                        throw IOException("Photo draft must be a bounded square")
                    }
                    val now = DateTimeHelper.nowUtc()
                    assets.insert(MediaAsset(
                        filePath = replacement.filePath, mimeType = replacement.mimeType,
                        fileSize = replacement.fileSize, width = width, height = replacement.height,
                        id = replacement.id, createdAt = now, updatedAt = now,
                    ))
                    replacement.id
                }
                detached = edit(replacements)
            }
        } finally {
            // Cancellation can arrive after SQLite commits. A reference check
            // also protects a photo shared by the second profile or a record.
            withContext(NonCancellable) {
                for (id in (copied.map { it.id } + detached).distinct()) {
                    runCatching { removeIfUnreferenced(id) }
                }
            }
        }
        changes.filterIsInstance<ProfilePhotoChange.Replace>().forEach { staging.discard(it.draftId) }
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
