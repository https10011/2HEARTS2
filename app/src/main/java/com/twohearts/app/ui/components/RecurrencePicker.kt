package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

internal fun recurrenceLabel(value: String): String = when (value) {
    "none" -> "Doesn’t repeat"
    "daily" -> "Every day"
    "weekly" -> "Every week"
    "monthly" -> "Every month"
    "yearly" -> "Every year"
    else -> value
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecurrencePicker(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean,
) {
    val spacing = TwoHeartsTokens.Spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
        Text("Repeat", style = MaterialTheme.typography.labelLarge, color = LocalTwoHeartsColors.current.textSecondary)
        FlowRow(
            Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(spacing.space1),
        ) {
            val choices = if (value in options) options else options + value
            for (option in choices) {
                val picked = option == value
                Row(
                    Modifier.heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
                        .selectable(picked, enabled = enabled, role = Role.RadioButton, onClick = { onSelect(option) })
                        .padding(horizontal = spacing.space2, vertical = spacing.space1),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space1),
                ) {
                    if (picked) Icon(ThIcons.Check, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        recurrenceLabel(option), style = MaterialTheme.typography.labelLarge,
                        color = if (picked) MaterialTheme.colorScheme.primary else LocalTwoHeartsColors.current.textSecondary,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }
    }
}
