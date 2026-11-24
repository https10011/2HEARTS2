package com.twohearts.app.ui.screens.notes

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput

@Composable
fun NoteEditor(
    noteId: String? = null,
    initialTitle: String = "",
    initialContent: String = "",
    initialCategory: NoteCategory = NoteCategory.GENERAL,
    onSave: (title: String, content: String, category: NoteCategory) -> Unit,
    onBack: () -> Unit,
    saving: Boolean = false,
    errorMessage: String? = null,
) {
    var title by rememberSaveable(noteId) { mutableStateOf(initialTitle) }
    var content by rememberSaveable(noteId) { mutableStateOf(initialContent) }
    var category by rememberSaveable(noteId) { mutableStateOf(initialCategory) }
    var chooseCategory by rememberSaveable(noteId) { mutableStateOf(false) }
    var attempted by rememberSaveable(noteId) { mutableStateOf(false) }
    val isEditing = noteId != null
    RecordEditorLayout(
        title = if (isEditing) "Edit note" else "New note",
        saveText = if (isEditing) "Save changes" else "Keep this note",
        saving = saving,
        errorMessage = errorMessage,
        onBack = onBack,
        onSave = {
            attempted = true
            if (title.isNotBlank() && content.isNotBlank()) onSave(title.trim(), content.trim(), category)
        },
    ) {
        ThInput(
            title, { title = it.replace('\n', ' ') }, label = "Title", placeholder = "Give your note a title",
            enabled = !saving, singleLine = false, maxLines = 2,
            imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences,
            error = "Give this note a title.".takeIf { attempted && title.isBlank() },
        )
        ThButton(
            { chooseCategory = true }, variant = ButtonVariant.SECONDARY, icon = ThIcons.ChevronRight,
            text = "Category · ${category.label}", full = true, enabled = !saving,
        )
        ThInput(
            content, { content = it }, Modifier.heightIn(min = 208.dp),
            label = "Note", placeholder = "Write your thoughts", enabled = !saving, multiline = true,
            capitalization = KeyboardCapitalization.Sentences,
            error = "Write something before saving.".takeIf { attempted && content.isBlank() },
        )
    }
    NoteCategoryPicker(chooseCategory, category, { chooseCategory = false }, {
        category = requireNotNull(it)
        chooseCategory = false
    })
}
