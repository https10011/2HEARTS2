package com.twohearts.app.ui.screens.notifications

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.repository.NotificationCenterRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import kotlinx.coroutines.launch

@Composable
fun NotificationCenterScreen(
    notificationCenterRepository: NotificationCenterRepository,
    onBack: () -> Unit,
    onOpenDestination: (String) -> Unit = {},
) {
    val notifications by remember(notificationCenterRepository) { notificationCenterRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadCount = notifications.count { !it.read }
    val scope = rememberCoroutineScope()
    val colors = LocalTwoHeartsColors.current

    Scaffold(topBar = {
        ThTopBar(title = "Notifications", onBack = onBack, titleBadge = unreadCount.takeIf { it > 0 }?.toString())
    }) { insets ->
        if (notifications.isEmpty()) {
            ThEmptyState(
                title = "You’re all caught up",
                modifier = Modifier.padding(insets).padding(top = 32.dp),
                description = "New alerts from TwoHearts will appear here.",
                visual = { Icon(ThIcons.BellNone, null, tint = colors.roseDeep) },
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(insets),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (unreadCount > 0) item {
                    TextButton(onClick = { scope.launch { notificationCenterRepository.markAllAsRead() } }) {
                        Text("Mark all as read")
                    }
                }
                items(notifications, key = { it.id }) { item ->
                    val destination = notificationDestination(item)
                    Surface(
                        color = if (item.read) MaterialTheme.colorScheme.surface else colors.surfaceWarm,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text(notificationKindLabel(item.kind), style = MaterialTheme.typography.labelMedium, color = colors.roseDeep)
                                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                                }
                                if (!item.read) Text("Unread", style = MaterialTheme.typography.labelMedium, color = colors.roseDeep)
                            }
                            if (item.body.isNotBlank()) Text(item.body, color = colors.textSecondary)
                            Text(
                                DateTimeHelper.formatShortDisplay(item.createdAt),
                                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
                            )
                            if (destination != null || !item.read) TextButton(onClick = {
                                scope.launch {
                                    if (!item.read) notificationCenterRepository.markAsRead(item.id)
                                    if (destination != null) onOpenDestination(destination)
                                }
                            }) {
                                Text(if (destination == null) "Mark as read" else "Open")
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

internal fun notificationDestination(entry: NotificationCenterEntry): String? = when (entry.originFeature) {
    "reminders" -> entry.originId?.let { "${RoutePath.APP_REMINDERS}/${Uri.encode(it)}" } ?: RoutePath.APP_REMINDERS
    "important-dates" -> RoutePath.APP_IMPORTANT_DATES
    "mood" -> RoutePath.APP_MOOD
    else -> null
}

private fun notificationKindLabel(kind: String): String = when (kind) {
    "reminder" -> "Reminder"
    "anniversary" -> "Anniversary"
    "mood" -> "Mood"
    else -> "TwoHearts"
}
