package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twohearts.app.data.settings.AppSettings
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.NotificationPermissionAction
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.components.rememberNotificationPermissionControl
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(settingsStorage: SettingsStorage, onBack: () -> Unit) {
    val settings by settingsStorage.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    val permission = rememberNotificationPermissionControl()
    NotificationSettingsContent(
        settings, permission.action, onBack, permission.openSettings, permission.request,
        onAllowed = { enabled -> scope.launch { settingsStorage.setNotificationsEnabled(enabled) } },
        onReminders = { enabled -> scope.launch { settingsStorage.setRemindersEnabled(enabled) } },
    )
}

@Composable
internal fun NotificationSettingsContent(
    settings: AppSettings, permission: NotificationPermissionAction, onBack: () -> Unit,
    openAndroid: () -> Unit, requestAndroid: () -> Unit, onAllowed: (Boolean) -> Unit, onReminders: (Boolean) -> Unit,
) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    val allowed = permission == NotificationPermissionAction.ALLOWED
    UtilityPage("Notifications", onBack) {
        Surface(shape = MaterialTheme.shapes.large, color = colors.surfaceWarm, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                ThEyebrow("Device access", color = colors.roseDeep)
                Text(
                    if (allowed) "Android alerts allowed" else "Android alerts are off",
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (!allowed) Text(
                    "Turn on Android alerts to receive scheduled reminders.",
                    style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary,
                )
                if (permission == NotificationPermissionAction.REQUEST) {
                    ThButton(requestAndroid, text = "Allow Android alerts", icon = ThIcons.Bell)
                }
                ThButton(openAndroid, text = "Android notification settings", variant = ButtonVariant.GHOST, icon = ThIcons.Settings)
            }
        }
        UtilityGroup("TwoHearts preferences") {
            UtilityToggle(
                "TwoHearts notifications", "Allow TwoHearts to send alerts on this device.",
                settings.notificationsEnabled, onChange = onAllowed,
            )
            ThDivider()
            UtilityToggle(
                "Reminders", "Alert at the time you set for a reminder.",
                settings.remindersEnabled, enabled = settings.notificationsEnabled, onChange = onReminders,
            )
        }
    }
}
