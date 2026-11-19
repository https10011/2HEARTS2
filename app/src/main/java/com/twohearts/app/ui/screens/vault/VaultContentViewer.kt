package com.twohearts.app.ui.screens.vault

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
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
) {
    val type = VaultContentType.entries.firstOrNull { it.name.equals(item.contentType, true) } ?: VaultContentType.FILE
    var deleting by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val feedback = useToast()
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(asset?.mimeType ?: "application/octet-stream"),
    ) { destination ->
        if (destination != null) {
            exporting = true
            scope.launch {
                try {
                    onExport(destination)
                    feedback.success("Copy saved.")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    feedback.error("Couldn’t save this copy.")
                } finally {
                    exporting = false
                }
            }
        }
    }
    val title = item.title?.takeIf(String::isNotBlank) ?: "Vault item"
    Scaffold(modifier, topBar = { ThTopBar(title = "Private Vault", onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(type.displayName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (type == VaultContentType.NOTE) {
                Text(item.contentText.orEmpty().ifBlank { "This note is empty." }, style = MaterialTheme.typography.bodyLarge)
            } else if (asset == null) {
                ThStatusBanner(StatusVariant.INFO, "This attachment is unavailable.")
            } else when (type) {
                VaultContentType.PHOTO -> MemoryMediaFrame(asset, Modifier.fillMaxWidth().height(260.dp))
                VaultContentType.VIDEO -> ThButton(
                    { playing = true }, icon = ThIcons.Play, text = "Play video",
                )
                VaultContentType.FILE -> {
                    Text(android.text.format.Formatter.formatShortFileSize(androidx.compose.ui.platform.LocalContext.current, asset.fileSize))
                    ThButton(
                        { exporter.launch(title) }, loading = exporting,
                        variant = ButtonVariant.SECONDARY, text = "Save a copy",
                    )
                }
                VaultContentType.NOTE -> Unit
            }
            HorizontalDivider()
            ThButton(
                { deleting = true }, icon = ThIcons.Trash, variant = ButtonVariant.GHOST,
                enabled = !exporting, text = "Delete from Vault",
            )
        }
    }
    if (playing && asset != null) VideoPreviewDialog(asset.filePath) { playing = false }
    if (deleting) {
        ConfirmDialog(
            "Delete this vault item?", "This item will be removed from your vault.",
            confirmText = "Delete", onConfirm = { deleting = false; onDelete(item) },
            onDismiss = { deleting = false },
        )
    }
}
