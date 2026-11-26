package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.game.YukiAction
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.theme.TwoHeartsTokens

private data class CareControl(val action: YukiAction, val icon: ImageVector)

private val everydayCare = listOf(
    CareControl(YukiAction.FEED, Icons.Outlined.Restaurant),
    CareControl(YukiAction.PLAY, ThIcons.Play),
    CareControl(YukiAction.PET, ThIcons.Heart),
)
private val quietCare = listOf(
    CareControl(YukiAction.DRINK, Icons.Outlined.WaterDrop),
    CareControl(YukiAction.CLEAN, Icons.Outlined.Brush),
    CareControl(YukiAction.SLEEP, Icons.Outlined.Bedtime),
)

@Composable
internal fun YukiActions(sleeping: Boolean, busy: Boolean, onAction: (YukiAction) -> Unit) {
    if (sleeping) {
        ThButton(
            { onAction(YukiAction.WAKE) }, enabled = !busy, icon = Icons.Outlined.WbSunny, text = "Wake Yuki", full = true,
        )
    } else {
        val spacing = TwoHeartsTokens.Spacing
        UtilityGroup("Care") {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val scale = LocalDensity.current.fontScale
                val compact = maxWidth < 298.dp || scale >= 1.5f
                val columns = if (compact) 2 else 3
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    CareGroup(everydayCare, columns, scale < 1.5f, !busy, ButtonVariant.SECONDARY, onAction)
                    CareGroup(quietCare, columns, scale < 1.5f, !busy, ButtonVariant.GHOST, onAction)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CareGroup(
    controls: List<CareControl>, columns: Int, showIcons: Boolean, enabled: Boolean, variant: ButtonVariant,
    onAction: (YukiAction) -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space1),
        maxItemsInEachRow = columns,
    ) {
        controls.forEach { control ->
            ThButton(
                { onAction(control.action) }, modifier = Modifier.weight(1f), full = true,
                variant = variant, enabled = enabled, icon = control.icon.takeIf { showIcons }, text = control.action.label,
            )
        }
    }
}
