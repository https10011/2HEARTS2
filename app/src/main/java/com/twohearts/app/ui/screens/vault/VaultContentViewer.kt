package com.twohearts.app.ui.screens.vault

import android.net.Uri
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.UtilityPage
import com.twohearts.app.ui.components.VideoPreviewDialog
import com.twohearts.app.ui.screens.memories.MemoryAttachmentPreview
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
import com.twohearts.app.ui.screens.memories.MemoryMediaSource
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun VaultContentViewer(
    item: VaultItem,
    onBack: () -> Unit,
    onDelete: (VaultItem) -> Unit,
    modifier: Modifier = Modifier,
    asset: MediaAsset? = null,
    onExport: suspend (Uri) -> Unit = {},
    deleting: Boolean = false,
    errorMessage: String? = null,
) {
    val type = vaultContentType(item.contentType)
    var confirmingDelete by remember(item.id) { mutableStateOf(false) }
    var confirmingExport by remember(item.id) { mutableStateOf(false) }
    var previewing by remember(item.id) { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf<Pair<StatusVariant, String>?>(null) }
    val scope = rememberCoroutineScope()
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(asset?.mimeType ?: "application/octet-stream"),
    ) { destination ->
        if (destination != null && !exporting) {
            exporting = true
            exportStatus = null
            scope.launch {
                try {
                    onExport(destination)
                    exportStatus = StatusVariant.SUCCESS to "Copy saved."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    exportStatus = StatusVariant.ERROR to "Couldn’t save this copy."
                } finally {
                    exporting = false
                }
            }
        }
    }
    val title = item.title?.takeIf(String::isNotBlank) ?: "Untitled item"
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing

    BackHandler(exporting || deleting) {}
    UtilityPage("Private Vault", { if (!exporting && !deleting) onBack() }, modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            ThEyebrow(type.displayName, color = colors.roseDeep)
            Text(title, style = MaterialTheme.typography.displayMedium, modifier = Modifier.semantics { heading() })
            Text(
                "Saved ${DateTimeHelper.formatDisplay(item.createdAt)}",
                style = MaterialTheme.typography.bodySmall, color = colors.textSecondary,
            )
        }
        when {
            type == VaultContentType.NOTE -> Text(
                item.contentText.orEmpty().ifBlank { "This note is empty." }, style = ThTextStyles.reading,
            )
            asset == null -> ThStatusBanner(StatusVariant.INFO, "This attachment is unavailable.")
            type == VaultContentType.PHOTO -> MemoryMediaFrame(
                MemoryMediaSource(asset),
                Modifier.fillMaxWidth().aspectRatio(1.4f).semantics { contentDescription = "Vault photo" }
                    .clickable(enabled = !deleting, onClickLabel = "View photo", role = Role.Button) { previewing = true },
            )
            type == VaultContentType.VIDEO -> ThButton(
                { previewing = true }, icon = ThIcons.Play, text = "Play video", enabled = !deleting,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                Text(Formatter.formatShortFileSize(LocalContext.current, asset.fileSize), color = colors.textSecondary)
                ThButton(
                    { confirmingExport = true }, text = "Save a copy", loading = exporting, enabled = !deleting,
                    icon = ThIcons.File, variant = ButtonVariant.SECONDARY,
                )
            }
        }
        exportStatus?.let { (variant, message) -> ThStatusBanner(variant, message) }
        errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
        ThDivider()
        ThButton(
            { confirmingDelete = true }, icon = ThIcons.Trash, variant = ButtonVariant.DANGER,
            enabled = !exporting, loading = deleting, text = "Delete from Vault",
        )
    }
    if (previewing && asset != null) {
        if (type == VaultContentType.VIDEO) VideoPreviewDialog(asset.filePath) { previewing = false }
        if (type == VaultContentType.PHOTO) MemoryAttachmentPreview(MemoryMediaSource(asset)) { previewing = false }
    }
    if (confirmingExport) VaultDecision(
        "Save a separate copy?", "Anyone with access to the destination can open the exported file.",
        confirmText = "Choose location", variant = ButtonVariant.PRIMARY, onConfirm = {
            confirmingExport = false
            exporter.launch(title.replace('/', '_').replace('\\', '_'))
        },
        onDismiss = { confirmingExport = false },
    )
    if (confirmingDelete) VaultDecision(
        "Delete this vault item?", "This item will be removed from your vault.",
        confirmText = "Delete", variant = ButtonVariant.DANGER, onConfirm = { confirmingDelete = false; onDelete(item) },
        onDismiss = { confirmingDelete = false },
    )
}

@Composable
private fun VaultDecision(
    title: String, message: String, confirmText: String, variant: ButtonVariant,
    onConfirm: () -> Unit, onDismiss: () -> Unit,
) {
    ThModal(open = true, onClose = onDismiss, label = title) {
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        Text(
            message, color = LocalTwoHeartsColors.current.textSecondary,
            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
                .padding(vertical = TwoHeartsTokens.Spacing.space3),
        )
        ThButton(onConfirm, text = confirmText, full = true, variant = variant)
        ThButton(onDismiss, text = "Cancel", variant = ButtonVariant.GHOST)
    }
}
