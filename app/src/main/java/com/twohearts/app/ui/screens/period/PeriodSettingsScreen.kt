package com.twohearts.app.ui.screens.period

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@Composable
fun PeriodSettingsScreen(
    onBack: () -> Unit,
    initialCycle: Int = 28,
    initialLength: Int = 5,
    onSave: (Int, Int) -> Unit = { _, _ -> },
    saving: Boolean = false,
    error: String? = null,
) {
    var cycleText by rememberSaveable(initialCycle) { mutableStateOf(initialCycle.toString()) }
    var lengthText by rememberSaveable(initialLength) { mutableStateOf(initialLength.toString()) }
    val cycle = cycleText.toIntOrNull()
    val length = lengthText.toIntOrNull()
    val valid = cycle != null && cycle in 1..365 && length != null && length in 1..minOf(cycle, 30)
    val colors = LocalTwoHeartsColors.current

    Scaffold(topBar = { Header(title = "Cycle settings", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "These figures estimate the next start and length of your period. They stay on this device.",
                color = colors.textSecondary,
            )
            Input(
                value = cycleText, onValueChange = { cycleText = it },
                label = "Typical cycle length (days)", modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number,
                error = if (cycle == null || cycle !in 1..365) "Enter 1–365 days." else null,
            )
            Input(
                value = lengthText, onValueChange = { lengthText = it },
                label = "Typical period length (days)", modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number,
                error = if (length == null || length !in 1..minOf(cycle ?: 30, 30)) {
                    "Enter 1–30 days, within your cycle length."
                } else null,
            )
            if (error != null) ThStatusBanner(StatusVariant.ERROR, error)
            ThButton(
                onClick = { if (valid) onSave(cycle, length) },
                enabled = valid, loading = saving, text = "Save settings", full = true,
            )
        }
    }
}
