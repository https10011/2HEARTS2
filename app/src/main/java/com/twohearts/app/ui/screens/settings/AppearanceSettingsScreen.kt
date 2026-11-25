package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.ui.components.ThChoiceChip
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingsScreen(appStateService: AppStateService, onBack: () -> Unit) {
    val theme by appStateService.themeMode.collectAsStateWithLifecycle()
    val textSize by appStateService.textSize.collectAsStateWithLifecycle()
    val reduceMotion by appStateService.reduceMotion.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    AppearanceSettingsContent(
        theme, textSize, reduceMotion, onBack,
        onTheme = { scope.launch { appStateService.setThemeMode(it) } },
        onTextSize = { scope.launch { appStateService.setTextSize(it) } },
        onMotion = { scope.launch { appStateService.setReduceMotion(it) } },
    )
}

@Composable
internal fun AppearanceSettingsContent(
    theme: String, textSize: String, reduced: Boolean, onBack: () -> Unit,
    onTheme: (String) -> Unit, onTextSize: (String) -> Unit, onMotion: (Boolean) -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    UtilityPage("Appearance", onBack) {
        Surface(color = colors.surfaceBlush, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                ThEyebrow("Preview", color = colors.roseDeep)
                Text("TwoHearts", style = MaterialTheme.typography.displayMedium)
                Text("Your memories, notes, and dates", style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
        }
        UtilityGroup("Theme") {
            AppearanceChoices(theme, listOf("system" to "Follow Android", "light" to "Light", "dark" to "Dark"), onTheme)
        }
        UtilityGroup("Text size") {
            AppearanceChoices(
                textSize, listOf("small" to "Small", "default" to "Default", "large" to "Large", "extra_large" to "Extra large"), onTextSize,
            )
        }
        UtilityGroup("Motion") {
            UtilityToggle(
                "Reduce motion", "Use simpler transitions and fewer decorative animations.",
                reduced, onChange = onMotion,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceChoices(selected: String, choices: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    FlowRow(
        Modifier.fillMaxWidth().selectableGroup().padding(top = spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        choices.forEach { (value, label) ->
            ThChoiceChip(selected == value, { onSelect(value) }, label = { Text(label) })
        }
    }
}

@Composable
internal fun UtilityToggle(
    title: String, details: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    Row(
        Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(details, style = MaterialTheme.typography.bodyMedium, color = LocalTwoHeartsColors.current.textSecondary)
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
