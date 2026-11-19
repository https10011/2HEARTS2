package com.twohearts.app.ui.screens.more

import com.twohearts.app.ui.components.ThIcons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.navigation.RoutePath

/**
 * MoreScreen — utility links screen.
 *
 * Matches legacy MoreScreen with:
 * - Settings link
 * - Search link
 * - About link
 * - Other utility links
 */
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp).semantics { heading() }
        )

        MoreMenuItem(
            icon = ThIcons.Search,
            title = "Search",
            subtitle = "Find notes, memories, and more",
            onClick = { onNavigate(RoutePath.APP_SEARCH) },
        )

        MoreMenuItem(
            icon = ThIcons.Bell,
            title = "Notifications",
            subtitle = "Reminders and important dates",
            onClick = { onNavigate(RoutePath.APP_NOTIFICATIONS) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Settings section
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MoreMenuItem(
            icon = ThIcons.Settings,
            title = "App Settings",
            subtitle = "Appearance, notifications, security",
            onClick = { onNavigate("/app/settings") }
        )

        MoreMenuItem(
            icon = ThIcons.Person,
            title = "Profile Settings",
            subtitle = "Edit your profile and partner's",
            onClick = { onNavigate("/app/settings/profile") }
        )

        MoreMenuItem(
            icon = ThIcons.Heart,
            title = "Relationship Settings",
            subtitle = "Edit relationship info",
            onClick = { onNavigate("/app/settings/relationship") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tools section
        Text(
            text = "Tools",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MoreMenuItem(
            icon = ThIcons.CloudUpload,
            title = "Import Data",
            subtitle = "Import notes and reminders",
            onClick = { onNavigate("/app/settings/import") }
        )

        MoreMenuItem(
            icon = ThIcons.Storage,
            title = "Storage",
            subtitle = "Manage app data and cache",
            onClick = { onNavigate("/app/settings/storage") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // About section
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MoreMenuItem(
            icon = ThIcons.Info,
            title = "About TwoHearts",
            subtitle = "Version, credits, and more",
            onClick = { onNavigate("/app/about") }
        )
    }
}

/**
 * MoreMenuItem — menu item with icon, title, and subtitle.
 */
@Composable
fun MoreMenuItem(
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Icon(
            imageVector = ThIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    }
}
