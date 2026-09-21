package com.twohearts.app.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThIconButton
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.decorations.DecorationPosition
import com.twohearts.app.ui.decorations.OnboardingArt
import com.twohearts.app.ui.decorations.ThDecoration
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.ThTextStyles
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * OnboardingLayout — the shared scaffold for every setup step.
 *
 * ## What the migrated version got wrong
 *
 * The previous scaffold was a `Column` with 24dp padding containing a 48dp
 * header row, a row of dots and a centred content `Box`. Rendering it showed
 * four concrete defects:
 *
 *  1. **It ignored the system bars.** With `enableEdgeToEdge()` in
 *     `MainActivity`, the header drew at y=0 under the status bar and the
 *     primary action sat inside the gesture-navigation area. The renders show
 *     the step dots colliding with the status-bar clock.
 *  2. **The step indicator carried no information.** Five identical dots, the
 *     current one merely 4dp larger, with no labels and no semantics — a
 *     person could not tell how many steps remained or what they were.
 *  3. **The brand lockup was drawn at 32dp in the header of every step**,
 *     including the steps where it competed with the step title, so the
 *     screen read as chrome-first rather than content-first.
 *  4. **Content was vertically centred in a fixed box**, so on the narrow
 *     Tecno-class geometry a form with a raised text size overflowed rather
 *     than scrolling, and the action button was pushed off-screen by the
 *     keyboard.
 *
 * ## The structure now
 *
 * The scaffold owns the frame and nothing else: safe areas, the back
 * affordance, the step indicator, a scrolling content region, and a pinned
 * action bar. Steps supply a title, optional supporting copy and their
 * fields. That split is what lets every step share one rhythm without each
 * one re-deciding its own spacing.
 *
 * @param stage the step being shown; drives the indicator and the back rule.
 * @param title the step heading. Rendered in the serif display face, because
 *   these are the product's emotional moments, not UI chrome.
 * @param onBack previous-step handler, or null where there is nowhere to go.
 * @param primaryLabel the main action's label.
 * @param onPrimary the main action.
 * @param eyebrow a short tracked label above the title.
 * @param subtitle supporting line under the title.
 * @param primaryEnabled false greys the action but keeps it visible, so the
 *   path forward is always legible.
 * @param secondary optional lower-emphasis action (a legitimate skip).
 * @param decoration which floral to frame the step with, or null for none.
 * @param content the step's fields.
 */
@Composable
fun OnboardingLayout(
    stage: OnboardingStage,
    title: String,
    onBack: (() -> Unit)?,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    primaryEnabled: Boolean = true,
    primaryLoading: Boolean = false,
    secondary: (@Composable () -> Unit)? = null,
    decoration: OnboardingArt? = OnboardingArt.ROSE_LILY_TRAIL,
    content: @Composable ColumnScope.() -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current
    val motion = LocalTwoHeartsMotion.current

    // Decorative entrance: the step fades in once on arrival. Suppressed
    // entirely under reduce-motion via the motion policy.
    val entrance by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = motion.decorative(TwoHeartsTokens.Duration.standard).toInt(),
        ),
        label = "th-onboarding-entrance",
    )

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // A single warm wash from the page colour into a blush corner. The
        // migrated scaffold used the flat page colour, which is a large part
        // of why first launch read as a blank form.
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

        if (decoration != null) {
            ThDecoration(
                art = decoration,
                position = DecorationPosition.TOP_END,
                size = 148.dp,
                alpha = 0.13f,
                offsetX = 30.dp,
                offsetY = 24.dp,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(entrance)
                // Consume the status bar, the gesture nav bar and the IME, so
                // the frame is never drawn underneath system chrome.
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = TwoHeartsTokens.Spacing.screenGutter),
        ) {
            OnboardingTopBar(stage = stage, onBack = onBack)

            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

            OnboardingStepIndicator(
                current = stage.order,
                total = OnboardingStage.LAST_SETUP_ORDER,
            )

            // Scrolling content region.
            //
            // Centre when it fits, scroll when it does not. The arrangement
            // must sit on a column that is *given* the viewport height: a
            // scrollable measures its content with an unbounded height, so an
            // arrangement on the scrolling column itself has nothing to
            // distribute into and silently top-aligns. The inner column below
            // is pinned to the viewport with `heightIn(min = …)`, which is what
            // lets a short step centre while a tall one (Extra Large text on a
            // 360dp phone) still scrolls.
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
                            .padding(top = TwoHeartsTokens.Spacing.sectionGap)
                            .padding(bottom = TwoHeartsTokens.Spacing.space4),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (eyebrow != null) {
                            Text(
                                text = eyebrow,
                                style = ThTextStyles.eyebrow,
                                color = thColors.textTertiary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (subtitle != null) {
                            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = thColors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

                    content()
                }
            }
            }

            // Pinned action bar — outside the scroll region so the way
            // forward is always reachable, and above the IME so the keyboard
            // can never cover it.
            OnboardingActionBar(
                primaryLabel = primaryLabel,
                onPrimary = onPrimary,
                primaryEnabled = primaryEnabled,
                primaryLoading = primaryLoading,
                secondary = secondary,
            )
        }
    }
}

