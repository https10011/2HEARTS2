package com.twohearts.app.services.media

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.data.repository.VaultItemRepository
import com.twohearts.app.data.repository.generateId
import com.twohearts.app.data.repository.nowUtc
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal class VaultStorage(
    private val context: Context,
    private val database: TwoHeartsDatabase,
    private val mediaStorage: MediaStorage,
    private val items: VaultItemRepository,
) {
    suspend fun add(title: String, type: String, payload: String?): VaultItem {
        require(title.isNotBlank() && !payload.isNullOrBlank())
        val timestamp = nowUtc()
        var copied: MediaAssetInfo? = null
        return try {
            // The same transaction also excludes the orphan sweep while copying.
            database.withTransaction {
                val file = if (type == "note") null else withContext(Dispatchers.IO) {
                    copy(Uri.parse(payload), type).also { copied = it }
                }
                if (file != null) {
                    check(file.fileSize > 0)
                    if (type == "photo") check(file.width != null && file.height != null)
                    database.mediaAssetDao().insert(MediaAsset(
                        file.filePath, file.mimeType, file.fileSize, file.width, file.height,
                        id = file.id, createdAt = timestamp, updatedAt = timestamp,
                    ))
                }
                val record = VaultItem(
                    title = title.trim(), contentType = type, contentText = payload.takeIf { type == "note" },
                    mediaRef = file?.id, id = generateId(), createdAt = timestamp, updatedAt = timestamp,
                )
                items.create(record)
            }
        } finally {
            clean(copied?.id)
        }
    }

    suspend fun delete(id: String) {
        var detached: String? = null
        try {
            database.withTransaction {
                val item = items.getById(id) ?: return@withTransaction
                detached = item.mediaRef
                items.softDelete(id)
            }
        } finally {
            clean(detached)
        }
    }

    suspend fun export(asset: MediaAsset, destination: Uri) = withContext(Dispatchers.IO) {
        require(destination.scheme == "content")
        val source = checkNotNull(privateFile(asset))
        checkNotNull(context.contentResolver.openOutputStream(destination, "wt")).use { output ->
            source.inputStream().use { it.copyTo(output) }
        }
    }

    suspend fun availableAsset(ref: String): MediaAsset? = withContext(Dispatchers.IO) {
        database.mediaAssetDao().getById(ref)?.takeIf { privateFile(it) != null }
    }

    private fun privateFile(asset: MediaAsset): File? {
        val mediaRoot = File(context.filesDir, "media").canonicalFile
        return File(asset.filePath).canonicalFile.takeIf { it.parentFile == mediaRoot && it.isFile }
    }

    private fun copy(uri: Uri, type: String): MediaAssetInfo {
        require(uri.scheme == "content")
        val mime = context.contentResolver.getType(uri)
        return checkNotNull(when (type) {
            "photo" -> mime?.takeIf { it.startsWith("image/") }?.let { mediaStorage.store(uri, it) }
            "video" -> mime?.takeIf { it.startsWith("video/") }?.let { mediaStorage.store(uri, it) }
            "file" -> mediaStorage.storeDocument(uri, mime ?: "application/octet-stream")
            else -> error("Unsupported vault type")
        })
    }

    private suspend fun clean(id: String?) {
        if (id == null) return
        withContext(NonCancellable) { runCatching { deleteUnreferencedMedia(database, mediaStorage, id) } }
    }
}
