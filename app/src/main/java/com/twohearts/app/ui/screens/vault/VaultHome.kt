package com.twohearts.app.ui.screens.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.twohearts.app.data.entity.VaultItem
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThDivider
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThTopBar
import com.twohearts.app.ui.components.UtilityItem
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
fun VaultHome(
    items: List<VaultItem>,
    onItemClick: (VaultItem) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val groups = remember(items) { items.groupBy { vaultContentType(it.contentType) } }
    val colors = LocalTwoHeartsColors.current
    val spacing = TwoHeartsTokens.Spacing
    Scaffold(
        modifier = modifier,
        topBar = { ThTopBar("Private Vault", onBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                ThDivider()
                ThButton(
                    onAddClick, text = "Add to Vault", icon = ThIcons.Add, full = true,
                    modifier = Modifier.padding(horizontal = spacing.screenGutter, vertical = spacing.space2),
                )
            }
        },
    ) { insets ->
        LazyColumn(
            Modifier.fillMaxSize().padding(insets),
            contentPadding = PaddingValues(horizontal = spacing.screenGutter, vertical = spacing.space4),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            if (items.isEmpty()) item {
                ThEmptyState(
                    title = "Your vault is empty", description = "Keep a note, photo, video, or file on this device.",
                    visual = { Icon(ThIcons.Lock, null, tint = colors.roseDeep) },
                )
            }
            for ((type, saved) in groups) {
                item {
                    Text(
                        when (type) {
                            VaultContentType.NOTE -> "Notes"
                            VaultContentType.PHOTO -> "Photos"
                            VaultContentType.VIDEO -> "Videos"
                            VaultContentType.FILE -> "Files"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.space2).semantics { heading() },
                    )
                }
                items(saved, key = { it.id }) { item ->
                    UtilityItem(
                        item.title?.takeIf(String::isNotBlank) ?: "Untitled item", type.icon,
                        DateTimeHelper.formatShortDisplay(item.createdAt),
                    ) { onItemClick(item) }
                }
            }
        }
    }
}
