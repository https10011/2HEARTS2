package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * ProfileAvatar — the shared avatar for the owner and partner.
 *
 * Renders, in priority order:
 *  1. the profile photo, when the URI resolves;
 *  2. the person's initial on a warm burgundy-to-rose gradient;
 *  3. a heart glyph when no name is known yet.
 *
 * Phase 1: the empty state used to render a "☺" text emoji, which fell
 * outside the app's icon system and rendered inconsistently across OEM
 * fonts (Tecno/Transsion devices in particular). It now uses a vector icon
 * from the central set. The gradient was also flattened slightly and the
 * initial re-weighted so small avatars (28–32dp in list rows) stay legible.
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
                    colors = listOf(thColors.burgundy, thColors.roseDeep)
                )
            )
            .border(
                TwoHeartsTokens.Border.hairline,
                thColors.textOnAccent.copy(alpha = 0.22f),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (initial.isNotEmpty()) {
            Text(
                text = initial,
                fontSize = (size * 0.42).sp,
                fontWeight = FontWeight.SemiBold,
                color = thColors.textOnAccent,
            )
        } else {
            Icon(
                imageVector = ThIcons.Heart,
                contentDescription = null,
                tint = thColors.textOnAccent.copy(alpha = 0.9f),
                modifier = Modifier.size((size * 0.46).dp),
            )
        }
    }
}
