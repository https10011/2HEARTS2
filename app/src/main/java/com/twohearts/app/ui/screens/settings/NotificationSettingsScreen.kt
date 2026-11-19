package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.settings.AppSettings
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(settingsStorage: SettingsStorage, onBack: () -> Unit) {
    val settings by settingsStorage.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    val permission = rememberNotificationPermissionControl()

    Scaffold(topBar = { ThTopBar(title = "Notifications", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NotificationPreference(
                "TwoHearts notifications", "Control whether TwoHearts can send alerts.",
                checked = settings.notificationsEnabled,
                onChange = { enabled -> scope.launch { settingsStorage.setNotificationsEnabled(enabled) } },
            )
            HorizontalDivider()
            NotificationPreference(
                "Reminders", "Allow alerts for scheduled reminders.",
                checked = settings.remindersEnabled, enabled = settings.notificationsEnabled,
                onChange = { enabled -> scope.launch { settingsStorage.setRemindersEnabled(enabled) } },
            )
            if (permission.action != NotificationPermissionAction.ALLOWED) {
                ThStatusBanner(StatusVariant.INFO, "Android notifications are off for TwoHearts.")
                if (permission.action == NotificationPermissionAction.REQUEST) {
                    ThButton(
                        onClick = permission.request,
                        text = "Allow Android alerts",
                    )
                }
            }
            ThButton(
                onClick = permission.openSettings,
                variant = ButtonVariant.SECONDARY, text = "Android notification settings",
            )
        }
    }
}

@Composable
private fun NotificationPreference(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 60.dp)
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}
