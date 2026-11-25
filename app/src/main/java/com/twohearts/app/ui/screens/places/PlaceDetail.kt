package com.twohearts.app.ui.screens.places

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Place
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.screens.memories.MemoryAttachmentPreview
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
import com.twohearts.app.ui.screens.memories.MemoryMediaSource
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun PlaceDetail(
    place: Place,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    media: MediaAsset? = null,
    deleting: Boolean = false,
    errorMessage: String? = null,
) {
    var confirm by rememberSaveable(place.id) { mutableStateOf(false) }
    var showPhoto by rememberSaveable(place.id) { mutableStateOf(false) }
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    val source = media?.let { MemoryMediaSource(it) }
    val address = listOfNotNull(place.address, placeLocation(place)).filter(String::isNotBlank).joinToString("\n")

    Scaffold(topBar = {
        ThTopBar("Place", onBack = onBack, actions = {
            ThIconButton(onEdit, label = "Edit place", enabled = !deleting) { Icon(ThIcons.Edit, null) }
        })
    }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
                verticalArrangement = Arrangement.spacedBy(spacing.space4),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ThEyebrow(placeCategoryLabel(place.category), color = colors.roseDeep)
                    Text(place.name, style = MaterialTheme.typography.displayMedium, modifier = Modifier.semantics { heading() })
                    if (address.isNotEmpty()) Text(address, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
                if (source != null) {
                    MemoryMediaFrame(
                        source,
                        Modifier.fillMaxWidth().aspectRatio(1.65f).clickable(onClickLabel = "View place photo") { showPhoto = true },
                    )
                } else if (place.photoRef != null) {
                    ThStatusBanner(StatusVariant.INFO, "Saved photo is unavailable. You can replace it in Edit place.")
                }
                place.notes?.takeIf(String::isNotBlank)?.let { Text(it, style = ThTextStyles.reading, color = colors.textSecondary) }
                ThDivider()
                Text(
                    "Saved ${DateTimeHelper.formatDisplay(place.createdAt)}",
                    style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
                )
                if (place.updatedAt != place.createdAt) {
                    Text(
                        "Updated ${DateTimeHelper.formatDisplay(place.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
                    )
                }
                errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
                ThButton(
                    onClick = { confirm = true }, text = "Delete place", variant = ButtonVariant.DANGER,
                    icon = ThIcons.Trash, loading = deleting,
                )
            }
        }
    }
    if (showPhoto && source != null) MemoryAttachmentPreview(source) { showPhoto = false }
    if (confirm) ConfirmDialog(
        title = "Delete this place?", message = "It will be removed from your saved places.", confirmText = "Delete",
        onConfirm = { confirm = false; onDelete() }, onDismiss = { confirm = false },
    )
}
