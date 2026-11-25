package com.twohearts.app.ui.screens.mood

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@Composable
fun MoodEntryScreen(
    initialMood: String = "",
    initialNote: String = "",
    onSave: (moodValue: String, moodEmoji: String, note: String?) -> Unit,
    onBack: () -> Unit,
    entryId: String? = null,
    entryDate: String = DateTimeHelper.todayLocal(),
    initialEmoji: String = "",
    onDelete: (() -> Unit)? = null,
    saving: Boolean = false,
    errorMessage: String? = null,
) {
    var selectedMood by rememberSaveable(entryId) { mutableStateOf(initialMood) }
    var note by rememberSaveable(entryId) { mutableStateOf(initialNote) }
    var confirming by rememberSaveable(entryId) { mutableStateOf(false) }
    val selected = MoodMeta.getMood(selectedMood) ?: initialMood.takeIf { entryId != null && selectedMood == it && it.isNotBlank() }
        ?.let { MoodEntryData(it, initialEmoji, it.replaceFirstChar(Char::titlecase)) }
    val colors = LocalTwoHeartsColors.current

    RecordEditorLayout(
        title = if (entryId == null) "Check in" else "Edit check-in",
        saveText = "Save check-in",
        onSave = { selected?.let { if (!saving) onSave(it.value, it.emoji, note.trim().ifEmpty { null }) } },
        onBack = onBack, saving = saving, errorMessage = errorMessage, saveEnabled = selected != null,
    ) {
        ThEyebrow(DateTimeHelper.formatDisplay(entryDate), color = colors.roseDeep)
        Text("How did you feel?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        if (selected != null && MoodMeta.getMood(selected.value) == null) {
            Text("Saved mood · ${selected.label}", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        MoodChoices(selectedMood, { selectedMood = it.value }, compact = true, enabled = !saving)
        ThInput(note, { note = it }, label = "Private note (optional)", multiline = true, enabled = !saving)
        if (onDelete != null) {
            ThButton(
                onClick = { confirming = true }, text = "Delete check-in", variant = ButtonVariant.DANGER,
                icon = ThIcons.Trash, enabled = !saving,
            )
        }
    }
    if (confirming && onDelete != null) ConfirmDialog(
        title = "Delete this check-in?", message = "The mood and its note will be removed from your history.",
        confirmText = "Delete", onConfirm = { confirming = false; onDelete() }, onDismiss = { confirming = false },
    )
}
