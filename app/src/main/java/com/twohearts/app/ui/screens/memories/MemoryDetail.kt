package com.twohearts.app.ui.screens.memories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetail(memory: Memory, onEdit: () -> Unit, onDelete: () -> Unit, onBack: () -> Unit, media: List<MediaAsset> = emptyList()) {
    var confirmDelete by remember { mutableStateOf(false) }
    val colors = LocalTwoHeartsColors.current
    Scaffold(topBar = { Header(title = "Memory", onBack = onBack, actions = {
        IconButton(onClick = onEdit) { Icon(ThIcons.Edit, "Edit memory") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
            if (media.isEmpty()) MemoryMediaFrame(null, Modifier.fillMaxWidth().height(260.dp)) else LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(media, key = { it.id }) { MemoryMediaFrame(it, Modifier.width(300.dp).height(260.dp)) }
            }
            Column(Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
                Text(DateTimeHelper.formatDisplay(memory.memoryDate).uppercase(), style = MaterialTheme.typography.labelLarge, color = colors.roseDeep)
                Spacer(Modifier.height(10.dp)); Text(memory.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                memory.caption?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(18.dp)); Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary) }
                if (media.isNotEmpty()) { Spacer(Modifier.height(18.dp)); Text("${media.size} ${if (media.size == 1) "keepsake" else "keepsakes"}", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary) }
                Spacer(Modifier.height(32.dp))
                OutlinedButton(onClick = { confirmDelete = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(ThIcons.Trash, null); Spacer(Modifier.width(8.dp)); Text("Delete memory")
                }
            }
        }
    }
    if (confirmDelete) ConfirmDialog(title = "Delete this memory?", message = "The memory will be removed from your story. This cannot be undone.", confirmText = "Delete", onConfirm = { confirmDelete = false; onDelete() }, onDismiss = { confirmDelete = false })
}
