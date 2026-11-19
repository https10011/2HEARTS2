package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.NotificationPermissionAction
import com.twohearts.app.ui.components.NotificationPermissionControl
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThQuietButton
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@Composable
internal fun CapabilitiesSetupScreen(
    notificationAccess: NotificationPermissionControl,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val allowed = notificationAccess.action == NotificationPermissionAction.ALLOWED
    OnboardingLayout(
        stage = OnboardingStage.CAPABILITIES,
        title = "Device access",
        onBack = onBack,
        primaryLabel = if (allowed) "Continue" else "Continue without alerts",
        onPrimary = onNext,
        secondary = when (notificationAccess.action) {
            NotificationPermissionAction.ALLOWED -> null
            NotificationPermissionAction.REQUEST -> {
                { ThQuietButton(notificationAccess.request, "Allow alerts") }
            }
            NotificationPermissionAction.SETTINGS -> {
                { ThQuietButton(notificationAccess.openSettings, "Android settings") }
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Capability(
                ThIcons.Bell, "Notifications",
                "Alerts for reminders and important dates.",
            )
            ThStatusBanner(
                if (allowed) StatusVariant.SUCCESS else StatusVariant.INFO,
                if (allowed) "Alerts are enabled." else "Alerts are off.",
            )
            ThDivider()
            Capability(
                ThIcons.PhotoLibrary, "Photos",
                "Optional. Android shares only the photos you choose.",
            )
            ThDivider()
            Capability(
                ThIcons.Alarm, "Reminder timing",
                "Android can delay background reminders while saving battery.",
            )
        }
    }
}

@Composable
private fun Capability(icon: ImageVector, title: String, description: String) {
    val colors = LocalTwoHeartsColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = colors.burgundy, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
    }
}
