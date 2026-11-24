package com.twohearts.app.ui.screens.timeline

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
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThInput
import java.time.LocalDate

@Composable
fun AddEvent(
    eventId: String? = null,
    initialTitle: String = "",
    initialDate: String = DateTimeHelper.todayLocal(),
    initialDescription: String = "",
    onSave: (title: String, eventDate: String, description: String?) -> Unit,
    onBack: () -> Unit,
    saving: Boolean = false,
    errorMessage: String? = null,
) {
    var title by rememberSaveable(eventId) { mutableStateOf(initialTitle) }
    var date by rememberSaveable(eventId) { mutableStateOf(initialDate) }
    var description by rememberSaveable(eventId) { mutableStateOf(initialDescription) }
    var attempted by rememberSaveable(eventId) { mutableStateOf(false) }
    val validDate = runCatching { LocalDate.parse(date.trim()) }.isSuccess
    val isEditing = eventId != null

    RecordEditorLayout(
        title = if (isEditing) "Edit moment" else "New moment",
        saveText = if (isEditing) "Save changes" else "Add to our story",
        saving = saving, errorMessage = errorMessage, onBack = onBack,
        onSave = {
            attempted = true
            if (title.isNotBlank() && validDate) onSave(title.trim(), date.trim(), description.trim().ifEmpty { null })
        },
    ) {
        ThInput(
            title, { title = it.replace('\n', ' ') }, label = "Title", placeholder = "What happened?",
            enabled = !saving, singleLine = false, maxLines = 2,
            imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences,
            error = "Give this moment a title.".takeIf { attempted && title.isBlank() },
        )
        ThInput(
            date, { date = it }, label = "Date (YYYY-MM-DD)", enabled = !saving,
            error = "Enter a valid date.".takeIf { attempted && !validDate },
        )
        ThInput(
            description, { description = it }, Modifier.heightIn(min = 164.dp), enabled = !saving,
            label = "Description (optional)", placeholder = "What do you remember?", multiline = true,
            capitalization = KeyboardCapitalization.Sentences,
        )
    }
}
