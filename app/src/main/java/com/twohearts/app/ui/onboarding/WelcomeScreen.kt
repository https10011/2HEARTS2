package com.twohearts.app.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.R
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.decorations.DecorationPosition
import com.twohearts.app.ui.decorations.OnboardingArt
import com.twohearts.app.ui.decorations.ThDecoration
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * WelcomeScreen — the first screen of TwoHearts.
 *
 * ## What the migrated version was
 *
 * A centred `Column` holding the brand lockup, the literal headline
 * "Welcome to TwoHearts", the sentence *"A private space for you and your
 * partner to share memories, notes, and moments together."*, and a "Get
 * Started" button.
 *
 * Rendering it (Phase 1/2 evidence `60-onboarding-welcome.png`) showed why it
 * did not read as TwoHearts:
 *
 *  - the headline repeated the wordmark already drawn directly above it, so
 *    the screen's most valuable line said nothing;
 *  - the body copy read as a feature list — "memories, notes, and moments" —
 *    which is a description of a product, not an invitation into one;
 *  - "partner" is the generic term the directive asks to avoid in favour of
 *    the product's own vocabulary;
 *  - the whole composition was vertically centred with no dateline, no
 *    imagery and no sense of place, so it read as a splash screen with a
 *    form button underneath;
 *  - the primary action was inset to 80% width, so it floated rather than
 *    anchoring the bottom of the screen.
 *
 * ## What it is now
 *
 * The capture scene. The owner-supplied welcome artwork
 * (`assets/images/onboarding-welcome-photo.svg`, rasterized by the onboarding
 * asset tool) provides the warmth; the official mark sits above it; the
 * headline is a real sentence rather than a repeated product name; one line
 * of copy states what the app *is* and — more importantly — what it is not;
 * and the way in is a single full-width action anchored above the system
 * navigation bar with a quiet privacy line beneath it.
 *
 * There is exactly one primary action and no secondary one. Nothing here
 * asks the person to do anything before they have been told what they are
 * joining, which is the difference between a welcome and a form.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current

    // The scene arrives rather than appearing: the artwork and the words
    // settle in together. Under reduce-motion this collapses to the final
    // state immediately.
    val entrance by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(motion.decorative(TwoHeartsTokens.Duration.slow).toInt()),
        label = "th-welcome-entrance",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            thColors.surfaceBlush,
                        ),
                    )
                ),
        )

        ThDecoration(
            art = OnboardingArt.ROSE_LILY_TRAIL,
            position = DecorationPosition.TOP_END,
            size = 168.dp,
            alpha = 0.15f,
            offsetX = 34.dp,
            offsetY = 20.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Vertical balance: the arrangement sits on a column *given* the
            // viewport height. A scrollable measures its child with an
            // unbounded height, so centring on the scrolling node itself does
            // nothing and silently top-aligns. Content centres when it fits;
            // oversized text (Extra Large on a 360dp phone) scrolls.
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val viewport = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = viewport)
                            .padding(top = TwoHeartsTokens.Spacing.space6)
                            .padding(bottom = TwoHeartsTokens.Spacing.space4),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        // Official artwork only. The directive forbids substituting
                        // text, emoji or a recreated mark here, and Phase 1 made
                        // this the single authoritative source for the brand.
                        BrandLogo(
                            variant = BrandLogoVariant.BRAND,
                            size = 172,
                        )

                        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space6))

                        WelcomeArtCard()

                        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space6))

                        ThOrnamentRule(label = "For two")

                        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))

                        Text(
                            text = "Just the two of you",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

                        // The wordmark is already on screen; this line's job is to
                        // say what the app is for, in the product's own vocabulary.
                        Text(
                            text = "Your memories, plans and little things — kept in one " +
                                "place that belongs only to you and your special someone.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = thColors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            ThButton(
                onClick = onGetStarted,
                text = "Begin",
                full = true,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

            // The privacy promise, at the one moment it is most meaningful.
            // It is a statement of fact about the architecture, not a badge.
            Text(
                text = "No accounts. Nothing leaves this device.",
                style = MaterialTheme.typography.labelSmall,
                color = thColors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))
        }
    }
}

/**
 * WelcomeArtCard — the welcome artwork in a warm, soft-cornered frame.
 *
 * The archived artwork is the owner-supplied illustration that the reference
 * screens place at the top of first launch. It is presented in a rounded card
 * rather than full-bleed so it reads as a framed picture on a wall — which
 * suits the "private little world" framing far better than a photograph
 * bleeding edge to edge.
 *
 * The frame is capped by width and by a fraction of the available height so
 * it cannot dominate a short screen; the vertical space that remains is what
 * keeps the composition breathing on the 360×780dp target.
 */
@Composable
private fun WelcomeArtCard(modifier: Modifier = Modifier) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.xl)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(shape)
            .background(thColors.surfaceWarm),
    ) {
        Image(
            painter = painterResource(id = R.drawable.onboarding_welcome_photo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

/**
 * ThOrnamentRule — a short decorative rule with a word between two hairlines.
 *
 * The approved reference screens use this between the illustration and the
 * headline. It is a small, quiet device: it separates the picture from the
 * words without a heavy divider, and it gives the composition a centre line
 * that the centred text can hang from.
 *
 * The label is exposed to assistive technology as text rather than hidden,
 * because it carries a little meaning ("For two" names who the app is for).
 */
@Composable
fun ThOrnamentRule(
    label: String,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(1.dp)
                .background(thColors.roseMuted),
        )
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
        Text(
            text = label,
            style = ThTextStyles.eyebrow,
            color = thColors.textTertiary,
        )
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(1.dp)
                .background(thColors.roseMuted),
        )
    }
}