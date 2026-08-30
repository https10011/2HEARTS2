package com.twohearts.app.services.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
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
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileId = UUID.randomUUID().toString()
            val extension = getExtensionFromMimeType(mimeType)
            val fileName = "$fileId.$extension"
            val file = File(mediaDir, fileName)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            val fileSize = file.length()
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
            )
        } catch (e: Exception) {
            null
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
            val extensions = listOf("png", "jpeg", "mp4", "webm")
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
        val extensions = listOf("jpg", "jpeg", "png", "mp4", "webm")
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
    fun isValidFileSize(fileSize: Long, mimeType: String): Boolean {
        val maxSize = if (mimeType.startsWith("video/")) {
            MAX_VIDEO_SIZE
        } else {
            MAX_PHOTO_SIZE
        }
        return fileSize <= maxSize
    }

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
