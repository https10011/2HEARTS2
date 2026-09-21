package com.twohearts.app.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.decorations.DecorationPosition
import com.twohearts.app.ui.decorations.OnboardingArt
import com.twohearts.app.ui.decorations.ThDecoration
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens
import com.twohearts.app.ui.components.ThDateValue

/**
 * SetupCompleteScreen — the last thing before Home.
 *
 * ## What the migrated version was
 *
 * The brand lockup, the headline "You're All Set!", the sentence *"Welcome to
 * TwoHearts, {name}! Start sharing moments with {partner}."* and a button
 * reading "Start Using TwoHearts".
 *
 * Three problems, all of them in the copy:
 *
 *  - **It used the word "partner"** — the same terminology problem as the
 *    relationship step, on the screen that summarises the whole setup.
 *  - **It narrated the app back to the person.** "Start sharing moments" is
 *    a feature pitch at the moment when the setup is already done; it adds
 *    nothing and it is the flattest possible note to end on.
 *  - **It showed nothing that had just been created.** Four steps of input
 *    produced a generic congratulations card, so the work the person did was
 *    never reflected back.
 *
 * ## What it is now
 *
 * The summary is the point. The screen names both people, shows the date the
 * count begins from, states whether a PIN was set, and then gets out of the
 * way. Every line is derived from what was actually entered, so the screen
 * cannot congratulate someone on a setup that did not happen.
 *
 * ## The transition into the app
 *
 * The directive asks that entering the app feel like arriving rather than
 * like a route change. The completion surface lifts and fades out through
 * [LocalTwoHeartsMotion], so the shell fades in underneath it — one
 * continuous movement instead of a hard cut. Under reduce-motion the movement
 * collapses to an instant change, which is the correct behaviour and not a
 * degraded one.
 */
@Composable
fun SetupCompleteScreen(
    data: OnboardingData,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current

    val entrance by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(motion.decorative(TwoHeartsTokens.Duration.slow).toInt()),
        label = "th-complete-entrance",
    )

    // The names are already trimmed and non-blank by the time this screen
    // renders; the fallbacks are defensive so a replayed state can never show
    // an empty string in the sentence.
    val owner = data.ownerName.ifBlank { "You" }
    val partner = data.partnerName.ifBlank { "your special someone" }

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

        // The one place the app allows itself a warmer floral moment: this is
        // the emotional peak of the flow, and the cluster artwork reads as a
        // small celebration without becoming a poster.
        ThDecoration(
            art = OnboardingArt.ROSE_LILY_CLUSTER,
            position = DecorationPosition.BOTTOM_START,
            size = 176.dp,
            alpha = 0.16f,
            offsetX = 30.dp,
            offsetY = 12.dp,
        )
        ThDecoration(
            art = OnboardingArt.ROSE_LILY_BLOOM,
            position = DecorationPosition.TOP_END,
            size = 132.dp,
            alpha = 0.13f,
            offsetX = 34.dp,
            offsetY = 14.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(entrance)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Vertical balance: the arrangement sits on a column *given* the
            // viewport height, because a scrollable measures its child with an
            // unbounded height and would silently top-align. Content centres
            // when it fits and scrolls when enlarged text makes it taller.
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
                    // Both people, drawn as a pair, before any words. This is
                    // the visual answer to "who is this for".
                    SetupCompletePair(
                        ownerName = owner,
                        partnerName = partner,
                    )

                    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space6))

                    Text(
                        text = "Your space is ready",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

                    Text(
                        text = "$owner and $partner — everything from here stays " +
                            "between the two of you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = thColors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space6))

                    SetupSummaryCard(data = data)

                    Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))

                    BrandLogo(
                        variant = BrandLogoVariant.MARK,
                        size = 40,
                    )
                    }
                }
            }

            ThButton(
                onClick = onComplete,
                text = "Open TwoHearts",
                full = true,
            )

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space4))
        }
    }
}

/**
 * SetupCompletePair — the two people, side by side, linked.
 *
 * A plain illustration of the couple rather than two avatars in a list: the
 * small heart between them is the one place in onboarding where the brand's
 * metaphor is stated literally, and it is earned here because the person has
 * just named both halves.
 */
@Composable
private fun SetupCompletePair(
    ownerName: String,
    partnerName: String,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        com.twohearts.app.ui.components.ProfileAvatar(
            name = ownerName,
            photoUrl = null,
            size = 76,
        )

        Box(
            modifier = Modifier
                .padding(horizontal = TwoHeartsTokens.Spacing.space3)
                .size(36.dp)
                .clip(CircleShape)
                .background(thColors.surfaceBlush)
                .border(TwoHeartsTokens.Border.hairline, thColors.roseMuted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ThIcons.HeartFilled,
                contentDescription = "Together",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp),
            )
        }

        com.twohearts.app.ui.components.ProfileAvatar(
            name = partnerName,
            photoUrl = null,
            size = 76,
        )
    }
}

/**
 * SetupSummaryCard — what was actually created, in four lines.
 *
 * Each row is derived from [OnboardingData], so this cannot claim something
 * the person did not do. The PIN row is the only one that can read as "not
 * set", and it is phrased as a choice rather than a gap.
 */
@Composable
private fun SetupSummaryCard(
    data: OnboardingData,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.lg)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(TwoHeartsTokens.Border.hairline, thColors.borderSubtle, shape)
            .padding(TwoHeartsTokens.Spacing.space5),
        verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3),
    ) {
        SummaryRow(
            label = "This is",
            value = data.ownerName.ifBlank { "You" },
        )
        SummaryRow(
            label = "Shared with",
            value = data.partnerName.ifBlank { "Your special someone" },
        )
        SummaryRow(
            label = "Counting from",
            value = null,
            valueContent = { ThDateValue(value = data.startDate) },
        )
        SummaryRow(
            label = "App lock",
            value = if (data.pin != null) "On" else "Not set",
        )
        SummaryRow(
            label = "Appearance",
            value = when (data.themeMode) {
                "dark" -> "Dark"
                "light" -> "Light"
                else -> "Follows your phone"
            },
        )
    }
}

/**
 * SummaryRow — one summary line.
 *
 * The value sits in the same row as its label so the card scans as a short
 * table. It is laid out with the label weighted, so a long value wraps rather
 * than colliding with the label at large text sizes.
 */
@Composable
private fun SummaryRow(
    label: String,
    value: String?,
    valueContent: (@Composable () -> Unit)? = null,
) {
    val thColors = LocalTwoHeartsColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = thColors.textTertiary,
            modifier = Modifier.width(112.dp),
        )
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space3))
        if (valueContent != null) {
            valueContent()
        } else {
            Text(
                text = value.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}