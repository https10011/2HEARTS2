package com.twohearts.app.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.ui.components.*
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

@Composable
fun VaultHome(
    items: List<VaultItem>,
    onItemClick: (VaultItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val colors = LocalTwoHeartsColors.current
    Scaffold(
        modifier = modifier,
        topBar = { ThTopBar(title = "Private Vault", onBack = onBack) },
        floatingActionButton = {
            if (items.isNotEmpty()) ExtendedFloatingActionButton(
                onClick = onAddClick, icon = { Icon(ThIcons.Add, null) }, text = { Text("Add to Vault") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            ThEmptyState(
                title = "Your vault is empty",
                description = "Keep a note, photo, video, or file on this device.",
                visual = { Icon(ThIcons.Lock, null, Modifier.size(52.dp), tint = colors.burgundy) },
                actionLabel = "Add to Vault", onAction = onAddClick,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    val type = VaultContentType.entries.firstOrNull { it.name.equals(item.contentType, true) }
                        ?: VaultContentType.FILE
                    Surface(onClick = { onItemClick(item) }, color = MaterialTheme.colorScheme.background) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(type.icon, null, Modifier.size(25.dp), tint = colors.burgundy)
                            Column(Modifier.weight(1f)) {
                                Text(item.title?.takeIf(String::isNotBlank) ?: "Untitled item", maxLines = 3,
                                    overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                                Text(type.displayName, color = colors.textSecondary)
                            }
                            Icon(ThIcons.ChevronRight, null, tint = colors.textSecondary)
                        }
                    }
                    HorizontalDivider(color = colors.divider)
                }
            }
        }
    }
}
