package com.twohearts.app.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.media.AvatarCrop
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.services.media.ProfilePhotoStaging
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal val ProfilePhotoChangeSaver = Saver<ProfilePhotoChange, String>(
    save = {
        when (it) {
            ProfilePhotoChange.Keep -> "keep"
            ProfilePhotoChange.Remove -> "remove"
            is ProfilePhotoChange.Replace -> it.draftId
        }
    },
    restore = {
        when (it) {
            "keep" -> ProfilePhotoChange.Keep
            "remove" -> ProfilePhotoChange.Remove
            else -> ProfilePhotoChange.Replace(it)
        }
    },
)

@Composable
internal fun profilePhotoBackAction(change: ProfilePhotoChange, saving: Boolean, onBack: () -> Unit): () -> Unit {
    val id = (change as? ProfilePhotoChange.Replace)?.draftId
    val context = LocalContext.current.applicationContext
    val staging = remember(context) { ProfilePhotoStaging(context) }
    val scope = rememberCoroutineScope()
    var leaving by remember { mutableStateOf(false) }
    val back: () -> Unit = {
        if (!saving && !leaving) {
            if (id == null) {
                onBack()
            } else {
                leaving = true
                scope.launch {
                    try {
                        staging.discard(id)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: SecurityException) {
                        // Android can still reclaim this private cache entry.
                    }
                    onBack()
                }
            }
        }
    }
    BackHandler(enabled = id != null && !saving, onBack = back)
    return back
}

@Composable
internal fun ProfilePhotoEditor(
    name: String,
    savedRef: String?,
    label: String,
    current: ProfilePhotoChange,
    onChange: (ProfilePhotoChange) -> Unit,
    enabled: Boolean,
) {
    val context = LocalContext.current.applicationContext
    val staging = remember(context) { ProfilePhotoStaging(context) }
    val scope = rememberCoroutineScope()
    var pendingId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingImage by remember { mutableStateOf<Bitmap?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val staged = (current as? ProfilePhotoChange.Replace)?.draftId

    fun select(change: ProfilePhotoChange) {
        onChange(change)
        if (staged != null && staged != (change as? ProfilePhotoChange.Replace)?.draftId) {
            scope.launch { staging.discard(staged) }
        }
    }

    fun dismissCrop() {
        val discarded = pendingId
        pendingId = null
        pendingImage = null
        if (discarded != null) scope.launch { staging.discard(discarded) }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && !busy && pendingId == null) {
            busy = true
            error = null
            scope.launch {
                try {
                    pendingId = staging.stage(uri)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    error = "Couldn’t open that photo. Choose another image."
                } finally {
                    busy = false
                }
            }
        }
    }

    LaunchedEffect(pendingId) {
        val id = pendingId ?: return@LaunchedEffect
        pendingImage = try {
            staging.preview(id)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
        if (pendingImage == null) {
            error = "Couldn’t read that photo. Choose another image."
            dismissCrop()
        }
    }

    val displayedRef = savedRef.takeIf { current == ProfilePhotoChange.Keep }
    val displayedDraft = staged?.let(staging::croppedUri)
    val hasPhoto = displayedRef != null || displayedDraft != null

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileAvatar(name, displayedRef, size = 72, label = label, selectedPhoto = displayedDraft)
            Column(Modifier.weight(1f)) {
                ThButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = enabled && !busy,
                    variant = ButtonVariant.SECONDARY,
                    text = if (hasPhoto) "Change photo" else "Choose photo",
                )
                if (hasPhoto) {
                    ThQuietButton({ select(ProfilePhotoChange.Remove) }, "Remove photo", enabled = enabled && !busy)
                }
                if (current != ProfilePhotoChange.Keep && savedRef != null) {
                    ThQuietButton({ select(ProfilePhotoChange.Keep) }, "Keep saved photo", enabled = enabled && !busy)
                }
            }
        }
        val message = error ?: if (staged != null && displayedDraft == null) {
            "The selected photo is no longer available. Choose it again or keep the saved photo."
        } else null
        if (message != null) ThStatusBanner(StatusVariant.ERROR, message)
    }

    if (busy || pendingId != null) {
        ProfilePhotoCropSheet(
            image = pendingImage,
            busy = busy,
            onDismiss = ::dismissCrop,
            onApply = { image, crop ->
                busy = true
                scope.launch {
                    try {
                        val result = staging.crop(image, crop)
                        select(ProfilePhotoChange.Replace(result))
                        dismissCrop()
                        error = null
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        error = "Couldn’t prepare this crop. Try again."
                        dismissCrop()
                    } finally {
                        busy = false
                    }
                }
            },
        )
    }
}

@Composable
internal fun ProfilePhotoCropSheet(
    image: Bitmap?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onApply: (Bitmap, AvatarCrop) -> Unit,
) {
    ThModal(open = true, onClose = { if (!busy) onDismiss() }, label = "Crop profile photo") {
        val height = LocalConfiguration.current.screenHeightDp.dp * 0.85f
        Column(
            Modifier.fillMaxWidth().heightIn(max = height)
                .windowInsetsPadding(WindowInsets.navigationBars).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (image == null) {
                ThLoadingState(modifier = Modifier.fillMaxWidth())
            } else {
                ProfilePhotoCropContent(image, busy, onDismiss, onApply = { crop -> onApply(image, crop) })
            }
        }
    }
}

@Composable
private fun ProfilePhotoCropContent(
    image: Bitmap,
    saving: Boolean,
    onCancel: () -> Unit,
    onApply: (AvatarCrop) -> Unit,
) {
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var horizontal by rememberSaveable { mutableFloatStateOf(.5f) }
    var vertical by rememberSaveable { mutableFloatStateOf(.5f) }
    val crop = AvatarCrop(zoom, horizontal, vertical)
    val bounds = crop.bounds(image.width, image.height)
    val preview = remember(image) { image.asImageBitmap() }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Crop profile photo", style = MaterialTheme.typography.headlineSmall)
        Canvas(
            Modifier.align(Alignment.CenterHorizontally).size(148.dp).clip(CircleShape)
                .background(Color.White)
                .semantics { contentDescription = "Photo crop preview" },
        ) {
            drawImage(
                image = preview,
                srcOffset = IntOffset(bounds.left, bounds.top),
                srcSize = IntSize(bounds.width(), bounds.height()),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            )
        }
        CropSlider("Zoom", zoom, 1f..3f, !saving) { zoom = it }
        if (bounds.width() < image.width) {
            CropSlider("Horizontal position", horizontal, 0f..1f, !saving) { horizontal = it }
        }
        if (bounds.height() < image.height) {
            CropSlider("Vertical position", vertical, 0f..1f, !saving) { vertical = it }
        }
        ThButton(onClick = { onApply(crop) }, text = "Use this crop", full = true, loading = saving)
        ThQuietButton(onCancel, "Cancel", enabled = !saving)
    }
}

@Composable
private fun CropSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, enabled: Boolean, change: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Slider(value, change, valueRange = range, enabled = enabled, modifier = Modifier.semantics { contentDescription = label })
    }
}
