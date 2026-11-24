package com.twohearts.app.ui.screens.notifications

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.entity.NotificationCenterEntry
import com.twohearts.app.data.repository.NotificationCenterRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.NotificationPermissionAction
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.components.rememberNotificationPermissionControl
import com.twohearts.app.ui.navigation.RoutePath
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun NotificationCenterScreen(
    notificationCenterRepository: NotificationCenterRepository,
    onBack: () -> Unit,
    onOpenDestination: (String) -> Unit = {},
    onOpenSettings: (() -> Unit)? = null,
) {
    val notifications by remember(notificationCenterRepository) { notificationCenterRepository.observeAll() }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val access = rememberNotificationPermissionControl()
    val scope = rememberCoroutineScope()
    NotificationCenterContent(
        notifications, access.action == NotificationPermissionAction.ALLOWED, onBack, onOpenSettings,
        markAll = { scope.launch { notificationCenterRepository.markAllAsRead() } },
        openAlert = { item, destination ->
            scope.launch {
                if (!item.read) notificationCenterRepository.markAsRead(item.id)
                destination?.let(onOpenDestination)
            }
        },
    )
}

@Composable
internal fun NotificationCenterContent(
    notifications: List<NotificationCenterEntry>,
    canNotify: Boolean,
    onBack: () -> Unit,
    onOpenSettings: (() -> Unit)?,
    markAll: () -> Unit,
    openAlert: (NotificationCenterEntry, String?) -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    val unread = notifications.count { !it.read }
    val groups = remember(notifications, zone) { notifications.groupBy { localDeliveryTime(it.createdAt, zone)?.toLocalDate() } }
    Scaffold(topBar = {
        ThTopBar("Notifications", onBack = onBack, actions = {
            if (onOpenSettings != null) {
                ThIconButton(onOpenSettings, label = "Notification settings") { Icon(ThIcons.Settings, null) }
            }
        })
    }) { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            if (!canNotify) item {
                Row(
                    Modifier.fillMaxWidth().background(colors.surfaceWarm, MaterialTheme.shapes.medium).padding(spacing.space3),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                ) {
                    Icon(ThIcons.BellOff, null, tint = colors.roseDeep)
                    Text("Android notifications are off", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
            if (notifications.isEmpty()) item {
                ThEmptyState(
                    title = "You’re all caught up", description = "Delivered alerts will appear here.",
                    visual = { Icon(ThIcons.BellNone, null, tint = colors.roseDeep) },
                )
            }
            if (unread > 0) item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                    Text(if (unread == 1) "1 unread" else "$unread unread", style = MaterialTheme.typography.headlineSmall)
                    ThButton(markAll, text = "Mark all as read", variant = ButtonVariant.GHOST, icon = ThIcons.Check)
                }
            }
            for ((day, entries) in groups) {
                item {
                    Text(
                        when (day) {
                            today -> "Today"
                            today.minusDays(1) -> "Yesterday"
                            null -> "Undated alerts"
                            else -> DateTimeHelper.formatDisplay(day.toString())
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.space2).semantics { heading() },
                    )
                }
                items(entries, key = { it.id }) { item ->
                    val destination = notificationDestination(item)
                    AlertRow(item, destination, zone, { openAlert(item, destination) })
                    ThDivider()
                }
            }
        }
    }
}

@Composable
private fun AlertRow(item: NotificationCenterEntry, destination: String?, zone: ZoneId, onOpen: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    val accent = if (item.read) colors.borderStrong else colors.roseDeep
    Row(
        Modifier.fillMaxWidth().padding(vertical = spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        Box(Modifier.padding(top = spacing.space2).size(8.dp).background(accent, CircleShape))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
            val kind = when (item.kind) {
                "reminder" -> "Reminder"
                "anniversary" -> "Anniversary"
                "mood" -> "Mood"
                else -> "TwoHearts"
            }
            ThEyebrow(if (item.read) kind else "$kind · Unread", color = colors.roseDeep)
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            if (item.body.isNotBlank()) Text(item.body, color = colors.textSecondary)
            localDeliveryTime(item.createdAt, zone)?.let {
                Text(it.format(alertTimeFormatter), color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
            if (destination != null || !item.read) {
                ThButton(
                    onOpen, text = if (destination == null) "Mark as read" else "Open",
                    variant = ButtonVariant.GHOST, icon = if (destination == null) ThIcons.Check else ThIcons.ChevronRight,
                )
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

private val alertTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun localDeliveryTime(value: String, zone: ZoneId): LocalDateTime? =
    runCatching { Instant.parse(value).atZone(zone).toLocalDateTime() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value) }.getOrNull()
