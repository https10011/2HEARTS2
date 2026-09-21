package com.twohearts.app.ui.decorations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twohearts.app.R

/**
 * OnboardingArt — the curated decorative vocabulary for first-launch moments.
 *
 * The migrated app shipped the owner-approved Rose/Lily artwork in
 * `assets/decorations/` and never referenced it (Phase 0 finding: 24 unused
 * SVG assets). Android cannot decode SVG at runtime and the app has no image
 * loader, so the art was simply dead weight.
 *
 * `tools/generate-onboarding-assets.mjs` rasterizes *only* the variants the
 * onboarding actually composes — the source files remain the single
 * authoritative artwork and nothing is redrawn or recoloured. This object is
 * the one place a screen asks for decoration; screens never touch resources
 * directly, so replacing a floral in `assets/decorations/` regenerates every
 * consumer.
 *
 * Decoration is deliberately *quiet*: the directive's warning about turning
 * onboarding into a Valentine's poster is why these are low-opacity corner
 * accents rather than repeating patterns, and why the welcome screen uses one
 * floral rather than a border.
 */
enum class OnboardingArt(val resId: Int) {
    /** Rose/Lily 01 — trailing stems; frames a corner without a focal bloom. */
    ROSE_LILY_TRAIL(R.drawable.decor_rose_lily_01),

    /** Rose/Lily 11 — a single open bloom; the welcome screen's accent. */
    ROSE_LILY_BLOOM(R.drawable.decor_rose_lily_11),

    /** Rose/Lily 15 — a cluster; used for the completion celebration. */
    ROSE_LILY_CLUSTER(R.drawable.decor_rose_lily_15),
}

/**
 * Corner placement for a decoration inside its parent.
 */
enum class DecorationPosition {
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END,
}

/**
 * ThDecoration — a single, non-interactive decorative floral anchored to a
 * corner of a [Box].
 *
 * It is `alpha`-faded via the painter rather than composited as a modifier so
 * the artwork keeps its own soft edges, and it is marked with no semantics so
 * screen readers never announce pure ornament.
 *
 * @param art which approved floral to draw.
 * @param position which corner to anchor to. The art is allowed to bleed off
 *   the edge deliberately — a floral cropped by the screen edge reads as a
 *   considered composition, while one floating fully inside the page reads as
 *   a sticker.
 * @param size rendered width. Height follows the artwork's own ratio.
 * @param alpha 0f–1f. Onboarding uses 0.10–0.22; anything higher starts to
 *   compete with form content on the narrow target device.
 * @param offsetX / offsetY nudge the art off the corner. Positive values move
 *   it toward the page centre.
 */
@Composable
fun BoxScope.ThDecoration(
    art: OnboardingArt,
    position: DecorationPosition,
    size: Dp,
    alpha: Float = 0.18f,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }

    // Derive the height from the intrinsic ratio so the art is never
    // distorted — the legacy implementation hardcoded ratios per variant and
    // got them wrong for several of the files.
    val painter = painterResource(id = art.resId)
    val ratio = painter.intrinsicSize.let { intrinsic ->
        if (intrinsic.width > 0f && intrinsic.height > 0f) {
            intrinsic.height / intrinsic.width
        } else {
            1f
        }
    }
    val height = with(density) { (sizePx * ratio).toDp() }

    val alignment = when (position) {
        DecorationPosition.TOP_START -> Alignment.TopStart
        DecorationPosition.TOP_END -> Alignment.TopEnd
        DecorationPosition.BOTTOM_START -> Alignment.BottomStart
        DecorationPosition.BOTTOM_END -> Alignment.BottomEnd
    }

    val dx = when (position) {
        DecorationPosition.TOP_START, DecorationPosition.BOTTOM_START -> offsetX
        else -> -offsetX
    }
    val dy = when (position) {
        DecorationPosition.TOP_START, DecorationPosition.TOP_END -> offsetY
        else -> -offsetY
    }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .align(alignment)
            .offset(x = dx, y = dy)
            .size(width = size, height = height),
        contentScale = ContentScale.Fit,
        alpha = alpha,
    )
}

/**
 * OnboardingArtBackdrop — the ambient corner composition used by the
 * onboarding scaffold.
 *
 * Kept as one composable rather than a per-screen arrangement so the whole
 * flow shares a single, deliberate frame. Place it as the first child of a
 * full-size [Box] and render content on top.
 */
@Composable
fun BoxScope.OnboardingArtBackdrop(
    corner: OnboardingArt = OnboardingArt.ROSE_LILY_TRAIL,
    cornerPosition: DecorationPosition = DecorationPosition.TOP_END,
    footer: OnboardingArt = OnboardingArt.ROSE_LILY_BLOOM,
    footerPosition: DecorationPosition = DecorationPosition.BOTTOM_START,
    cornerSize: Dp = 148.dp,
    footerSize: Dp = 120.dp,
    cornerAlpha: Float = 0.14f,
    footerAlpha: Float = 0.12f,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ThDecoration(
            art = corner,
            position = cornerPosition,
            size = cornerSize,
            alpha = cornerAlpha,
            offsetX = 24.dp,
            offsetY = (-8).dp,
        )
        ThDecoration(
            art = footer,
            position = footerPosition,
            size = footerSize,
            alpha = footerAlpha,
            offsetX = 28.dp,
            offsetY = (-10).dp,
        )
    }
}
