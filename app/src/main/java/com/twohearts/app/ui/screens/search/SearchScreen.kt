package com.twohearts.app.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.repository.*
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.services.search.SearchResult
import com.twohearts.app.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.Instant

@Composable
fun SearchScreen(
    searchEngine: SearchEngine,
    noteRepository: NoteRepository,
    memoryRepository: MemoryRepository,
    reminderRepository: ReminderRepository,
    placeRepository: PlaceRepository,
    timelineEventRepository: TimelineEventRepository,
    onResultClick: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<SearchResult>()) }
    var searching by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(query) {
        results = emptyList()
        failed = false
        searching = query.isNotBlank()
        if (query.isBlank()) return@LaunchedEffect
        delay(160)
        try {
            combine(
                noteRepository.observeAll(), memoryRepository.observeAll(), reminderRepository.observeAll(),
                placeRepository.observeAll(), timelineEventRepository.observeAll(),
            ) { notes, memories, reminders, places, events ->
                withContext(Dispatchers.Default) {
                    val candidates = buildList {
                        notes.forEach { add(result(it.id, "note", it.title, it.content, it.updatedAt)) }
                        memories.forEach { add(result(it.id, "memory", it.title, it.caption, it.updatedAt)) }
                        reminders.forEach { add(result(it.id, "reminder", it.title, it.description, it.updatedAt)) }
                        places.forEach {
                            val details = listOfNotNull(it.city, it.address, it.notes).joinToString(" · ")
                            add(result(it.id, "place", it.name, details, it.updatedAt))
                        }
                        events.forEach { add(result(it.id, "timeline", it.title, it.description, it.updatedAt)) }
                    }
                    searchEngine.searchRecords(query, candidates)
                }
            }.collect {
                results = it
                searching = false
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            failed = true
            searching = false
        }
    }

    Scaffold(topBar = { ThTopBar(title = "Search", onBack = onBack) }) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                label = { Text("Search TwoHearts") },
                leadingIcon = { Icon(ThIcons.Search, contentDescription = null) },
                trailingIcon = if (query.isBlank()) null else {
                    { IconButton(onClick = { query = "" }) { Icon(ThIcons.Close, "Clear search") } }
                },
            )
            when {
                failed -> ThEmptyState("Search unavailable", description = "Change your search to try again.")
                searching -> ThLoadingState(label = "Searching")
                query.isBlank() -> ThEmptyState(
                    "Find something you saved",
                    description = "Search notes, memories, reminders, places, and timeline events.",
                )
                results.isEmpty() -> ThEmptyState("No matches", description = "Try a shorter word or a different spelling.")
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(results, key = { "${it.type}:${it.id}" }) { result ->
                        val destination = when (result.type) {
                            "note" -> "Notes" to ThIcons.Note
                            "memory" -> "Memories" to ThIcons.PhotoLibrary
                            "reminder" -> "Reminders" to ThIcons.Bell
                            "place" -> "Places" to ThIcons.MapPin
                            else -> "Timeline" to ThIcons.Clock
                        }
                        Surface(onClick = { onResultClick(result.type, result.id) }) {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(destination.second, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(destination.first, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(result.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (result.subtitle.isNotBlank()) Text(
                                        result.subtitle, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Icon(ThIcons.ChevronRight, contentDescription = null)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun result(id: String, type: String, title: String, details: String?, modified: String): SearchResult =
    SearchResult(
        id, type, title, details.orEmpty(), score = 0,
        recency = runCatching { Instant.parse(modified).toEpochMilli() }.getOrDefault(0),
    )
