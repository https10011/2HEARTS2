package com.twohearts.app.ui.screens.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * NoteCategory — note categories matching legacy exactly.
 */
enum class NoteCategory(val label: String, val icon: ImageVector) {
    GENERAL("General", Icons.Default.Note),
    SHARED("Shared", Icons.Default.People),
    PRIVATE("Private", Icons.Default.Lock),
    LOVE_LETTER("Love Letter", Icons.Default.Favorite),
    GRATITUDE("Gratitude", Icons.Default.ThumbUp),
    IDEA("Idea", Icons.Default.Lightbulb),
    REMINDER("Reminder", Icons.Default.Alarm);

    companion object {
        /**
         * Get category from string value.
         */
        fun fromString(value: String): NoteCategory {
            return entries.find { it.name.lowercase() == value.lowercase() } ?: GENERAL
        }
    }
}

/**
 * CategoryMeta — metadata for note categories.
 */
object CategoryMeta {
    /**
     * Get category display name.
     */
    fun getDisplayName(category: NoteCategory): String {
        return category.label
    }

    /**
     * Get category icon.
     */
    fun getIcon(category: NoteCategory): ImageVector {
        return category.icon
    }

    /**
     * Get all categories.
     */
    fun getAllCategories(): List<NoteCategory> {
        return NoteCategory.entries.toList()
    }

    /**
     * Get category color (for visual treatment).
     */
    fun getCategoryColor(category: NoteCategory): Long {
        return when (category) {
            NoteCategory.GENERAL -> 0xFF6A1B2B // Burgundy
            NoteCategory.SHARED -> 0xFF4F7A5A // Green
            NoteCategory.PRIVATE -> 0xFFB07A1E // Gold
            NoteCategory.LOVE_LETTER -> 0xFFE8A0B4 // Pink
            NoteCategory.GRATITUDE -> 0xFF8B9E7C // Sage
            NoteCategory.IDEA -> 0xFF7A3F5E // Plum
            NoteCategory.REMINDER -> 0xFFA33A2A // Red
        }
    }
}
