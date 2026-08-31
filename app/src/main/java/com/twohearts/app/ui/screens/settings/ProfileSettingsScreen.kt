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
 * ProfileSettingsScreen — edit user profile (name, birthday).
 *
 * Matches legacy ProfileSettingsScreen with:
 * - Owner name editing
 * - Birthday editing
 * - Save button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit
) {
    var ownerName by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Header(
                title = "Profile",
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
                text = "Your Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Edit your name and birthday. Your partner's info is managed separately.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Owner name
            Input(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = "Your Name",
                placeholder = "Enter your name",
                modifier = Modifier.fillMaxWidth()
            )

            // Birthday
            Input(
                value = birthday,
                onValueChange = { birthday = it },
                label = "Birthday",
                placeholder = "yyyy-mm-dd",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    // TODO: Save profile via AppStateService/RelationshipService
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                text = "Save Profile"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo section
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "📷 Add Profile Photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
