package com.twohearts.app.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * RelationshipCounter — displays days together.
 *
 * Matches legacy relationshipCounter helper with:
 * - Days since start date
 * - "Our story together" label
 */
@Composable
fun RelationshipCounter(
    startDate: String,
    modifier: Modifier = Modifier
) {
    val daysTogether = DateTimeHelper.daysSinceStartDate(startDate)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Our story together",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$daysTogether days",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}
