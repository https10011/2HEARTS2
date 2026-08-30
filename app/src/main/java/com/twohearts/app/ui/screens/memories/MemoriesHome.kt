package com.twohearts.app.ui.screens.memories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.EmptyState
import com.twohearts.app.ui.components.Header
import com.twohearts.app.data.entity.Memory

/**
 * MemoriesHome — memories grid gallery screen.
 *
 * Matches legacy MemoriesHome with:
 * - Grid gallery of memory cards
 * - Empty state
 * - Add memory FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesHome(
    memories: List<Memory>,
    onMemoryClick: (String) -> Unit,
    onAddMemory: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Header(
                title = "Memories",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMemory,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Memory"
                )
            }
        }
    ) { paddingValues ->
        if (memories.isEmpty()) {
            EmptyState(
                title = "No Memories Yet",
                message = "Capture your first memory to get started",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(memories) { memory ->
                    MemoryCard(
                        memory = memory,
                        onClick = { onMemoryClick(memory.id) }
                    )
                }
            }
        }
    }
}

/**
 * MemoryCard — memory card for grid display.
 */
@Composable
fun MemoryCard(
    memory: Memory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title
            Text(
                text = memory.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Date
            Text(
                text = memory.memoryDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (memory.caption != null) {
                Spacer(modifier = Modifier.height(8.dp))

                // Caption preview
                Text(
                    text = memory.caption.take(50),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
        }
    }
}
