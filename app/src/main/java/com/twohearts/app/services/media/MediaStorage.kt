package com.twohearts.app.services.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * MediaStorage — media file lifecycle management.
 *
 * Matches legacy MediaStorage with:
 * - Store media files (photos, videos)
 * - Resolve media URLs for display
 * - Delete media files
 * - Media validation (MIME, size limits)
 */
class MediaStorage(private val context: Context) {

    private val mediaDir: File
        get() {
            val dir = File(context.filesDir, "media")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    /**
     * Store a media file and return the media asset ID.
     */
    fun store(uri: Uri, mimeType: String): MediaAssetInfo? {
        if (!isValidMimeType(mimeType)) return null
        return storeCopy(uri, mimeType)
    }

    internal fun storeDocument(uri: Uri, mimeType: String): MediaAssetInfo? = storeCopy(uri, mimeType)

    private fun storeCopy(uri: Uri, mimeType: String): MediaAssetInfo? {
        var unfinished: File? = null
        return try {
            val declaredSize = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            if (declaredSize != null && declaredSize >= 0 && !isValidFileSize(declaredSize, mimeType)) return null
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileId = UUID.randomUUID().toString()
            val extension = getExtensionFromMimeType(mimeType)
            val fileName = "$fileId.$extension"
            val file = File(mediaDir, fileName)
            unfinished = file

            val fileSize = inputStream.use { input ->
                FileOutputStream(file).use { output -> input.copyWithinLimit(output, maxSize(mimeType)) }
            }
            val dimensions = if (mimeType.startsWith("image/")) {
                getImageDimensions(file)
            } else {
                null
            }

            MediaAssetInfo(
                id = fileId,
                filePath = file.absolutePath,
                mimeType = mimeType,
                fileSize = fileSize,
                width = dimensions?.first,
                height = dimensions?.second
            ).also { unfinished = null }
        } catch (e: Exception) {
            null
        } finally {
            unfinished?.delete()
        }
    }

    /**
     * Store a bitmap as JPEG.
     */
    fun storeBitmap(bitmap: Bitmap, quality: Int = 85): MediaAssetInfo? {
        return try {
            val fileId = UUID.randomUUID().toString()
            val fileName = "$fileId.jpg"
            val file = File(mediaDir, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            MediaAssetInfo(
                id = fileId,
                filePath = file.absolutePath,
                mimeType = "image/jpeg",
                fileSize = file.length(),
                width = bitmap.width,
                height = bitmap.height
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resolve a media file URL for display.
     */
    fun resolveUrl(mediaRef: String): Uri? {
        val file = File(mediaDir, "$mediaRef.jpg")
        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            // Try other extensions
            val extensions = listOf("png", "jpeg", "gif", "webp", "mp4", "webm")
            for (ext in extensions) {
                val extFile = File(mediaDir, "$mediaRef.$ext")
                if (extFile.exists()) {
                    return Uri.fromFile(extFile)
                }
            }
            null
        }
    }

    /**
     * Delete a media file.
     */
    fun delete(mediaId: String): Boolean {
        val extensions = listOf("jpg", "jpeg", "png", "gif", "webp", "mp4", "webm", "bin")
        for (ext in extensions) {
            val file = File(mediaDir, "$mediaId.$ext")
            if (file.exists()) {
                return file.delete()
            }
        }
        return false
    }

    /**
     * Get all media file IDs.
     */
    fun getAllMediaIds(): Set<String> {
        return mediaDir.listFiles()?.map { it.nameWithoutExtension }?.toSet() ?: emptySet()
    }

    /**
     * Get total media size in bytes.
     */
    fun getTotalMediaSize(): Long {
        return mediaDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Clean up orphan media files.
     */
    fun cleanupOrphans(knownIds: Set<String>): Int {
        var cleaned = 0
        mediaDir.listFiles()?.forEach { file ->
            val fileId = file.nameWithoutExtension
            if (fileId !in knownIds) {
                if (file.delete()) {
                    cleaned++
                }
            }
        }
        return cleaned
    }

    /**
     * Validate MIME type.
     */
    fun isValidMimeType(mimeType: String): Boolean {
        val allowedTypes = setOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "video/mp4",
            "video/webm"
        )
        return mimeType in allowedTypes
    }

    /**
     * Validate file size.
     */
    fun isValidFileSize(fileSize: Long, mimeType: String): Boolean = fileSize <= maxSize(mimeType)

    private fun maxSize(mimeType: String) = if (mimeType.startsWith("video/")) MAX_VIDEO_SIZE else MAX_PHOTO_SIZE

    /**
     * Get extension from MIME type.
     */
    private fun getExtensionFromMimeType(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            else -> "bin"
        }
    }

    /**
     * Get image dimensions from file.
     */
    private fun getImageDimensions(file: File): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val MAX_PHOTO_SIZE = 25 * 1024 * 1024L // 25MB
        const val MAX_VIDEO_SIZE = 500 * 1024 * 1024L // 500MB
    }
}

internal fun InputStream.copyWithinLimit(output: OutputStream, limit: Long): Long {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val count = read(buffer)
        if (count < 0) return total
        if (count > limit - total) throw IOException("Media exceeds the size limit")
        output.write(buffer, 0, count)
        total += count
    }
}

/**
 * Media asset info data class.
 */
data class MediaAssetInfo(
    val id: String,
    val filePath: String,
    val mimeType: String,
    val fileSize: Long,
    val width: Int? = null,
    val height: Int? = null
)
