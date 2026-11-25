package com.twohearts.app.ui.screens.places

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Place
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
import com.twohearts.app.ui.screens.memories.MemoryMediaSource
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun PlacesHome(
    places: List<Place>,
    onPlaceClick: (String) -> Unit,
    onAddPlace: () -> Unit,
    onBack: () -> Unit,
    media: Map<String, MediaAsset> = emptyMap(),
) {
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var showCategories by rememberSaveable { mutableStateOf(false) }
    val categories = remember(places) { places.map { placeCategoryKey(it.category) }.distinct().sorted() }
    val active = category?.takeIf { it in categories }
    val visible = remember(places, active) { places.filter { active == null || placeCategoryKey(it.category) == active } }
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current

    Scaffold(
        topBar = { ThTopBar("Places", onBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                ThDivider()
                ThButton(
                    onClick = onAddPlace, text = "Save a place", icon = ThIcons.Add, full = true,
                    modifier = Modifier.padding(horizontal = spacing.screenGutter, vertical = spacing.space2),
                )
            }
        },
    ) { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space3),
        ) {
            if (places.isEmpty()) item {
                ThEmptyState(
                    title = "Places in your story",
                    description = "Keep the address, a photo, or a few words about why a place matters.",
                    visual = { Icon(ThIcons.MapPin, null, tint = colors.roseDeep) },
                )
            } else {
                item {
                    ThButton(
                        onClick = { showCategories = true }, variant = ButtonVariant.SECONDARY, icon = ThIcons.ChevronRight,
                        text = active?.let { "Category · ${placeCategoryLabel(it)}" } ?: "Category · All places",
                    )
                }
                itemsIndexed(visible, key = { _, place -> place.id }) { index, place ->
                    val photo = media[place.id]?.takeIf { it.mimeType.startsWith("image/") }
                    PlaceRow(place, photo, featured = index == 0 && photo != null) { onPlaceClick(place.id) }
                    ThDivider()
                }
            }
        }
    }
    PlaceCategorySheet(showCategories, active, listOf<String?>(null) + categories, { category = it }, { showCategories = false })
}

@Composable
private fun PlaceRow(place: Place, photo: MediaAsset?, featured: Boolean, onClick: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Column(
        Modifier.fillMaxWidth().clickable(onClickLabel = "Open place", onClick = onClick)
            .padding(vertical = spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        if (featured && photo != null) {
            MemoryMediaFrame(MemoryMediaSource(photo), Modifier.fillMaxWidth().aspectRatio(1.9f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.Top) {
            if (!featured) {
                if (photo != null) MemoryMediaFrame(MemoryMediaSource(photo), Modifier.size(64.dp))
                else Box(
                    Modifier.size(48.dp).background(colors.surfaceWarm, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(ThIcons.MapPin, null, tint = colors.roseDeep)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                ThEyebrow(placeCategoryLabel(place.category), color = colors.roseDeep)
                Text(place.name, style = if (featured) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge)
                placeLocation(place)?.let { Text(it, color = colors.textSecondary, style = MaterialTheme.typography.labelMedium) }
                place.notes?.takeIf(String::isNotBlank)?.let {
                    Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

internal fun placeLocation(place: Place): String? =
    listOfNotNull(place.city, place.state, place.country).filter(String::isNotBlank).joinToString(", ").ifBlank { null }
