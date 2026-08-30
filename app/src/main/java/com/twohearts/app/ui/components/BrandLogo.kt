package com.twohearts.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * BrandLogo — the ONE authoritative TwoHearts brand mark.
 *
 * Renders the OFFICIAL owner-provided SVG artwork from assets/branding/.
 * Variants:
 * - 'brand' — full logo: interlocked hearts + "TwoHearts" + tagline
 * - 'mark' — interlocked hearts only
 *
 * Tone:
 * - 'brand' — standard rendering
 * - 'light' — recolored for dark brand surfaces
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    variant: BrandLogoVariant = BrandLogoVariant.BRAND,
    size: Int = 120,
    tone: BrandLogoTone = BrandLogoTone.BRAND,
) {
    // SVG assets are loaded from assets/branding/
    // For now, render a placeholder that will be replaced with actual SVG loading
    // when the asset pipeline is set up in later stages
    val width = size.dp
    val height = (size * when (variant) {
        BrandLogoVariant.BRAND -> 0.857f // 433.8324/506.3152
        BrandLogoVariant.MARK -> 0.929f  // 285/306.7499
    }).dp

    // Placeholder — will be replaced with actual SVG rendering
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        // In later stages, this will load the actual SVG from assets
        // For now, show a simple text placeholder
        androidx.compose.material3.Text(
            text = if (variant == BrandLogoVariant.BRAND) "TwoHearts" else "♥",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
        )
    }
}

enum class BrandLogoVariant {
    BRAND,  // Full logo with text
    MARK,   // Hearts only
}

enum class BrandLogoTone {
    BRAND,  // Standard rendering
    LIGHT,  // Recolored for dark surfaces
}
