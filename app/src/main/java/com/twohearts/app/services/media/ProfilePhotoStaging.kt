package com.twohearts.app.services.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

sealed interface ProfilePhotoChange {
    data object Keep : ProfilePhotoChange
    data object Remove : ProfilePhotoChange
    data class Replace(val draftId: String) : ProfilePhotoChange

    fun reference(current: String?, replacement: String?): String? = when (this) {
        Keep -> current
        Remove -> null
        is Replace -> requireNotNull(replacement)
    }
}

internal data class AvatarCrop(val zoom: Float = 1f, val horizontal: Float = .5f, val vertical: Float = .5f) {
    fun bounds(width: Int, height: Int): Rect {
        val edge = (minOf(width, height) / zoom.coerceIn(1f, 3f)).roundToInt().coerceAtLeast(1)
        val left = ((width - edge) * horizontal.coerceIn(0f, 1f)).roundToInt()
        val top = ((height - edge) * vertical.coerceIn(0f, 1f)).roundToInt()
        return Rect(left, top, left + edge, top + edge)
    }
}

/** Copies picker content to private cache so preview and save do not depend on a URI grant. */
internal class ProfilePhotoStaging(context: Context) {
    private val app = context.applicationContext
    private val directory = File(app.cacheDir, "profile-photos")

    suspend fun stage(uri: Uri): String = writeDraft("source") { draft ->
        val input = app.contentResolver.openInputStream(uri) ?: throw IOException("Could not open photo")
        val bytes = input.use { source ->
            draft.outputStream().use { target -> source.copyWithinLimit(target, MediaStorage.MAX_PHOTO_SIZE) }
        }
        if (bytes == 0L) throw IOException("Empty photo")
    }

    suspend fun preview(id: String): Bitmap? = withContext(Dispatchers.IO) {
        file(id)?.let { decodeProfileImage(it, maxEdge = 2048) }
    }

    suspend fun crop(source: Bitmap, framing: AvatarCrop): String = writeDraft("jpg") { draft ->
        val bounds = framing.bounds(source.width, source.height)
        val edge = minOf(bounds.width(), 1024)
        val avatar = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888)
        try {
            Canvas(avatar).apply {
                drawColor(Color.WHITE)
                drawBitmap(source, bounds, Rect(0, 0, edge, edge), Paint(Paint.FILTER_BITMAP_FLAG))
            }
            val encoded = draft.outputStream().use { avatar.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            if (!encoded) throw IOException("Could not encode avatar")
        } finally {
            avatar.recycle()
        }
    }

    fun croppedUri(id: String): Uri? = file(id)?.takeIf { it.extension == "jpg" && it.isFile }?.let(Uri::fromFile)

    suspend fun discard(id: String) = withContext(Dispatchers.IO) { file(id)?.delete() }

    private suspend fun writeDraft(extension: String, write: (File) -> Unit): String {
        var partial: File? = null
        try {
            return withContext(Dispatchers.IO) {
                if (!directory.isDirectory && !directory.mkdirs() && !directory.isDirectory) {
                    throw IOException("Could not create private photo staging")
                }
                val draft = File(directory, "${UUID.randomUUID()}.$extension")
                partial = draft
                write(draft)
                draft.name
            }
        } catch (failure: Exception) {
            withContext(NonCancellable + Dispatchers.IO) { partial?.delete() }
            throw failure
        }
    }

    private fun file(id: String): File? = id.takeIf(draftName::matches)?.let { File(directory, it) }

    private companion object {
        val draftName = Regex("[0-9a-f-]{36}\\.(source|jpg)")
    }
}

internal fun decodeProfileImage(file: File, maxEdge: Int): Bitmap? {
    if (!file.isFile) return null
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, options)
    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    options.inSampleSize = MediaThumbnails.sampleSize(options.outWidth, options.outHeight, maxEdge)
    options.inJustDecodeBounds = false
    val image = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
    val orientation = try {
        ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (_: IOException) {
        ExifInterface.ORIENTATION_NORMAL
    }
    val transform = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> transform.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> transform.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> transform.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            transform.setRotate(90f)
            transform.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> transform.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            transform.setRotate(-90f)
            transform.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> transform.setRotate(-90f)
        else -> return image
    }
    return Bitmap.createBitmap(image, 0, 0, image.width, image.height, transform, true).also {
        if (it !== image) image.recycle()
    }
}
