package com.twohearts.app.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@Composable
internal fun ShellSearchAction(onClick: () -> Unit) {
    ThIconButton(onClick = onClick, label = "Search all records") {
        Icon(ThIcons.Search, contentDescription = null, tint = LocalTwoHeartsColors.current.textSecondary)
    }
}
