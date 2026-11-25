package com.twohearts.app.ui.screens.period

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThInput
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
    val cycle = cycleText.trim().toIntOrNull()
    val length = lengthText.trim().toIntOrNull()
    val periodLimit = minOf(cycle?.takeIf { it in 1..365 } ?: 30, 30)
    val valid = cycle != null && cycle in 1..365 && length != null && length in 1..periodLimit
    val colors = LocalTwoHeartsColors.current

    RecordEditorLayout(
        title = "Cycle settings", saveText = "Save settings",
        onSave = { if (valid && !saving) onSave(cycle, length) },
        onBack = onBack, saving = saving, errorMessage = error, saveEnabled = valid,
    ) {
        ThEyebrow("Calendar estimates", color = colors.goldInk)
        Text(
            "Set the usual lengths to estimate future dates. These settings stay on this device.",
            style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary,
        )
        ThInput(
            cycleText, { cycleText = it }, label = "Typical cycle length (days)",
            keyboardType = KeyboardType.Number, enabled = !saving,
            error = "Enter 1–365 days.".takeIf { cycle == null || cycle !in 1..365 },
        )
        ThInput(
            lengthText, { lengthText = it }, label = "Typical period length (days)",
            keyboardType = KeyboardType.Number, enabled = !saving,
            error = "Enter 1–$periodLimit days.".takeIf { length == null || length !in 1..periodLimit },
        )
    }
}
