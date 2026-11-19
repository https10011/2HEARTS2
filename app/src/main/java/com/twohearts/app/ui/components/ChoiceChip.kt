package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** A single toggleable choice with a visible selected surface and an accessible touch target. */
@Composable
fun ThChoiceChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val palette = LocalTwoHeartsColors.current
    val scheme = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin),
        enabled = enabled,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(percent = 50),
        colors = FilterChipDefaults.filterChipColors(
            labelColor = scheme.onSurface,
            iconColor = scheme.primary,
            selectedContainerColor = scheme.primaryContainer,
            selectedLabelColor = scheme.onPrimaryContainer,
            selectedLeadingIconColor = scheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = palette.borderStrong,
            selectedBorderColor = scheme.primaryContainer,
        ),
    )
}
