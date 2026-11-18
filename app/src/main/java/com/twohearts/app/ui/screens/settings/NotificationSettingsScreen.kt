package com.twohearts.app.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.settings.AppSettings
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(settingsStorage: SettingsStorage, onBack: () -> Unit) {
    val settings by settingsStorage.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val manager = remember(context) { NotificationManagerCompat.from(context) }
    var allowed by remember { mutableStateOf(manager.areNotificationsEnabled()) }
    DisposableEffect(owner, manager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) allowed = manager.areNotificationsEnabled()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

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
            if (!allowed) ThStatusBanner(StatusVariant.INFO, "Android notifications are off for TwoHearts.")
            ThButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    context.startActivity(intent)
                },
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
