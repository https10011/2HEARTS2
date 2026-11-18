package com.twohearts.app.ui.screens.places

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import androidx.compose.ui.Alignment

/**
 * PlacesHome — places list screen.
 *
 * Matches legacy PlacesHome with:
 * - Hero band + grid + category chips + search
 * - Empty state
 * - Add place FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesHome(
    places: List<Place>,
    onPlaceClick: (String) -> Unit,
    onAddPlace: () -> Unit,
    onBack: () -> Unit,
    media: Map<String, MediaAsset> = emptyMap(),
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredPlaces = remember(places, selectedCategory) {
        if (selectedCategory == null) places
        else places.filter { it.category == selectedCategory }
    }

    val categories = remember(places) {
        places.map { it.category }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            Header(
                title = "Places",
                onBack = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlace,
                containerColor = LocalTwoHeartsColors.current.burgundy,
                contentColor = LocalTwoHeartsColors.current.textOnAccent,
                icon = { Icon(ThIcons.Add, null) }, text = { Text("Save a place") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category chips
            if (categories.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        text = { Text("All") }
                    )
                    categories.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { Text(category.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            if (filteredPlaces.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(ThIcons.MapPin, null, tint = LocalTwoHeartsColors.current.burgundy, modifier = Modifier.size(52.dp)); Spacer(Modifier.height(20.dp))
                    Text("The places in your story", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp)); Text("Keep the café, park, home, or faraway corner that became part of us.", color = LocalTwoHeartsColors.current.textSecondary)
                    Spacer(Modifier.height(24.dp)); Button(onClick = onAddPlace) { Text("Save your first place") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPlaces) { place ->
                        PlaceCard(
                            place = place,
                            media = media[place.id],
                            onClick = { onPlaceClick(place.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * PlaceCard — place card for list display.
 */
@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    media: MediaAsset? = null,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            MemoryMediaFrame(media, Modifier.fillMaxWidth().height(150.dp))
            Spacer(modifier = Modifier.height(12.dp))
            // Title
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (place.address != null || place.city != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildString {
                        place.address?.let { append(it) }
                        if (place.city != null) {
                            if (isNotEmpty()) append(", ")
                            append(place.city)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            if (place.notes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = place.notes.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category badge
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = place.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
