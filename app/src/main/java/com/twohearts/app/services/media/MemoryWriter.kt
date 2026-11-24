package com.twohearts.app.services.media

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.IOException

sealed interface MemoryAttachmentChoice {
    data class Saved(val id: String) : MemoryAttachmentChoice
    data class Picked(val uri: Uri) : MemoryAttachmentChoice
}

/** Saves the record and its ordered links together, including edits to existing links. */
internal class MemoryWriter(
    private val database: TwoHeartsDatabase,
    private val memories: MemoryRepository,
    private val storage: MediaStorage,
    private val resolver: ContentResolver,
) {
    suspend fun save(
        record: Memory,
        original: Memory?,
        originalAttachmentIds: List<String>,
        attachments: List<MemoryAttachmentChoice>,
    ) {
        val copied = mutableListOf<String>()
        val detached = mutableListOf<String>()
        try {
            database.withTransaction {
                val previous = memories.getAssets(record.id).map { it.id }
                check(memories.getById(record.id) == original && previous == originalAttachmentIds) {
                    "Memory changed while it was open"
                }
                val retained = attachments.filterIsInstance<MemoryAttachmentChoice.Saved>().map { it.id }
                check(retained.size == retained.distinct().size && previous.containsAll(retained))
                val ids = attachments.map { attachment ->
                    when (attachment) {
                        is MemoryAttachmentChoice.Saved -> attachment.id
                        is MemoryAttachmentChoice.Picked -> import(attachment.uri, copied)
                    }
                }
                if (original == null) memories.create(record)
                else checkNotNull(memories.update(record.id, mapOf(
                    "title" to record.title, "caption" to record.caption, "memoryDate" to record.memoryDate,
                )))
                if (previous != ids) {
                    previous.forEach { memories.removeMedia(record.id, it) }
                    ids.forEachIndexed { index, id -> memories.addMedia(record.id, id, index) }
                    detached.addAll(previous - ids.toSet())
                }
            }
        } finally {
            clean(copied + detached)
        }
    }

    suspend fun delete(id: String) {
        val detached = mutableListOf<String>()
        try {
            database.withTransaction {
                detached.addAll(memories.getMedia(id).map { it.mediaAssetId })
                memories.softDelete(id)
            }
        } finally {
            clean(detached)
        }
    }

    private suspend fun import(uri: Uri, copied: MutableList<String>): String {
        val stored = withContext(Dispatchers.IO) {
            val type = resolver.getType(uri) ?: throw IOException("The selected attachment has no media type")
            storage.store(uri, type)?.also { copied += it.id }
                ?: throw IOException("Could not read the selected attachment")
        }
        if (stored.fileSize == 0L || (stored.mimeType.startsWith("image/") &&
                (stored.width == null || stored.height == null))) {
            throw IOException("The selected attachment is empty or unreadable")
        }
        val now = DateTimeHelper.nowUtc()
        database.mediaAssetDao().insert(MediaAsset(
            filePath = stored.filePath, mimeType = stored.mimeType, fileSize = stored.fileSize,
            width = stored.width, height = stored.height, id = stored.id, createdAt = now, updatedAt = now,
        ))
        return stored.id
    }

    private suspend fun clean(ids: List<String>) {
        withContext(NonCancellable) {
            ids.distinct().forEach { id ->
                runCatching { deleteUnreferencedMedia(database, storage, id) }
            }
        }
    }
}
