package com.twohearts.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/** Two independently sized labels and matching private-media avatars. */
@Composable
internal fun CouplePortrait(
    owner: Profile?,
    partner: Profile?,
    modifier: Modifier = Modifier,
    maxAvatarSize: Int = 112,
) {
    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .padding(vertical = TwoHeartsTokens.Spacing.space1),
    ) {
        val stackPortraits = maxWidth < 430.dp && LocalDensity.current.fontScale >= 1.5f
        if (stackPortraits) {
            Column(verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3)) {
                PortraitRow(owner, "You")
                HorizontalDivider(color = LocalTwoHeartsColors.current.borderSubtle)
                PortraitRow(partner, "Your special someone")
            }
        } else {
            val portraitSize = (maxWidth * 0.31f).value.toInt().coerceIn(72, maxAvatarSize)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                PortraitColumn(owner, "You", portraitSize, Modifier.weight(1f))
                Box(
                    modifier = Modifier.width(32.dp).height((portraitSize + 10).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        ThIcons.HeartFilled,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = LocalTwoHeartsColors.current.roseDeep,
                    )
                }
                PortraitColumn(partner, "Your special someone", portraitSize, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PortraitColumn(profile: Profile?, fallback: String, size: Int, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        PortraitAvatar(profile, size)
        Spacer(Modifier.height(TwoHeartsTokens.Spacing.space3))
        PortraitName(profile, fallback, Modifier.fillMaxWidth(), TextAlign.Center)
    }
}

@Composable
private fun PortraitRow(profile: Profile?, fallback: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3),
    ) {
        PortraitAvatar(profile, size = 78)
        PortraitName(profile, fallback, Modifier.weight(1f), TextAlign.Start, stacked = true)
    }
}

@Composable
private fun PortraitAvatar(profile: Profile?, size: Int) {
    val colors = LocalTwoHeartsColors.current
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(TwoHeartsTokens.Border.hairline, colors.borderStrong, CircleShape)
            .padding(5.dp),
    ) {
        ProfileAvatar(name = profile?.name.orEmpty(), photoUrl = profile?.photoRef, size = size)
    }
}

@Composable
private fun PortraitName(
    profile: Profile?,
    fallback: String,
    modifier: Modifier,
    textAlign: TextAlign,
    stacked: Boolean = false,
) {
    val display = MaterialTheme.typography.displaySmall
    val style = if (stacked) {
        display.copy(
            fontSize = TwoHeartsTokens.Typography.sizeXl,
            lineHeight = 28.sp,
            lineBreak = LineBreak.Paragraph,
        )
    } else {
        display.copy(lineBreak = LineBreak.Heading, hyphens = Hyphens.Auto)
    }
    Text(
        text = profile?.name.orEmpty().trim().ifEmpty { fallback },
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = textAlign,
        maxLines = if (stacked) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis,
    )
}
