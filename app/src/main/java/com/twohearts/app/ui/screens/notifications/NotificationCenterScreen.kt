package com.twohearts.app.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.repository.NotificationCenterRepository
import com.twohearts.app.services.datetime.DateTimeHelper

/**
 * NotificationCenterScreen — notification center.
 *
 * Matches legacy NotificationCenter with:
 * - List of all notifications
 * - Mark as read / Mark all as read
 * - Unread count indicator
 * - Notification kind-based styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    notificationCenterRepository: NotificationCenterRepository,
    onBack: () -> Unit
) {
    val notifications by notificationCenterRepository.observeAll().collectAsState(initial = emptyList())
    val unreadCount by notificationCenterRepository.observeUnreadCount().collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notifications")
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("$unreadCount")
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                kotlinx.coroutines.runBlocking {
                                    notificationCenterRepository.markAllAsRead()
                                }
                            }
                        ) {
                            Text("Mark all read")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Reminders and anniversaries will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkAsRead = {
                            kotlinx.coroutines.runBlocking {
                                notificationCenterRepository.markAsRead(notification.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationCenterEntry,
    onMarkAsRead: () -> Unit
) {
    val backgroundColor = if (!notification.read) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMarkAsRead() }
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(top = 6.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = formatNotificationTime(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.kind.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = getKindColor(notification.kind)
                    )
                    if (notification.originFeature.isNotBlank()) {
                        Text(
                            text = " • ${notification.originFeature}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getKindColor(kind: String): Color {
    return when (kind.lowercase()) {
        "reminder" -> Color(0xFF4F7A5A) // Green
        "anniversary" -> Color(0xFFB07A1E) // Gold
        "system" -> Color(0xFF6B5D58) // Gray
        else -> Color(0xFF6B5D58)
    }
}

private fun formatNotificationTime(isoTimestamp: String): String {
    return try {
        // Simple time formatting - in production would use proper date library
        val instant = java.time.Instant.parse(isoTimestamp)
        val now = java.time.Instant.now()
        val diff = java.time.Duration.between(instant, now)

        when {
            diff.toMinutes() < 1 -> "Just now"
            diff.toMinutes() < 60 -> "${diff.toMinutes()}m ago"
            diff.toHours() < 24 -> "${diff.toHours()}h ago"
            diff.toDays() < 7 -> "${diff.toDays()}d ago"
            else -> isoTimestamp.substring(0, 10)
        }
    } catch (e: Exception) {
        isoTimestamp.substring(0, 10)
    }
}
