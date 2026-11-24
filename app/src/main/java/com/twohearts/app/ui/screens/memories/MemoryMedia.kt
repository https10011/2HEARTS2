package com.twohearts.app.ui.screens.memories

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.VideoPreviewDialog
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal data class MemoryMediaSource(
    val asset: MediaAsset? = null,
    val uri: Uri? = null,
    val mimeType: String? = asset?.mimeType,
) {
    val isPhoto get() = mimeType?.startsWith("image/") == true
    val isVideo get() = mimeType?.startsWith("video/") == true
}

@Composable
fun MemoryMediaFrame(asset: MediaAsset?, modifier: Modifier = Modifier) {
    MemoryMediaFrame(MemoryMediaSource(asset), modifier, legacyPresentation = true)
}

@Composable
internal fun MemoryMediaFrame(
    source: MemoryMediaSource,
    modifier: Modifier,
    fit: Boolean = false,
    legacyPresentation: Boolean = false,
) {
    val colors = LocalTwoHeartsColors.current
    val resolver = LocalContext.current.contentResolver
    val corner = if (legacyPresentation) 20.dp else TwoHeartsTokens.Radius.md
    BoxWithConstraints(
        modifier = modifier.clip(RoundedCornerShape(corner)).background(colors.surfaceWarm),
        contentAlignment = Alignment.Center,
    ) {
        val edge = with(LocalDensity.current) {
            MediaThumbnails.bucket(maxWidth.roundToPx(), maxHeight.roundToPx())
        }
        val asset = source.asset
        val uri = source.uri
        val preview by key(source, edge) {
            val cached = asset?.takeIf { source.isPhoto }?.let {
                MediaThumbnails.cached(it.filePath, it.updatedAt, edge, normalizeExif = !legacyPresentation)
            }
            produceState(ImagePreview(cached, source.isPhoto && cached == null)) {
                if (!value.loading) return@produceState
                val loaded = try {
                    when {
                        asset != null -> MediaThumbnails.load(asset.filePath, asset.updatedAt, edge, normalizeExif = !legacyPresentation)
                        uri != null -> withContext(Dispatchers.IO) { decodePickedImage(resolver, uri, edge) }
                        else -> null
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }
                value = ImagePreview(loaded, false)
            }
        }
        val bitmap = preview.bitmap
        if (bitmap != null) {
            Image(
                bitmap.asImageBitmap(), contentDescription = if (fit) "Memory photo" else if (legacyPresentation) "Saved photo" else null,
                modifier = Modifier.fillMaxSize(), contentScale = if (fit) ContentScale.Fit else ContentScale.Crop,
            )
        } else if (fit) {
            Text(
                if (preview.loading) "Opening photo…" else "Preview unavailable",
                style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary,
                modifier = Modifier.padding(12.dp),
            )
        } else {
            Icon(
                if (source.isVideo) ThIcons.Play else ThIcons.Image,
                contentDescription = null,
                tint = if (legacyPresentation && source.asset == null) colors.roseMuted else colors.burgundy,
                modifier = Modifier.size(if (legacyPresentation) 52.dp else 32.dp),
            )
            if (source.isVideo && legacyPresentation) {
                Text("Video", Modifier.align(Alignment.BottomStart).padding(14.dp),
                    style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
        }
    }
}

@Composable
internal fun MemoryAttachmentPreview(source: MemoryMediaSource, onClose: () -> Unit) {
    val video = source.uri ?: source.asset?.let { Uri.fromFile(File(it.filePath)) }
    if (source.isVideo && video != null) {
        VideoPreviewDialog(video, onClose)
        return
    }
    ThModal(open = true, onClose = onClose, label = "Photo preview", modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Photo preview", style = MaterialTheme.typography.titleLarge)
        MemoryMediaFrame(source, Modifier.padding(vertical = 12.dp).fillMaxWidth().height(220.dp), fit = true)
        ThButton(onClose, variant = ButtonVariant.GHOST, full = true, text = "Close")
    }
}

private data class ImagePreview(val bitmap: Bitmap?, val loading: Boolean)

internal fun decodePickedImage(resolver: ContentResolver, uri: Uri, edge: Int): Bitmap? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    options.inSampleSize = MediaThumbnails.sampleSize(options.outWidth, options.outHeight, edge)
    options.inJustDecodeBounds = false
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}
