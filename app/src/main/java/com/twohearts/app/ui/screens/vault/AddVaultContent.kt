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
import com.twohearts.app.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddVaultContent(
    onBack: () -> Unit,
    onContentAdded: (title: String, contentType: VaultContentType, content: String?) -> Unit,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(VaultContentType.NOTE) }
    var attachment by remember { mutableStateOf<Uri?>(null) }
    var note by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) attachment = uri
    }
    val payload = if (type == VaultContentType.NOTE) note.trim() else attachment?.toString()
    Scaffold(modifier, topBar = { ThTopBar(title = "Add to Vault", onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("What would you like to keep?", style = MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VaultContentType.entries.forEach { option ->
                    ThChoiceChip(
                        selected = type == option,
                        onClick = {
                            if (type != option) attachment = null
                            type = option
                        },
                        enabled = !saving,
                        leadingIcon = { Icon(option.icon, null, Modifier.size(18.dp)) },
                        label = { Text(option.displayName) },
                    )
                }
            }
            ThInput(title, { title = it }, label = "Title", enabled = !saving)
            if (type == VaultContentType.NOTE) {
                ThInput(note, { note = it }, multiline = true, label = "Private note", enabled = !saving)
            } else {
                ThButton(
                    onClick = {
                        picker.launch(when (type) {
                            VaultContentType.PHOTO -> "image/*"
                            VaultContentType.VIDEO -> "video/*"
                            else -> "*/*"
                        })
                    },
                    variant = ButtonVariant.SECONDARY, text = if (attachment == null) "Choose a file" else "Replace file",
                    enabled = !saving, icon = ThIcons.Attach,
                )
                if (attachment != null) Text("File selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ThButton(
                onClick = { onContentAdded(title.trim(), type, payload) },
                enabled = title.isNotBlank() && !payload.isNullOrBlank(),
                loading = saving, full = true, text = "Save to Vault",
            )
        }
    }
}
