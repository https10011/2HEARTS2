package com.twohearts.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.datamanagement.StorageReport
import com.twohearts.app.ui.components.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StorageSettingsScreen(dataManagementService: DataManagementService, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<StorageReport?>(null) }
    var working by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<Pair<StatusVariant, String>?>(null) }

    suspend fun refresh() {
        report = withContext(Dispatchers.IO) { dataManagementService.getStorageReport() }
    }

    LaunchedEffect(dataManagementService) {
        try {
            refresh()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            status = StatusVariant.ERROR to "Storage usage is unavailable."
        }
    }

    Scaffold(topBar = { ThTopBar(title = "Storage", onBack = onBack) }) { insets ->
        Column(
            Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val current = report
            if (current != null) {
                Text("Used on this device", style = MaterialTheme.typography.titleMedium)
                Text(android.text.format.Formatter.formatFileSize(context, current.totalAppSize), style = MaterialTheme.typography.headlineMedium)
                Text("Media · " + android.text.format.Formatter.formatFileSize(context, current.mediaBytes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Saved records", style = MaterialTheme.typography.titleMedium)
                listOf(
                    "Profiles" to current.profileCount, "Memories" to current.memoryCount, "Notes" to current.noteCount,
                    "Timeline events" to current.timelineEventCount, "Reminders" to current.reminderCount,
                    "Places" to current.placeCount, "Mood entries" to current.moodEntryCount,
                    "Period entries" to current.periodEntryCount, "Vault items" to current.vaultItemCount,
                    "Notifications" to current.notificationCount, "Media files" to current.mediaAssetCount,
                ).forEach { (label, count) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(label, Modifier.weight(1f))
                        Text(count.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (status == null) {
                Text("Reading storage usage…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            status?.let { (variant, message) -> ThStatusBanner(variant, message) }
            HorizontalDivider()
            Text("Clear temporary files", style = MaterialTheme.typography.titleMedium)
            Text("Removes unused media files. Saved content stays in TwoHearts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ThButton(
                onClick = {
                    working = true
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) { dataManagementService.clearCache() }
                            refresh()
                            status = StatusVariant.SUCCESS to "Removed ${result.orphanFilesRemoved} unused files."
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            status = StatusVariant.ERROR to "Couldn’t clear temporary files."
                        } finally {
                            working = false
                        }
                    }
                },
                variant = ButtonVariant.SECONDARY, loading = working, text = "Clear temporary files",
            )
            HorizontalDivider()
            Text("Reset TwoHearts", style = MaterialTheme.typography.titleMedium)
            Text("Permanently erases the app’s local records, media, and settings. The app closes after the reset.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ThButton(
                onClick = { confirming = true }, enabled = !working, variant = ButtonVariant.DANGER,
                text = "Reset all app data",
            )
        }
    }

    if (confirming) {
        ConfirmDialog(
            title = "Erase all TwoHearts data?",
            message = "This permanently removes both profiles, saved records, media, and settings from this device.",
            confirmText = "Erase all data",
            onDismiss = { confirming = false },
            onConfirm = {
                confirming = false
                if (!dataManagementService.requestFullReset()) {
                    status = StatusVariant.ERROR to "Android couldn’t reset TwoHearts."
                }
            },
        )
    }
}
