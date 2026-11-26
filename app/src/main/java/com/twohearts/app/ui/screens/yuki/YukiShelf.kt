package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.data.game.YUKI_ACCESSORIES
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.data.game.YukiToy
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
internal fun YukiShelf(
    state: YukiState, enabled: Boolean, onToy: (YukiToy) -> Unit, onAccessory: (String) -> Unit, onDismiss: () -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    ThModal(open = true, onClose = onDismiss, label = "Yuki’s keepsakes") {
        Text(
            "Yuki’s keepsakes", style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(vertical = spacing.space3),
            verticalArrangement = Arrangement.spacedBy(spacing.space4),
        ) {
            UtilityGroup("Companion progress") {
                Column(Modifier.padding(vertical = spacing.space2), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Text("Companion level ${state.level}", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { (state.experience.toFloat() / state.xpToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(), color = colors.roseDeep, trackColor = colors.surfaceBlush,
                    )
                }
            }
            UtilityGroup("Choose a toy") {
                Column(Modifier.selectableGroup()) {
                    YukiToy.entries.forEach { toy ->
                        val unlocked = toy.unlockLevel <= state.level
                        val detail = if (unlocked) when (toy) {
                            YukiToy.FEATHER -> "A gentle chase"
                            YukiToy.BALL -> "A livelier chase"
                        } else "Unlocks at level ${toy.unlockLevel}"
                        KeepsakeChoice(toy.label, detail, state.selectedToy == toy, enabled && unlocked) { onToy(toy) }
                    }
                }
            }
            UtilityGroup("Accessories") {
                Column(Modifier.selectableGroup()) {
                    YUKI_ACCESSORIES.filter { it.id in state.ownedAccessories }.forEach { accessory ->
                        KeepsakeChoice(accessory.name, null, (state.accessory ?: "none") == accessory.id, enabled) {
                            onAccessory(accessory.id)
                        }
                    }
                }
            }
            UtilityGroup("Milestones") {
                val milestones = listOf(
                    "Meals together" to state.totalFeedings, "Play sessions" to state.totalPlays, "Pets" to state.totalPets,
                ).filter { it.second > 0 }
                if (milestones.isEmpty()) {
                    Text(
                        "Your first care moments will appear here.",
                        Modifier.padding(vertical = spacing.space2), color = colors.textSecondary,
                    )
                }
                milestones.forEach { (label, count) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = spacing.space2),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                    ) {
                        Text(label, Modifier.weight(1f), color = colors.textSecondary)
                        Text(count.toString(), color = colors.roseDeep)
                    }
                }
            }
        }
        ThButton(onDismiss, text = "Done", full = true)
    }
}

@Composable
private fun KeepsakeChoice(title: String, detail: String?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(spacing.space2), verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled, modifier = Modifier.clearAndSetSemantics {})
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            Text(title, color = if (enabled) colors.charcoal else colors.textSecondary, style = MaterialTheme.typography.titleMedium)
            detail?.let { Text(it, color = colors.textSecondary, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
