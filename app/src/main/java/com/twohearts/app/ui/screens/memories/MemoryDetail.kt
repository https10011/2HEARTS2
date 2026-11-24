package com.twohearts.app.ui.screens.memories

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ConfirmDialog
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThSectionHeader
import com.twohearts.app.ui.components.ThStatusBanner
import com.twohearts.app.ui.components.StatusVariant
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun MemoryDetail(
    memory: Memory, onEdit: () -> Unit, onDelete: () -> Unit, onBack: () -> Unit,
    media: List<MediaAsset> = emptyList(), deleting: Boolean = false, errorMessage: String? = null,
) {
    var confirmDelete by rememberSaveable(memory.id) { mutableStateOf(false) }
    var previewId by rememberSaveable(memory.id) { mutableStateOf<String?>(null) }
    var selectedId by rememberSaveable(memory.id) { mutableStateOf<String?>(null) }
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    Scaffold(topBar = {
        ThTopBar("Memory", onBack = onBack, actions = {
            ThIconButton(onClick = onEdit, label = "Edit memory", enabled = !deleting) {
                Icon(ThIcons.Edit, null, tint = MaterialTheme.colorScheme.primary)
            }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenGutter, vertical = spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space5),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                Text(
                    DateTimeHelper.formatDisplay(memory.memoryDate),
                    style = MaterialTheme.typography.labelLarge, color = colors.roseDeep,
                )
                Text(memory.title, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            }
            val selected = media.firstOrNull { it.id == selectedId } ?: media.firstOrNull()
            if (selected != null) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    ThSectionHeader(attachmentCount(media.size))
                    val source = MemoryMediaSource(selected)
                    val label = if (source.isVideo) "Play video" else "View photo"
                    Column(
                        Modifier.fillMaxWidth().clickable(enabled = !deleting, onClickLabel = label, role = Role.Button) {
                            previewId = selected.id
                        },
                        verticalArrangement = Arrangement.spacedBy(spacing.space1),
                    ) {
                        MemoryMediaFrame(source, Modifier.fillMaxWidth().height(194.dp))
                        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    if (media.size > 1) {
                        LazyRow(
                            modifier = Modifier.selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                        ) {
                            itemsIndexed(media, key = { _, item -> item.id }) { index, asset ->
                                val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)
                                val active = asset.id == selected.id
                                val thumbnail = MemoryMediaSource(asset)
                                val kind = if (thumbnail.isVideo) "Video" else "Photo"
                                MemoryMediaFrame(
                                    thumbnail,
                                    Modifier.size(60.dp).clip(shape)
                                        .selectable(active, enabled = !deleting, role = Role.Tab, onClick = { selectedId = asset.id })
                                        .semantics { contentDescription = "$kind ${index + 1}" }
                                        .border(
                                            if (active) TwoHeartsTokens.Border.strong else TwoHeartsTokens.Border.hairline,
                                            if (active) MaterialTheme.colorScheme.primary else colors.borderSubtle,
                                            shape,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
            memory.caption?.takeIf(String::isNotBlank)?.let {
                Text(it, style = ThTextStyles.reading, color = colors.textSecondary)
            }
            ThDivider()
            errorMessage?.let { ThStatusBanner(StatusVariant.ERROR, it) }
            ThButton(
                onClick = { confirmDelete = true }, variant = ButtonVariant.DANGER,
                icon = ThIcons.Trash, text = "Delete memory", loading = deleting,
            )
        }
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete this memory?",
            message = "The memory and its attachments will be removed from your story.",
            confirmText = "Delete",
            onConfirm = { confirmDelete = false; onDelete() },
            onDismiss = { confirmDelete = false },
        )
    }
    media.firstOrNull { it.id == previewId }?.let { MemoryAttachmentPreview(MemoryMediaSource(it)) { previewId = null } }
}
