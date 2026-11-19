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
import kotlinx.coroutines.withContext

internal class VaultStorage(
    private val context: Context,
    private val database: TwoHeartsDatabase,
    private val mediaStorage: MediaStorage,
    private val items: VaultItemRepository,
) {
    suspend fun add(title: String, type: String, payload: String?) = withContext(Dispatchers.IO) {
        require(title.isNotBlank() && !payload.isNullOrBlank())
        val timestamp = nowUtc()
        val stored = if (type == "note") null else {
            val uri = Uri.parse(payload)
            require(uri.scheme == "content")
            val mime = context.contentResolver.getType(uri)
            when (type) {
                "photo" -> checkNotNull(mime?.takeIf { it.startsWith("image/") }?.let { mediaStorage.store(uri, it) })
                "video" -> checkNotNull(mime?.takeIf { it.startsWith("video/") }?.let { mediaStorage.store(uri, it) })
                "file" -> checkNotNull(mediaStorage.storeDocument(uri, mime ?: "application/octet-stream"))
                else -> error("Unsupported vault type")
            }
        }
        val record = VaultItem(
            title = title.trim(), contentType = type, contentText = payload.takeIf { type == "note" },
            mediaRef = stored?.id, id = generateId(), createdAt = timestamp, updatedAt = timestamp,
        )
        database.withTransaction {
            if (stored != null) {
                database.mediaAssetDao().insert(MediaAsset(
                    stored.filePath, stored.mimeType, stored.fileSize, stored.width, stored.height,
                    id = stored.id, createdAt = timestamp, updatedAt = timestamp,
                ))
            }
            items.create(record)
        }
    }

    suspend fun export(asset: MediaAsset, destination: Uri) = withContext(Dispatchers.IO) {
        val source = File(asset.filePath)
        val mediaRoot = File(context.filesDir, "media").canonicalFile
        check(source.canonicalFile.parentFile == mediaRoot && source.isFile)
        checkNotNull(context.contentResolver.openOutputStream(destination, "wt")).use { output ->
            source.inputStream().use { it.copyTo(output) }
        }
    }
}
