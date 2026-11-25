package com.twohearts.app.ui.screens.places

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.services.media.PlacePhotoSelection
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
import com.twohearts.app.ui.screens.memories.MemoryMediaSource
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePlace(
    placeId: String? = null,
    initialName: String = "",
    initialAddress: String = "",
    initialCity: String = "",
    initialNotes: String = "",
    initialCategory: String = "general",
    onSave: (name: String, address: String?, city: String?, notes: String?, category: String, photo: PlacePhotoSelection) -> Unit,
    onBack: () -> Unit,
    initialMedia: MediaAsset? = null,
    hasSavedPhoto: Boolean = initialMedia != null,
    saving: Boolean = false,
    errorMessage: String? = null,
) {
    var name by rememberSaveable(placeId) { mutableStateOf(initialName) }
    var address by rememberSaveable(placeId) { mutableStateOf(initialAddress) }
    var city by rememberSaveable(placeId) { mutableStateOf(initialCity) }
    var notes by rememberSaveable(placeId) { mutableStateOf(initialNotes) }
    var category by rememberSaveable(placeId) { mutableStateOf(initialCategory) }
    var choosing by rememberSaveable(placeId) { mutableStateOf(false) }
    var photoUri by rememberSaveable(placeId) { mutableStateOf<Uri?>(null) }
    var removePhoto by rememberSaveable(placeId) { mutableStateOf(false) }
    var attempted by rememberSaveable(placeId) { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri
    }
    val selectPhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val uri = photoUri
    val source = when {
        uri != null -> MemoryMediaSource(uri = uri, mimeType = "image/*")
        !removePhoto && initialMedia != null -> MemoryMediaSource(asset = initialMedia)
        else -> null
    }
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing

    RecordEditorLayout(
        title = if (placeId == null) "New place" else "Edit place", saveText = "Save place",
        onSave = {
            attempted = true
            if (!saving && name.isNotBlank()) {
                val photo = when {
                    uri != null -> PlacePhotoSelection.Pick(uri)
                    removePhoto -> PlacePhotoSelection.Remove
                    else -> PlacePhotoSelection.Keep
                }
                onSave(
                    name.trim(), address.trim().ifEmpty { null }, city.trim().ifEmpty { null },
                    notes.trim().ifEmpty { null }, category, photo,
                )
            }
        },
        onBack = onBack, saving = saving, errorMessage = errorMessage,
    ) {
        ThInput(
            name, { name = it }, label = "Name", placeholder = "What do you call this place?",
            enabled = !saving, error = "Give the place a name.".takeIf { attempted && name.isBlank() },
        )
        ThButton(
            onClick = { choosing = true }, text = "Category · ${placeCategoryLabel(category)}",
            icon = ThIcons.ChevronRight, variant = ButtonVariant.SECONDARY, enabled = !saving,
        )
        ThInput(city, { city = it }, label = "City (optional)", enabled = !saving)
        ThInput(address, { address = it }, label = "Address (optional)", enabled = !saving)
        ThInput(notes, { notes = it }, label = "Story (optional)", multiline = true, enabled = !saving)
        ThDivider()
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            ThEyebrow("Photo", color = colors.roseDeep)
            if (source != null) MemoryMediaFrame(source, Modifier.fillMaxWidth().aspectRatio(1.8f))
            val hasPhoto = source != null || (hasSavedPhoto && !removePhoto)
            if (source == null && hasPhoto) {
                Text("Saved photo is unavailable", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                ThButton(
                    selectPhoto, text = if (hasPhoto) "Change photo" else "Choose photo",
                    icon = ThIcons.Image, variant = ButtonVariant.SECONDARY, enabled = !saving,
                )
                if (hasPhoto) {
                    ThButton(
                        onClick = {
                            photoUri = null
                            if (uri == null) removePhoto = true
                        },
                        text = if (uri != null) "Discard selection" else "Remove photo",
                        variant = ButtonVariant.GHOST, enabled = !saving,
                    )
                } else if (hasSavedPhoto && removePhoto) {
                    ThButton(
                        onClick = { removePhoto = false },
                        text = "Keep saved photo", variant = ButtonVariant.GHOST, enabled = !saving,
                    )
                }
            }
        }
    }

    val key = placeCategoryKey(category)
    PlaceCategorySheet(
        choosing, key, (placeCategories + key).distinct(),
        onSelect = { selected -> if (selected != null && key != selected) category = selected },
        onClose = { choosing = false },
    )
}
