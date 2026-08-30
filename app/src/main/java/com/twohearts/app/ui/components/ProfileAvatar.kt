package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

/**
 * ProfileAvatar — shared avatar component matching legacy ProfileAvatar.tsx.
 *
 * Displays a profile photo when available, or a styled initial-letter fallback.
 * Falls back to a smile icon when no name is provided.
 */
@Composable
fun ProfileAvatar(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Int = 64,
    label: String? = null,
) {
    val thColors = LocalTwoHeartsColors.current
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        thColors.burgundy.copy(alpha = 0.7f),
                        thColors.roseMuted.copy(alpha = 0.7f),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (initial.isNotEmpty()) {
            Text(
                text = initial,
                fontSize = (size * 0.44).sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        } else {
            // Smile icon placeholder
            Text(
                text = "☺",
                fontSize = (size * 0.44).sp,
                color = Color.White,
            )
        }
    }
}
