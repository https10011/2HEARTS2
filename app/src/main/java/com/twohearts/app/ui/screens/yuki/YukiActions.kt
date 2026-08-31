package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.data.game.YukiAction
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * YukiActions — The action bar for interacting with Yuki.
 * Four actions: Feed, Pet, Play, Clean.
 * Each action has an icon, label, and visual feedback.
 * Buttons are disabled during activity animations.
 *
 * Mirrors legacy YukiActions.tsx exactly.
 */
@Composable
fun YukiActions(
    onAction: (YukiAction) -> Unit,
    disabled: Boolean,
    activeAction: YukiAction?,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        ActionDef(YukiAction.FEED, Icons.Default.Favorite, "Feed"),
        ActionDef(YukiAction.PET, Icons.Default.Star, "Pet"),
        ActionDef(YukiAction.PLAY, Icons.Default.ThumbUp, "Play"),
        ActionDef(YukiAction.CLEAN, Icons.Default.CleaningServices, "Clean")
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2)
    ) {
        actions.forEach { action ->
            val isActive = activeAction == action.id
            val isDisabled = disabled && !isActive

            YukiActionButton(
                action = action,
                isActive = isActive,
                isDisabled = isDisabled,
                onClick = { onAction(action.id) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun YukiActionButton(
    action: ActionDef,
    isActive: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isActive -> TwoHeartsTokens.Color.burgundy
        else -> TwoHeartsTokens.Color.surface
    }
    val contentColor = when {
        isActive -> TwoHeartsTokens.Color.textOnAccent
        else -> TwoHeartsTokens.Color.textPrimary
    }
    val borderColor = when {
        isActive -> TwoHeartsTokens.Color.burgundy
        else -> TwoHeartsTokens.Color.border
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.lg))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(TwoHeartsTokens.Radius.lg)
            )
            .alpha(if (isDisabled) 0.4f else 1f)
            .clickable(enabled = !isDisabled) { onClick() }
            .padding(vertical = TwoHeartsTokens.Spacing.space3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = "${action.label} Yuki",
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))
        Text(
            text = action.label,
            fontSize = 11.sp,
            color = contentColor
        )
    }
}

private data class ActionDef(
    val id: YukiAction,
    val icon: ImageVector,
    val label: String
)


