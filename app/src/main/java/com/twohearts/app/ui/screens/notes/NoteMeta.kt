package com.twohearts.app.ui.screens.notes

import androidx.compose.ui.graphics.vector.ImageVector
import com.twohearts.app.ui.components.ThIcons

/**
 * NoteCategory — note categories matching legacy exactly.
 */
enum class NoteCategory(val label: String, val icon: ImageVector) {
    GENERAL("General", ThIcons.Note),
    SHARED("Shared", ThIcons.People),
    PRIVATE("Private", ThIcons.Lock),
    LOVE_LETTER("Love Letter", ThIcons.HeartFilled),
    GRATITUDE("Gratitude", ThIcons.ThumbUp),
    IDEA("Idea", ThIcons.Star),
    REMINDER("Reminder", ThIcons.Alarm);

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
