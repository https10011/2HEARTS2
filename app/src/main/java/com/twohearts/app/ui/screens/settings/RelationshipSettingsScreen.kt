package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.Input
import com.twohearts.app.ui.components.Button
import com.twohearts.app.ui.theme.Burgundy

/**
 * RelationshipSettingsScreen — edit relationship info.
 *
 * Matches legacy RelationshipSettingsScreen with:
 * - Partner name editing
 * - Start date editing
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipSettingsScreen(
    onBack: () -> Unit
) {
    var partnerName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Header(
                title = "Relationship",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Our Relationship",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Edit your partner's name and when your relationship started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Partner name
            Input(
                value = partnerName,
                onValueChange = { partnerName = it },
                label = "Partner's Name",
                placeholder = "Enter partner's name",
                modifier = Modifier.fillMaxWidth()
            )

            // Start date
            Input(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Relationship Start Date",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    // TODO: Save relationship via RelationshipService
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                text = "Save Relationship"
            )
        }
    }
}
