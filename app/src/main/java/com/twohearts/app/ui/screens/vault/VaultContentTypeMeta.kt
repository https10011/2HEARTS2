package com.twohearts.app.ui.screens.vault

import androidx.compose.ui.graphics.vector.ImageVector
import com.twohearts.app.ui.components.ThIcons

enum class VaultContentType(val displayName: String, val icon: ImageVector) {
    PHOTO("Photo", ThIcons.Image),
    VIDEO("Video", ThIcons.VideoFile),
    NOTE("Note", ThIcons.Note),
    FILE("File", ThIcons.File),
}
