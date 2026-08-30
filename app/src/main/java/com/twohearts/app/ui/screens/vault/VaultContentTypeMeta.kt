package com.twohearts.app.ui.screens.vault

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Vault content types with visual metadata.
 * Maps to VaultItem.contentType from the data layer.
 */
enum class VaultContentType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val mimeTypePrefix: String
) {
    PHOTO(
        displayName = "Photo",
        icon = Icons.Default.Image,
        color = Color(0xFF6A1B2B), // Burgundy
        mimeTypePrefix = "image/"
    ),
    VIDEO(
        displayName = "Video",
        icon = Icons.Default.VideoFile,
        color = Color(0xFF4F7A5A), // Green
        mimeTypePrefix = "video/"
    ),
    NOTE(
        displayName = "Note",
        icon = Icons.Default.Note,
        color = Color(0xFFB07A1E), // Amber
        mimeTypePrefix = "text/"
    ),
    FILE(
        displayName = "File",
        icon = Icons.Default.InsertDriveFile,
        color = Color(0xFF2B2420), // Charcoal
        mimeTypePrefix = "application/"
    );

    companion object {
        fun fromMimeType(mimeType: String): VaultContentType {
            return when {
                mimeType.startsWith("image/") -> PHOTO
                mimeType.startsWith("video/") -> VIDEO
                mimeType.startsWith("text/") -> NOTE
                else -> FILE
            }
        }
    }
}

/**
 * Content type display information for the vault.
 */
data class ContentTypeDisplayInfo(
    val type: VaultContentType,
    val title: String,
    val description: String,
    val extension: String
)

/**
 * Get display info for a vault item based on its content type.
 */
fun VaultContentType.getDisplayInfo(fileName: String? = null): ContentTypeDisplayInfo {
    val ext = fileName?.substringAfterLast('.', "")?.uppercase() ?: ""
    
    return when (this) {
        VaultContentType.PHOTO -> ContentTypeDisplayInfo(
            type = this,
            title = "Photo",
            description = "Private photo stored securely",
            extension = ext.ifEmpty { "IMG" }
        )
        VaultContentType.VIDEO -> ContentTypeDisplayInfo(
            type = this,
            title = "Video",
            description = "Private video stored securely",
            extension = ext.ifEmpty { "VID" }
        )
        VaultContentType.NOTE -> ContentTypeDisplayInfo(
            type = this,
            title = "Note",
            description = "Private note stored securely",
            extension = ext.ifEmpty { "TXT" }
        )
        VaultContentType.FILE -> ContentTypeDisplayInfo(
            type = this,
            title = "File",
            description = "Private file stored securely",
            extension = ext.ifEmpty { "FILE" }
        )
    }
}
