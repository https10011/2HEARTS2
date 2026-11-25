package com.twohearts.app.services.media

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

sealed interface PlacePhotoSelection {
    data object Keep : PlacePhotoSelection
    data object Remove : PlacePhotoSelection
    data class Pick(val uri: Uri) : PlacePhotoSelection
}

internal class PlaceWriter(
    private val database: TwoHeartsDatabase,
    private val places: PlaceRepository,
    private val storage: MediaStorage,
    private val resolver: ContentResolver,
) {
    suspend fun save(place: Place, original: Place?, photo: PlacePhotoSelection) {
        var copied: MediaAssetInfo? = null
        var detached: String? = null
        try {
            // Serialize the copy and reference write with the orphan-media sweep.
            database.withTransaction {
                check(places.getById(place.id) == original) { "Place changed while it was open" }
                val photoRef = when (photo) {
                    PlacePhotoSelection.Keep -> original?.photoRef
                    PlacePhotoSelection.Remove -> null
                    is PlacePhotoSelection.Pick -> withContext(Dispatchers.IO) {
                        val type = resolver.getType(photo.uri)?.takeIf { it.startsWith("image/") }
                            ?: throw IOException("The selected file is not a photo")
                        val stored = storage.store(photo.uri, type) ?: throw IOException("The photo could not be read")
                        copied = stored
                        if (stored.fileSize <= 0 || stored.width == null || stored.height == null) {
                            throw IOException("The photo could not be decoded")
                        }
                        stored.id
                    }
                }
                copied?.let { stored ->
                    val now = DateTimeHelper.nowUtc()
                    database.mediaAssetDao().insert(MediaAsset(
                        filePath = stored.filePath, mimeType = stored.mimeType, fileSize = stored.fileSize,
                        width = stored.width, height = stored.height, id = stored.id, createdAt = now, updatedAt = now,
                    ))
                }
                if (original == null) {
                    places.create(place.copy(photoRef = photoRef))
                } else {
                    checkNotNull(places.update(place.id, mapOf(
                        "name" to place.name, "address" to place.address, "city" to place.city,
                        "notes" to place.notes, "category" to place.category, "photoRef" to photoRef,
                    )))
                    detached = original.photoRef?.takeUnless { it == photoRef }
                }
            }
        } finally {
            withContext(NonCancellable) {
                listOfNotNull(copied?.id, detached).distinct().forEach { id ->
                    runCatching { deleteUnreferencedMedia(database, storage, id) }
                }
            }
        }
    }
}
