package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Tab groups share the same selected treatment on fixed and scrolling rows. */
@Composable
fun ThSectionTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val surface = LocalTwoHeartsColors.current.surfaceWarm
    val tabs = @Composable {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                modifier = Modifier.heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin),
            ) {
                val shape = RoundedCornerShape(TwoHeartsTokens.Radius.lg)
                val selection = if (selected) Modifier.background(scheme.primaryContainer, shape) else Modifier
                Text(
                    label,
                    modifier = selection.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    if (scrollable) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex, modifier = modifier, containerColor = surface,
            edgePadding = 4.dp, indicator = {}, divider = {}, tabs = tabs,
        )
    } else {
        TabRow(
            selectedTabIndex = selectedIndex, modifier = modifier, containerColor = surface,
            indicator = {}, divider = {}, tabs = tabs,
        )
    }
}
