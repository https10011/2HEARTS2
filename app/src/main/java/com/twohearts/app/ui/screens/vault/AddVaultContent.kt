package com.twohearts.app.ui.screens.vault

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.RecordEditorLayout
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThChoiceChip
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.screens.memories.MemoryMediaFrame
import com.twohearts.app.ui.screens.memories.MemoryMediaSource
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddVaultContent(
    onBack: () -> Unit,
    onContentAdded: (title: String, contentType: VaultContentType, content: String?) -> Unit,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
    errorMessage: String? = null,
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(VaultContentType.NOTE) }
    var attachment by remember { mutableStateOf<Uri?>(null) }
    var note by remember { mutableStateOf("") }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) attachment = uri
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) attachment = uri
    }
    val payload = if (type == VaultContentType.NOTE) note.trim() else attachment?.toString()
    val valid = title.isNotBlank() && !payload.isNullOrBlank()
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current

    RecordEditorLayout(
        "Add to Vault", "Save to Vault", onSave = { if (valid && !saving) onContentAdded(title.trim(), type, payload) },
        onBack = onBack, saving = saving, errorMessage = errorMessage, saveEnabled = valid, modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            ThEyebrow("Item type", color = colors.roseDeep)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                listOf(VaultContentType.NOTE, VaultContentType.PHOTO, VaultContentType.VIDEO, VaultContentType.FILE).forEach { option ->
                    ThChoiceChip(
                        type == option, onClick = {
                            if (type != option) attachment = null
                            type = option
                        },
                        enabled = !saving,
                        leadingIcon = { Icon(option.icon, null) },
                        label = { Text(option.displayName) },
                    )
                }
            }
        }
        ThInput(title, { title = it }, label = "Title", enabled = !saving)
        if (type == VaultContentType.NOTE) {
            ThInput(note, { note = it }, label = "Private note", multiline = true, enabled = !saving)
        } else {
            val chosen = attachment
            if (chosen != null && type == VaultContentType.PHOTO) {
                MemoryMediaFrame(MemoryMediaSource(uri = chosen, mimeType = "image/*"), Modifier.fillMaxWidth().aspectRatio(1.65f))
            }
            if (chosen != null) {
                val resolver = LocalContext.current.contentResolver
                val name by produceState<String?>(null, chosen) {
                    value = withContext(Dispatchers.IO) {
                        runCatching {
                            resolver.query(chosen, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                                ?.use { if (it.moveToFirst()) it.getString(0) else null }
                        }.getOrNull()
                    }
                }
                Text(name ?: "${type.displayName} selected", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            ThButton(
                onClick = {
                    when (type) {
                        VaultContentType.PHOTO -> mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        VaultContentType.VIDEO -> mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        VaultContentType.FILE -> filePicker.launch(arrayOf("*/*"))
                        VaultContentType.NOTE -> Unit
                    }
                },
                variant = ButtonVariant.SECONDARY, icon = ThIcons.Attach, enabled = !saving,
                text = if (chosen == null) "Choose ${type.displayName.lowercase()}" else "Replace attachment",
            )
            if (chosen != null) ThButton(
                { attachment = null }, text = "Remove selection", variant = ButtonVariant.GHOST, enabled = !saving,
            )
        }
    }
}
