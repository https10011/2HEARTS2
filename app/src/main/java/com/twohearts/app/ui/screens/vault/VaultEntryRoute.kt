package com.twohearts.app.ui.screens.vault

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.twohearts.app.data.entity.VaultItem

/**
 * Vault entry route - handles vault lock state and routes to appropriate screen.
 * When vault is locked, shows VaultLocked screen.
 * When vault is unlocked, shows VaultHome screen.
 */
@Composable
fun VaultEntryRoute(
    isVaultLocked: Boolean,
    vaultItems: List<VaultItem>,
    onPinEntered: (String) -> Unit,
    onPinError: (String) -> Unit,
    onItemClicked: (VaultItem) -> Unit,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVaultLocked) {
        VaultLocked(
            onPinEntered = onPinEntered,
            onError = onPinError,
            modifier = modifier
        )
    } else {
        VaultHome(
            items = vaultItems,
            onItemClick = onItemClicked,
            onAddClick = onAddClicked,
            modifier = modifier
        )
    }
}
