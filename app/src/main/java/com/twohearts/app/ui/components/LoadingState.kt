package com.twohearts.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ThLoadingState — loading spinner with caption matching legacy LoadingState.tsx.
 * Caption is visible (not only screen-reader) so reduced-motion users
 * still see status when spinner freezes.
 */
@Composable
fun ThLoadingState(
    modifier: Modifier = Modifier,
    label: String = "Loading…",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(TwoHeartsTokens.Spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
