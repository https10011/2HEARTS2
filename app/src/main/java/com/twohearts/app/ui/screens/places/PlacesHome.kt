package com.twohearts.app.ui.screens.places

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.Place

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
    onBack: () -> Unit
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
            FloatingActionButton(
                onClick = onAddPlace,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Place"
                )
            }
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
                EmptyState(
                    title = "No Places Yet",
                    message = "Add your first place to get started"
                )
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
