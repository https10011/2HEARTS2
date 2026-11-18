package com.twohearts.app.ui.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.Note
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesHome(notes: List<Note>, onNoteClick: (String) -> Unit, onAddNote: () -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<NoteCategory?>(null) }
    val filtered = remember(notes, query, category) { filterNotes(notes, query, category) }
    val colors = LocalTwoHeartsColors.current
    Scaffold(
        topBar = { Header(title = "Notes", onBack = onBack) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAddNote, icon = { Icon(ThIcons.Edit, null) }, text = { Text("Write a note") }, containerColor = colors.burgundy, contentColor = colors.textOnAccent) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (notes.isNotEmpty()) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), placeholder = { Text("Search your notes") }, leadingIcon = { Icon(ThIcons.Search, null) }, trailingIcon = if (query.isNotEmpty()) {{ IconButton({ query = "" }) { Icon(ThIcons.Clear, "Clear search") } }} else null, singleLine = true, shape = RoundedCornerShape(18.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(category == null, { category = null }, { Text("All") }) }
                    items(NoteCategory.entries) { item -> FilterChip(category == item, { category = item }, { Text(item.label) }, leadingIcon = { Icon(item.icon, null, Modifier.size(17.dp)) }) }
                }
            }
            when {
                notes.isEmpty() -> NotesEmptyState(onAddNote)
                filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No notes match this view", color = colors.textSecondary) }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 104.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item { Text("Words worth keeping", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold); Text("Private thoughts, shared moments, and little reminders.", color = colors.textSecondary) }
                    items(filtered, key = { it.id }) { note -> NotePaper(note) { onNoteClick(note.id) } }
                }
            }
        }
    }
}

internal fun filterNotes(notes: List<Note>, query: String, category: NoteCategory?): List<Note> = notes.filter { note ->
    (category == null || NoteCategory.fromString(note.category) == category) &&
        (query.isBlank() || note.title.contains(query, true) || note.content.contains(query, true))
}

@Composable
private fun NotesEmptyState(onAdd: () -> Unit) {
    val colors = LocalTwoHeartsColors.current
    Column(Modifier.fillMaxSize().padding(36.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(ThIcons.Note, null, tint = colors.burgundy, modifier = Modifier.size(54.dp)); Spacer(Modifier.height(22.dp))
        Text("A quiet place for your words", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp)); Text("Write a love letter, a thought to share, or something only you need to remember.", color = colors.textSecondary)
        Spacer(Modifier.height(24.dp)); Button(onClick = onAdd) { Icon(ThIcons.Edit, null); Spacer(Modifier.width(8.dp)); Text("Write your first note") }
    }
}

@Composable
private fun NotePaper(note: Note, onClick: () -> Unit) {
    val category = NoteCategory.fromString(note.category)
    val colors = LocalTwoHeartsColors.current
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), color = if (category == NoteCategory.LOVE_LETTER) colors.surfaceBlush else colors.surfaceElevated, tonalElevation = 1.dp) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(category.icon, null, tint = colors.roseDeep, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(category.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.roseDeep); Spacer(Modifier.weight(1f)); Text(DateTimeHelper.formatDisplay(note.updatedAt), style = MaterialTheme.typography.labelSmall, color = colors.textTertiary) }
            Spacer(Modifier.height(12.dp)); Text(note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp)); Text(note.content, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = colors.textSecondary, maxLines = 4, overflow = TextOverflow.Ellipsis)
        }
    }
}
