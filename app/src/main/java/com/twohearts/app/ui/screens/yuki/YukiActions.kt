package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.game.YukiAction
import com.twohearts.app.ui.components.ThButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun YukiActions(
    sleeping: Boolean,
    busy: Boolean,
    onAction: (YukiAction) -> Unit,
) {
    if (sleeping) {
        ThButton(onClick = { onAction(YukiAction.WAKE) }, enabled = !busy, text = "Wake Yuki", full = true)
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3,
        ) {
            listOf(YukiAction.FEED, YukiAction.PLAY, YukiAction.PET, YukiAction.DRINK, YukiAction.CLEAN, YukiAction.SLEEP)
                .forEach { action ->
                    OutlinedButton(onClick = { onAction(action) }, enabled = !busy, modifier = Modifier.weight(1f)) {
                        Text(action.label)
                    }
                }
        }
    }
}


