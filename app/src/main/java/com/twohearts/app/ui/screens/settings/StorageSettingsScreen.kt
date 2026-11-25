package com.twohearts.app.ui.screens.settings

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.datamanagement.StorageReport
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.UtilityGroup
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StorageSettingsScreen(dataManagementService: DataManagementService, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<StorageReport?>(null) }
    var working by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf<Pair<StatusVariant, String>?>(null) }

    LaunchedEffect(dataManagementService, refresh) {
        loadFailed = false
        try {
            report = withContext(Dispatchers.IO) { dataManagementService.getStorageReport() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadFailed = true
        }
    }

    BackHandler(working) {}
    StorageSettingsContent(
        report, working, loadFailed, status, onBack = { if (!working) onBack() },
        retry = { refresh++ },
        clear = {
            if (!working) {
                working = true
                status = null
                scope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) { dataManagementService.clearCache() }
                        status = StatusVariant.SUCCESS to "Removed ${result.orphanFilesRemoved} unused files."
                        refresh++
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        status = StatusVariant.ERROR to "Couldn’t clear temporary files."
                    } finally {
                        working = false
                    }
                }
            }
        },
        reset = {
            if (!dataManagementService.requestFullReset()) status = StatusVariant.ERROR to "Android couldn’t reset TwoHearts."
        },
    )
}

@Composable
internal fun StorageSettingsContent(
    report: StorageReport?, working: Boolean, loadFailed: Boolean, status: Pair<StatusVariant, String>?,
    onBack: () -> Unit, retry: () -> Unit, clear: () -> Unit, reset: () -> Unit,
) {
    var showingCounts by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing

    UtilityPage("Storage", onBack) {
        if (report != null) {
            Surface(shape = MaterialTheme.shapes.large, color = colors.surfaceWarm, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(spacing.space4), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ThEyebrow("Used on this device", color = colors.roseDeep)
                    Text(Formatter.formatFileSize(context, report.totalAppSize), style = MaterialTheme.typography.displayMedium)
                    Text("Media · ${Formatter.formatFileSize(context, report.mediaBytes)}", color = colors.textSecondary)
                }
            }
            UtilityGroup("Saved records") {
                ThButton(
                    { showingCounts = !showingCounts }, text = if (showingCounts) "Hide record counts" else "Show record counts",
                    variant = ButtonVariant.GHOST, icon = if (showingCounts) ThIcons.Check else ThIcons.ChevronRight,
                )
                if (showingCounts) listOf(
                    "Profiles" to report.profileCount, "Memories" to report.memoryCount, "Notes" to report.noteCount,
                    "Timeline events" to report.timelineEventCount, "Reminders" to report.reminderCount,
                    "Places" to report.placeCount, "Mood entries" to report.moodEntryCount, "Period entries" to report.periodEntryCount,
                    "Vault items" to report.vaultItemCount, "Notifications" to report.notificationCount, "Media files" to report.mediaAssetCount,
                ).forEach { (name, count) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = spacing.space1),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
                    ) {
                        Text(name, Modifier.weight(1f))
                        Text(count.toString(), color = colors.textSecondary)
                    }
                }
            }
        } else if (!loadFailed) {
            Text("Reading storage usage…", color = colors.textSecondary)
        }
        if (loadFailed) {
            ThStatusBanner(StatusVariant.ERROR, "Storage usage is unavailable.")
            ThButton(retry, text = "Try again", variant = ButtonVariant.GHOST)
        }
        status?.let { (variant, message) -> ThStatusBanner(variant, message) }
        UtilityGroup("Temporary files") {
            Text(
                "Clear media files that no saved record uses.",
                modifier = Modifier.padding(top = spacing.space3), color = colors.textSecondary,
            )
            ThButton(
                { confirm = "clear" }, text = "Clear temporary files", variant = ButtonVariant.SECONDARY,
                loading = working, modifier = Modifier.padding(top = spacing.space2),
            )
        }
        UtilityGroup("Reset TwoHearts") {
            Text(
                "Erase local records, media, and settings. Android closes TwoHearts after the reset.",
                modifier = Modifier.padding(top = spacing.space3), color = colors.textSecondary,
            )
            ThButton(
                { confirm = "reset" }, text = "Reset all app data", variant = ButtonVariant.DANGER,
                icon = ThIcons.Trash, enabled = !working, modifier = Modifier.padding(top = spacing.space2),
            )
        }
    }

    when (confirm) {
        "clear" -> ConfirmDialog(
            title = "Clear temporary files?", message = "TwoHearts will remove unused media files. Saved records and their files stay here.",
            confirmText = "Clear files", onDismiss = { confirm = null }, onConfirm = { confirm = null; clear() },
        )
        "reset" -> ConfirmDialog(
            title = "Erase all TwoHearts data?",
            message = "This permanently removes both profiles, saved records, media, and settings from this device.",
            confirmText = "Erase all data", onDismiss = { confirm = null }, onConfirm = { confirm = null; reset() },
        )
    }
}
