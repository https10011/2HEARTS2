package com.twohearts.app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.MediaThumbnails
import com.twohearts.app.services.media.localMediaId
import kotlinx.coroutines.CancellationException

/**
 * ProfileAvatar — the shared avatar for the owner and partner.
 *
 * Renders, in priority order:
 *  1. the profile photo, when the URI resolves;
 *  2. the person's initial on a warm burgundy-to-rose gradient;
 *  3. a heart glyph when no name is known yet.
 *
 * Phase 1: the empty state used to render a "☺" text emoji, which fell
 * outside the app's icon system and rendered inconsistently across OEM
 * fonts (Tecno/Transsion devices in particular). It now uses a vector icon
 * from the central set. The gradient was also flattened slightly and the
 * initial re-weighted so small avatars (28–32dp in list rows) stay legible.
 */
@Composable
fun ProfileAvatar(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Int = 64,
    label: String? = null,
    selectedPhoto: Uri? = null,
    shape: Shape = CircleShape,
) {
    val thColors = LocalTwoHeartsColors.current
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
    val context = LocalContext.current.applicationContext
    val desiredEdge = with(LocalDensity.current) { (size.dp * 2).roundToPx() }.coerceAtMost(1024)
    val maxEdge = MediaThumbnails.bucket(desiredEdge, desiredEdge)
    val path = remember(context, photoUrl, selectedPhoto) {
        val uri = selectedPhoto ?: photoUrl?.takeIf(localMediaId::matches)?.let { MediaStorage(context).resolveUrl(it) }
        uri?.takeIf { it.scheme == "file" }?.path
    }
    val bitmap by key(path, maxEdge) {
        val cached = path?.let { MediaThumbnails.cached(it, "", maxEdge, normalizeExif = true) }
        produceState<Bitmap?>(cached) {
            if (path != null && value == null) {
                value = try {
                    MediaThumbnails.load(path, "", maxEdge, normalizeExif = true)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .semantics { if (label != null) contentDescription = label }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(TwoHeartsTokens.Color.burgundy600, TwoHeartsTokens.Color.burgundy400)
                )
            )
            .border(
                TwoHeartsTokens.Border.hairline,
                thColors.textOnAccent.copy(alpha = 0.22f),
                shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val photo = bitmap
        if (photo != null) {
            Image(photo.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (initial.isNotEmpty()) {
            Text(
                text = initial,
                fontSize = (size * 0.42).sp,
                fontWeight = FontWeight.SemiBold,
                color = thColors.textOnAccent,
            )
        } else {
            Icon(
                imageVector = ThIcons.Person,
                contentDescription = null,
                tint = thColors.textOnAccent.copy(alpha = 0.9f),
                modifier = Modifier.size((size * 0.46).dp),
            )
        }
    }
}
