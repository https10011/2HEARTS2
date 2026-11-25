package com.twohearts.app.ui.screens.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.components.ButtonVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.theme.TwoHeartsTokens

internal val placeCategories = listOf("general", "date", "restaurant", "park", "home", "travel", "other")

internal fun placeCategoryKey(value: String): String = value.trim().lowercase().ifEmpty { "general" }

internal fun placeCategoryLabel(value: String): String = placeCategoryKey(value).replaceFirstChar(Char::titlecase)

@Composable
internal fun PlaceCategorySheet(
    open: Boolean,
    selected: String?,
    categories: List<String?>,
    onSelect: (String?) -> Unit,
    onClose: () -> Unit,
) {
    ThModal(open, onClose, label = "Place categories") {
        val space = TwoHeartsTokens.Spacing
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(space.space1)) {
            Text("Place categories", style = MaterialTheme.typography.headlineSmall)
            categories.forEach { category ->
                ThButton(
                    onClick = { onSelect(category); onClose() },
                    text = category?.let(::placeCategoryLabel) ?: "All places",
                    variant = if (selected == category) ButtonVariant.SECONDARY else ButtonVariant.GHOST,
                    icon = ThIcons.Check.takeIf { selected == category },
                )
            }
            ThButton(onClose, text = "Close", variant = ButtonVariant.GHOST)
        }
    }
}
