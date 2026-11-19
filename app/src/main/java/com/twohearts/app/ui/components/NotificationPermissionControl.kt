package com.twohearts.app.ui.components

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

internal enum class NotificationPermissionAction { ALLOWED, REQUEST, SETTINGS }

internal fun notificationPermissionAction(sdk: Int, allowed: Boolean, previouslyRequested: Boolean) = when {
    allowed -> NotificationPermissionAction.ALLOWED
    sdk >= Build.VERSION_CODES.TIRAMISU && !previouslyRequested -> NotificationPermissionAction.REQUEST
    else -> NotificationPermissionAction.SETTINGS
}

internal data class NotificationPermissionControl(
    val action: NotificationPermissionAction,
    val request: () -> Unit,
    val openSettings: () -> Unit,
)

@Composable
internal fun rememberNotificationPermissionControl(
    previouslyRequested: Boolean = false,
    onRequested: () -> Unit = {},
): NotificationPermissionControl {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val manager = remember(context) { NotificationManagerCompat.from(context) }
    var allowed by remember(manager) { mutableStateOf(manager.areNotificationsEnabled()) }
    var requestedHere by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        allowed = manager.areNotificationsEnabled()
    }
    DisposableEffect(lifecycle, manager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) allowed = manager.areNotificationsEnabled()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val action = notificationPermissionAction(Build.VERSION.SDK_INT, allowed, previouslyRequested || requestedHere)
    return NotificationPermissionControl(
        action = action,
        request = {
            if (action == NotificationPermissionAction.REQUEST && !requestedHere) {
                requestedHere = true
                onRequested()
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        openSettings = { openNotificationSettings(context) },
    )
}

private fun openNotificationSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
    } catch (_: ActivityNotFoundException) {
        val packageUri = Uri.fromParts("package", context.packageName, null)
        runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)) }
    }
}
