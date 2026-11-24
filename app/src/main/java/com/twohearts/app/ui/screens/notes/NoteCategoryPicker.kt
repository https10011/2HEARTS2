package com.twohearts.app.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.components.ThChoiceChip
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.theme.TwoHeartsTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NoteCategoryPicker(
    open: Boolean,
    selected: NoteCategory?,
    onClose: () -> Unit,
    onSelect: (NoteCategory?) -> Unit,
    includeAll: Boolean = false,
) {
    if (!open) return
    val spacing = TwoHeartsTokens.Spacing
    val title = if (includeAll) "Filter by category" else "Choose a category"
    ThModal(true, onClose, Modifier.verticalScroll(rememberScrollState()), label = title) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                verticalArrangement = Arrangement.spacedBy(spacing.space2),
            ) {
                if (includeAll) {
                    ThChoiceChip(selected == null, { onSelect(null) }, { Text("All notes") })
                }
                NoteCategory.entries.forEach { category ->
                    ThChoiceChip(
                        selected == category, { onSelect(category) }, { Text(category.label) },
                        leadingIcon = { Icon(category.icon, contentDescription = null) },
                    )
                }
            }
        }
    }
}
