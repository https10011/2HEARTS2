package com.twohearts.app.ui.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.Note
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.ThQuietButton
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.navigation.ShellSearchAction
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun NotesHome(
    notes: List<Note>,
    onNoteClick: (String) -> Unit,
    onAddNote: () -> Unit,
    onBack: () -> Unit,
    onSearch: (() -> Unit)? = null,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<NoteCategory?>(null) }
    var filtering by rememberSaveable { mutableStateOf(false) }
    val filtered = remember(notes, query, category) { filterNotes(notes, query, category) }
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    fun clearFilters() {
        query = ""
        category = null
    }

    Scaffold(
        topBar = {
            ThTopBar("Notes", onBack = onBack, actions = {
                if (onSearch != null) ShellSearchAction(onSearch)
                if (notes.isNotEmpty()) ThIconButton(onAddNote, label = "Write a note") {
                    Icon(ThIcons.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            })
        },
    ) { padding ->
        if (notes.isEmpty()) {
            ThEmptyState(
                title = "A quiet place for your words",
                description = "Keep a letter, a shared thought, or something you want to remember.",
                visual = { Icon(ThIcons.Note, null, Modifier.size(48.dp), tint = colors.roseDeep) },
                actionLabel = "Write your first note", onAction = onAddNote,
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        ThInput(
                            query, { query = it }, label = "Search your notes",
                            placeholder = "Find a word or title",
                        )
                        ThButton(
                            { filtering = true }, variant = ButtonVariant.SECONDARY, text = "Category · ${category?.label ?: "All notes"}",
                            icon = ThIcons.ChevronRight, full = true,
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (filtered.size == 1) "1 note" else "${filtered.size} notes",
                                Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary,
                            )
                            if (query.isNotEmpty() || category != null) ThQuietButton(::clearFilters, "Clear filters")
                        }
                        ThDivider()
                    }
                }
                if (filtered.isEmpty()) {
                    item {
                        ThEmptyState(title = "No matching notes", description = "Try a different word or category.")
                    }
                }
                items(filtered, key = { it.id }) { note ->
                    NoteEntry(note) { onNoteClick(note.id) }
                    ThDivider()
                }
            }
        }
    }

    NoteCategoryPicker(filtering, category, { filtering = false }, { category = it; filtering = false }, includeAll = true)
}

internal fun filterNotes(notes: List<Note>, query: String, category: NoteCategory?): List<Note> {
    val search = query.trim()
    return notes.filter { note ->
        (category == null || NoteCategory.fromString(note.category) == category) &&
            (search.isEmpty() || note.title.contains(search, true) || note.content.contains(search, true))
    }
}

@Composable
private fun NoteEntry(note: Note, onClick: () -> Unit) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val category = NoteCategory.fromString(note.category)
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = spacing.space4),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(category.icon, null, Modifier.size(22.dp), tint = colors.roseDeep)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            ThEyebrow("${category.label} · ${DateTimeHelper.formatDisplay(note.updatedAt)}")
            Text(note.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(note.content.replace('\n', ' '), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(ThIcons.ChevronRight, null, Modifier.size(20.dp), tint = colors.textSecondary)
    }
}
