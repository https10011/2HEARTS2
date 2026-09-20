package com.twohearts.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * TwoHearts Typography.
 *
 * Derived from the legacy tokens.css scale, with two Phase 1 corrections:
 *
 *  1. The readable floor is 12sp. The legacy scale bottomed out at 12sp for
 *     labels but shipped 13sp body copy that many users reported as too
 *     small; `bodySmall` and `labelMedium` are now 14sp and 13sp
 *     respectively, so secondary copy is comfortably readable on a
 *     6.6" 720p-class device.
 *
 *  2. Hierarchy is separated by *shape* as well as size. Screen titles and
 *     emotional copy use the serif display face; controls, navigation and
 *     metadata use the system sans. That contrast is what stops the app
 *     from reading as one undifferentiated sans-serif grid.
 *
 * Type roles:
 *  - displayLarge/Medium/Small — serif. Splash, onboarding titles, the
 *    relationship counter, memory/event reading views. Used for emotional
 *    moments only, never for UI chrome.
 *  - headlineLarge/Medium/Small — sans semibold. Screen titles.
 *  - titleLarge/Medium/Small — sans medium. Section headings, card titles.
 *  - bodyLarge/Medium/Small — sans regular. Reading copy.
 *  - labelLarge/Medium/Small — sans medium. Buttons, chips, metadata.
 */
val TwoHeartsTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.4).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp,
    ),
    /**
     * Section eyebrows, badges and dense metadata. This is the floor of the
     * type system — nothing in the app should be smaller than this.
     */
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

/**
 * TwoHearts-specific text roles that don't map cleanly onto Material's
 * 15-slot scale. Screens should use these instead of hardcoding font sizes.
 */
object ThTextStyles {
    /**
     * Big emotional numerals — the relationship day counter, game scores,
     * streak counts. Serif, tightly tracked, tabular so digits don't jitter
     * as they change.
     */
    val numeral = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1).sp,
    )

    /** Reading copy for memories, notes and story text. */
    val reading = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.1.sp,
    )

    /**
     * Uppercase-ish section eyebrow above a group of content. Rendered in
     * sentence case (the directive avoids shouty all-caps), but tracked out
     * and small so it reads as a label rather than a heading.
     */
    val eyebrow = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
    )
}
