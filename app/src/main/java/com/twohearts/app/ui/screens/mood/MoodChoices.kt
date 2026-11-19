package com.twohearts.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Uses the parent screen's scroll container and lets every choice grow with its label. */
@Composable
internal fun MoodChoices(
    selected: String?,
    onSelect: (MoodEntryData) -> Unit,
    compact: Boolean = false,
) {
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val minChoiceWidth = (84 * fontScale).dp
        val columns = (maxWidth / minChoiceWidth).toInt()
            .coerceIn(1, if (compact) 4 else 2)
        Column(
            Modifier.fillMaxWidth().selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MoodMeta.moods.chunked(columns).forEach { options ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (mood in options) {
                        val chosen = mood.value == selected
                        Surface(
                            modifier = Modifier.weight(1f)
                                .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
                                .selectable(chosen, role = Role.RadioButton, onClick = { onSelect(mood) }),
                            shape = MaterialTheme.shapes.medium,
                            color = if (chosen) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (chosen) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                        ) {
                            Column(
                                Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(mood.emoji, Modifier.clearAndSetSemantics {}, style = MaterialTheme.typography.titleLarge)
                                Text(mood.label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    repeat(columns - options.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