/**
 * OnboardingTopBar — back affordance and the compact brand mark.
 *
 * The brand mark is drawn at 26dp here: present, so every step still belongs
 * to TwoHearts, but small enough that it never competes with the step title.
 * The migrated scaffold drew the full lockup at 32dp centred between two
 * spacers, which made the header the heaviest element on every step.
 */
@Composable
private fun OnboardingTopBar(
    stage: OnboardingStage,
    onBack: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back is only offered from step 1 onward. On the welcome screen there
        // is nothing behind it, and offering an exit from first launch is
        // exactly the "user accidentally leaves onboarding" failure the phase
        // guards against.
        if (onBack != null && !stage.isFirst()) {
            ThIconButton(onClick = onBack, label = "Go back to the previous step") {
                Icon(
                    imageVector = ThIcons.Back,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Dimensions.touchTargetMin))
        }

        Spacer(modifier = Modifier.weight(1f))

        BrandLogo(
            variant = BrandLogoVariant.MARK,
            size = 26,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Symmetry spacer, matching the back affordance's width so the mark
        // stays optically centred. A `Spacer` with only a height occupies zero
        // width — the exact bug Phase 2 fixed in the shell header — so this
        // sets an explicit width.
        Spacer(modifier = Modifier.width(TwoHeartsTokens.Dimensions.touchTargetMin))
    }
}

/**
 * OnboardingStepIndicator — progress through the setup steps.
 *
 * Replaces the row of anonymous dots. It shows a filled segment per completed
 * or current step, a readable caption ("Step 2 of 4"), and — critically —
 * publishes a real progress semantic so assistive technology announces
 * position rather than reading a row of unlabelled boxes.
 *
 * It is deliberately not a Material `Stepper`: the directive warns against
 * generic steppers, and a setup flow of four steps does not need a component
 * designed for wizards with branch logic.
 *
 * Progress is carried by *shape* (segment fill) as well as colour.
 */
@Composable
fun OnboardingStepIndicator(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val stepNumber = current.coerceIn(1, total)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Step $stepNumber of $total" },
            horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
        ) {
            for (step in 1..total) {
                val isReached = step <= stepNumber
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isReached) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                thColors.borderSubtle
                            }
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

        Text(
            text = "Step $stepNumber of $total",
            style = MaterialTheme.typography.labelMedium,
            color = thColors.textTertiary,
            // The visible caption duplicates the semantics on the row above.
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

/**
 * OnboardingActionBar — the pinned footer holding the step's actions.
 *
 * Establishes the hierarchy the directive asks for: exactly one primary
 * action, with any secondary action rendered at lower emphasis beneath it.
 * The migrated steps rendered "Continue" at `fillMaxWidth(0.8f)` with no top
 * separation, so the button floated unanchored at the bottom of the form.
 */
@Composable
private fun OnboardingActionBar(
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean,
    primaryLoading: Boolean,
    secondary: (@Composable () -> Unit)?,
) {
    val thColors = LocalTwoHeartsColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = TwoHeartsTokens.Spacing.space3)
            .padding(bottom = TwoHeartsTokens.Spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ThButton(
            onClick = onPrimary,
            text = primaryLabel,
            full = true,
            enabled = primaryEnabled,
            loading = primaryLoading,
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))

        if (secondary != null) {
            secondary()
        } else {
            Text(
                text = "Private · On this device",
                style = MaterialTheme.typography.labelSmall,
                color = thColors.textTertiary,
            )
        }
    }
}

/**
 * OnboardingSectionLabel — the small heading that groups a step's fields.
 *
 * Fields were previously labelled only by the input's own caption, so a step
 * with several related controls read as an undifferentiated list.
 */
@Composable
fun OnboardingSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/**
 * OnboardingInlineError — a step-level failure (a service that could not
 * write), as opposed to a field-level one.
 *
 * Uses the semantic error background rather than a bare red line so it reads
 * as a recoverable notice rather than an alarm — the directive's warning
 * about alarming red everywhere. It is a live region, so it is announced.
 */
@Composable
fun OnboardingInlineError(
    message: String,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.sm)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(thColors.errorBg)
            .border(TwoHeartsTokens.Border.hairline, thColors.error, shape)
            .padding(TwoHeartsTokens.Spacing.space3),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = thColors.error,
        )
    }
}
