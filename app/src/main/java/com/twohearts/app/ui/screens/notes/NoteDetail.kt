package com.twohearts.app.ui.screens.notes

import androidx.compose.runtime.Composable
import com.twohearts.app.data.entity.Note
import com.twohearts.app.ui.components.RecordReadingLayout

@Composable
fun NoteDetail(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    deleting: Boolean = false,
    errorMessage: String? = null,
) {
    RecordReadingLayout(
        id = note.id, screenTitle = "Note", title = note.title, headingDetail = NoteCategory.fromString(note.category).label,
        body = note.content, createdAt = note.createdAt, updatedAt = note.updatedAt, recordLabel = "note",
        deletionDescription = "This permanently removes the note from TwoHearts.",
        onEdit = onEdit, onDelete = onDelete, onBack = onBack, deleting = deleting, errorMessage = errorMessage,
    )
}
