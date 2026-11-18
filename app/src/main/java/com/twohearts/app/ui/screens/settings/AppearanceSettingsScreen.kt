package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.ui.components.ThTopBar
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingsScreen(appStateService: AppStateService, onBack: () -> Unit) {
    val theme by appStateService.themeMode.collectAsStateWithLifecycle()
    val textSize by appStateService.textSize.collectAsStateWithLifecycle()
    val reduceMotion by appStateService.reduceMotion.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { ThTopBar(title = "Appearance", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppearanceChoices("Theme", theme, listOf("system" to "Follow Android", "light" to "Light", "dark" to "Dark")) {
                scope.launch { appStateService.setThemeMode(it) }
            }
            HorizontalDivider()
            AppearanceChoices(
                "Text size", textSize,
                listOf("small" to "Small", "default" to "Default", "large" to "Large", "extra_large" to "Extra large"),
            ) { scope.launch { appStateService.setTextSize(it) } }
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().heightIn(min = 60.dp).toggleable(
                    value = reduceMotion, role = Role.Switch,
                    onValueChange = { value -> scope.launch { appStateService.setReduceMotion(value) } },
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Reduce motion", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Use simpler transitions and fewer decorative animations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = reduceMotion, onCheckedChange = null)
            }
        }
    }
}

@Composable
private fun AppearanceChoices(
    title: String, selected: String,
    choices: List<Pair<String, String>>, onSelect: (String) -> Unit,
) {
    Column(Modifier.selectableGroup()) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        choices.forEach { (key, label) ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
                    selected = selected == key, role = Role.RadioButton, onClick = { onSelect(key) },
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected == key, onClick = null)
                Text(label)
            }
        }
    }
}
