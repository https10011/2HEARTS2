package com.twohearts.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.services.search.SearchResult
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.ThLoadingState
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal sealed interface SearchUiState {
    data object Empty : SearchUiState
    data object Searching : SearchUiState
    data object Failed : SearchUiState
    data class Results(val records: List<SearchResult>) : SearchUiState
}

private sealed interface SearchIndex {
    data object Loading : SearchIndex
    data object Failed : SearchIndex
    data class Ready(val records: List<SearchResult>) : SearchIndex
}

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
    var retry by remember { mutableIntStateOf(0) }
    val normalized = searchEngine.normalize(query)
    val indexFlow = remember(noteRepository, memoryRepository, reminderRepository, placeRepository, timelineEventRepository, retry) {
        combine(
            noteRepository.observeAll(), memoryRepository.observeAll(), reminderRepository.observeAll(),
            placeRepository.observeAll(), timelineEventRepository.observeAll(),
        ) { notes, memories, reminders, places, events ->
            val index: SearchIndex = SearchIndex.Ready(buildList {
                notes.forEach { add(searchRecord(it.id, "note", it.title, it.content, it.updatedAt)) }
                memories.forEach { add(searchRecord(it.id, "memory", it.title, it.caption, it.updatedAt)) }
                reminders.forEach { add(searchRecord(it.id, "reminder", it.title, it.description, it.updatedAt)) }
                places.forEach {
                    val detail = listOfNotNull(it.city, it.address, it.notes).filter(String::isNotBlank).joinToString(" · ")
                    add(searchRecord(it.id, "place", it.name, detail, it.updatedAt))
                }
                events.forEach { add(searchRecord(it.id, "timeline", it.title, it.description, it.updatedAt)) }
            })
            index
        }.flowOn(Dispatchers.Default).catch { emit(SearchIndex.Failed) }
    }
    val index = if (normalized.isNotEmpty()) indexFlow.collectAsStateWithLifecycle(initialValue = SearchIndex.Loading).value
        else SearchIndex.Loading
    var state by remember(normalized, index, retry) {
        mutableStateOf<SearchUiState>(when {
            normalized.isEmpty() -> SearchUiState.Empty
            index == SearchIndex.Failed -> SearchUiState.Failed
            else -> SearchUiState.Searching
        })
    }
    LaunchedEffect(normalized, index, searchEngine) {
        if (normalized.isNotEmpty() && index is SearchIndex.Ready) {
            delay(120)
            state = SearchUiState.Results(withContext(Dispatchers.Default) { searchEngine.searchRecords(normalized, index.records) })
        }
    }
    SearchContent(query, { query = it }, state, { retry++ }, onResultClick, onBack)
}

@Composable
internal fun SearchContent(
    query: String, onQuery: (String) -> Unit, state: SearchUiState, retry: () -> Unit,
    onResult: (String, String) -> Unit, onBack: () -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            ThTopBar("Search", onBack = onBack, actions = {
                if (query.isNotEmpty()) {
                    ThIconButton({ onQuery("") }, label = "Clear search") { Icon(ThIcons.Close, null) }
                }
            })
        },
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets)) {
            ThInput(
                query, onQuery, modifier = Modifier.padding(horizontal = spacing.screenGutter, vertical = spacing.space3),
                label = "Search TwoHearts", placeholder = "A title or a few words", imeAction = ImeAction.Search,
            )
            ThDivider()
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space3),
            ) {
                when (state) {
                    SearchUiState.Empty -> item {
                        ThEmptyState(
                            "Find something you saved", description = "Search notes, memories, reminders, places, and timeline events.",
                            visual = { Icon(ThIcons.Search, null, tint = colors.roseDeep) },
                        )
                    }
                    SearchUiState.Searching -> item { ThLoadingState(label = "Searching") }
                    SearchUiState.Failed -> item {
                        ThEmptyState("Search unavailable", actionLabel = "Try again", onAction = retry)
                    }
                    is SearchUiState.Results -> {
                        if (state.records.isEmpty()) item {
                            ThEmptyState("No matches", description = "Try fewer words or a different spelling.")
                        } else {
                            item {
                                val count = state.records.size
                                Text(
                                    if (count == 1) "1 result" else "$count results",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = spacing.space1).semantics { heading() },
                                )
                            }
                            items(state.records, key = { "${it.type}:${it.id}" }) { record ->
                                SearchResultRow(record) { onResult(record.type, record.id) }
                                ThDivider()
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun SearchResultRow(record: SearchResult, onClick: () -> Unit) {
    val (section, icon) = when (record.type) {
        "note" -> "Notes" to ThIcons.Note
        "memory" -> "Memories" to ThIcons.PhotoLibrary
        "reminder" -> "Reminders" to ThIcons.Bell
        "place" -> "Places" to ThIcons.MapPin
        else -> "Timeline" to ThIcons.Clock
    }
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    Row(
        Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .clickable(onClickLabel = "Open search result", role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = "${record.title}, $section" }
            .padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, tint = colors.roseDeep)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            ThEyebrow(section, color = colors.roseDeep)
            Text(record.title, style = MaterialTheme.typography.titleMedium)
            if (record.subtitle.isNotBlank()) Text(
                record.subtitle, style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(ThIcons.ChevronRight, null, tint = colors.textSecondary)
    }
}

private fun searchRecord(id: String, type: String, title: String, details: String?, modified: String): SearchResult =
    SearchResult(id, type, title, details.orEmpty(), score = 0, recency = runCatching { Instant.parse(modified).toEpochMilli() }.getOrDefault(0))
