package com.twohearts.app.services.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

/** Small, process-wide cache for rendered previews. Original media remains untouched. */
internal object MediaThumbnails {
    private data class Key(val path: String, val revision: String, val maxEdge: Int, val normalizeExif: Boolean)

    private val maxCacheBytes = (Runtime.getRuntime().maxMemory() / 24)
        .coerceIn(3L * 1024 * 1024, 16L * 1024 * 1024).toInt()
    private val previews = object : LruCache<Key, Bitmap>(maxCacheBytes) {
        override fun sizeOf(key: Key, bitmap: Bitmap): Int = bitmap.allocationByteCount
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val decoder = Dispatchers.IO.limitedParallelism(2)

    fun cached(path: String, revision: String, maxEdge: Int, normalizeExif: Boolean = false): Bitmap? =
        previews.get(Key(path, revision, maxEdge, normalizeExif))

    suspend fun load(path: String, revision: String, maxEdge: Int, normalizeExif: Boolean = false): Bitmap? {
        val key = Key(path, revision, maxEdge, normalizeExif)
        return previews.get(key) ?: withContext(decoder) {
            previews.get(key) ?: (
                if (normalizeExif) decodeProfileImage(File(path), maxEdge) else decode(path, maxEdge)
            )?.also { previews.put(key, it) }
        }
    }

    private fun decode(path: String, maxEdge: Int): Bitmap? {
        if (!File(path).isFile) return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        options.inSampleSize = sampleSize(options.outWidth, options.outHeight, maxEdge)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        require(maxEdge > 1)
        val sourceEdge = maxOf(width, height).toLong()
        var sample = 1
        while ((sourceEdge + sample - 1) / sample > maxEdge) sample *= 2
        return sample
    }

    fun bucket(width: Int, height: Int): Int {
        val edge = maxOf(width, height)
        return when {
            edge <= 512 -> 512
            edge <= 1024 -> 1024
            else -> 1536
        }
    }
}
