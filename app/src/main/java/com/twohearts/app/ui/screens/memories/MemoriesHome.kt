package com.twohearts.app.ui.screens.memories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.MediaAsset
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.CardTone
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThEyebrow
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThSectionHeader
import com.twohearts.app.ui.components.ThSurfaceCard
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun MemoriesHome(
    memories: List<Memory>,
    onMemoryClick: (String) -> Unit,
    onAddMemory: () -> Unit,
    onBack: () -> Unit,
    media: Map<String, List<MediaAsset>> = emptyMap(),
) {
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    Scaffold(
        topBar = {
            ThTopBar("Memories", onBack = onBack, actions = {
                if (memories.isNotEmpty()) ThIconButton(onClick = onAddMemory, label = "New memory") {
                    Icon(ThIcons.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            })
        },
    ) { insets ->
        val featured = memories.firstOrNull()
        if (featured == null) {
            ThEmptyState(
                title = "Your story starts here",
                description = "Save a photo, a date, or a few words from a moment you want to keep.",
                modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()),
                visual = { Icon(ThIcons.PhotoLibrary, null, Modifier.size(52.dp), tint = colors.roseDeep) },
                actionLabel = "Save your first memory", onAction = onAddMemory,
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(insets),
                contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            ) {
                item(key = featured.id) {
                    ThSurfaceCard(tone = CardTone.WARM, onClick = { onMemoryClick(featured.id) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                            val attachments = media[featured.id].orEmpty()
                            attachments.firstOrNull()?.let { asset ->
                                MemoryMediaFrame(MemoryMediaSource(asset), Modifier.fillMaxWidth().height(188.dp))
                            }
                            ThEyebrow(DateTimeHelper.formatDisplay(featured.memoryDate))
                            Text(
                                featured.title, style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 3, overflow = TextOverflow.Ellipsis,
                            )
                            featured.caption?.takeIf(String::isNotBlank)?.let {
                                Text(it, color = colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                            if (attachments.isNotEmpty()) {
                                Text(
                                    attachmentCount(attachments.size), style = MaterialTheme.typography.labelMedium,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }
                }
                if (memories.size > 1) {
                    item {
                        ThSectionHeader("Earlier memories", modifier = Modifier.padding(top = spacing.space6))
                    }
                    items(memories.drop(1), key = { it.id }) { memory ->
                        EarlierMemory(memory, media[memory.id].orEmpty()) { onMemoryClick(memory.id) }
                        ThDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun EarlierMemory(memory: Memory, media: List<MediaAsset>, onClick: () -> Unit) {
    val spacing = TwoHeartsTokens.Spacing
    val colors = LocalTwoHeartsColors.current
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(vertical = spacing.space3),
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        media.firstOrNull()?.let { asset -> MemoryMediaFrame(MemoryMediaSource(asset), Modifier.size(62.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.tightGap)) {
            Text(
                DateTimeHelper.formatShortDisplay(memory.memoryDate),
                style = MaterialTheme.typography.labelMedium, color = colors.textSecondary,
            )
            Text(
                memory.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(ThIcons.ChevronRight, null, Modifier.size(20.dp), tint = colors.textSecondary)
    }
}

internal fun attachmentCount(size: Int) = if (size == 1) "1 attachment" else "$size attachments"
