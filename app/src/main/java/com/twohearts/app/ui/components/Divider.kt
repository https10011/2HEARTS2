package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

/**
 * Divider — horizontal visual separator matching legacy Divider.tsx.
 * Uses the design token divider color.
 */
@Composable
fun ThDivider(modifier: Modifier = Modifier) {
    val thColors = LocalTwoHeartsColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(thColors.divider)
    )
}
