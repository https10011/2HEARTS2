package com.twohearts.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.twohearts.app.R
import com.twohearts.app.ui.theme.TwoHeartsTokens
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

/**
 * BrandLogo — the ONE authoritative TwoHearts brand mark.
 *
 * The full lockup uses the owner artwork. The compact mark uses the native
 * vector traced from that artwork, with its optical bounds preserved.
 *
 * Variants:
 *  - [BrandLogoVariant.BRAND] — full lockup: interlocked hearts + wordmark.
 *  - [BrandLogoVariant.MARK]  — interlocked hearts only, for compact spots
 *    such as the centre bottom-nav button.
 *
 * Tones:
 *  - [BrandLogoTone.BRAND] — the supplied colors on light surfaces and an
 *    ivory tint in dark mode.
 *  - [BrandLogoTone.LIGHT] — an ivory tint for an explicitly dark accent surface.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    variant: BrandLogoVariant = BrandLogoVariant.BRAND,
    size: Int = 120,
    tone: BrandLogoTone = BrandLogoTone.BRAND,
) {
    val thColors = LocalTwoHeartsColors.current
    val useLightInk = tone == BrandLogoTone.LIGHT || MaterialTheme.colorScheme.background.luminance() < 0.20f

    val (resId, aspect) = when (variant) {
        // 720x617
        BrandLogoVariant.BRAND -> R.drawable.brand_logo to 0.857f
        // Cropped optical bounds of the official intertwined mark.
        BrandLogoVariant.MARK -> R.drawable.twohearts_mark to (334f / 429f)
    }

    Image(
        painter = painterResource(id = resId),
        contentDescription = "TwoHearts",
        modifier = modifier
            .width(size.dp)
            .height((size * aspect).dp),
        contentScale = ContentScale.Fit,
        colorFilter = if (useLightInk) {
            ColorFilter.tint(thColors.textOnAccent)
        } else if (variant == BrandLogoVariant.MARK) {
            ColorFilter.tint(TwoHeartsTokens.Color.burgundy600)
        } else {
            null
        },
    )
}

enum class BrandLogoVariant {
    /** Full lockup with wordmark. */
    BRAND,

    /** Interlocked hearts only. */
    MARK,
}

enum class BrandLogoTone {
    /** Palette follows the current theme. */
    BRAND,

    /** Recoloured for dark or burgundy surfaces. */
    LIGHT,
}
