package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.navigation.RoutePath

/**
 * SettingsHomeScreen — settings hub with navigation to sub-screens.
 *
 * Matches legacy SettingsHomeScreen with:
 * - Profile settings
 * - Relationship settings
 * - Appearance settings
 * - Notification settings
 * - Security settings
 * - Storage settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Account section
            SettingsSection(title = "Account") {
                SettingsMenuItem(
                    icon = Icons.Default.Person,
                    title = "Profile",
                    subtitle = "Edit your name and photo",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_PROFILE) }
                )
                SettingsMenuItem(
                    icon = Icons.Default.FavoriteBorder,
                    title = "Relationship",
                    subtitle = "Edit partner name and start date",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_RELATIONSHIP) }
                )
            }

            // Preferences section
            SettingsSection(title = "Preferences") {
                SettingsMenuItem(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Theme, text size, and motion",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_APPEARANCE) }
                )
                SettingsMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification settings",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_NOTIFICATIONS) }
                )
                SettingsMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Security",
                    subtitle = "App lock and PIN management",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_SECURITY) }
                )
            }

            // Data section
            SettingsSection(title = "Data") {
                SettingsMenuItem(
                    icon = Icons.Default.Storage,
                    title = "Storage",
                    subtitle = "Storage usage, cache, and reset",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_STORAGE) }
                )
                SettingsMenuItem(
                    icon = Icons.Default.FileUpload,
                    title = "Import",
                    subtitle = "Import notes and reminders",
                    onClick = { onNavigate(RoutePath.APP_SETTINGS_IMPORT) }
                )
            }
        }
    }
}

/**
 * SettingsSection — grouped section with title.
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * SettingsMenuItem — menu item with icon, title, and subtitle.
 */
@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * HorizontalDivider for settings sections.
 */
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
