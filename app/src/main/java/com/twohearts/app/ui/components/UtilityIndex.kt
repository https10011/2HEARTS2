package com.twohearts.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
internal fun UtilityGroup(title: String, items: @Composable ColumnScope.() -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    Column {
        ThEyebrow(
            title, color = LocalTwoHeartsColors.current.roseDeep,
            modifier = Modifier.padding(bottom = spacing.space2).semantics { heading() },
        )
        ThDivider()
        items()
    }
}

@Composable
internal fun UtilityItem(
    title: String,
    icon: ImageVector,
    details: String? = null,
    onClick: () -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3), verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = colors.roseDeep, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            details?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }
        }
        Icon(ThIcons.ChevronRight, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
    }
    ThDivider()
}


@Composable
internal fun UtilityPage(
    title: String, onBack: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    Scaffold(modifier = modifier, topBar = { ThTopBar(title, onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.widthIn(max = 620.dp).fillMaxWidth().padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
                verticalArrangement = Arrangement.spacedBy(spacing.space5),
                content = content,
            )
        }
    }
}
