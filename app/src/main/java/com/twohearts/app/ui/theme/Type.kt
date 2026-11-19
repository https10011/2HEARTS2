package com.twohearts.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.twohearts.app.R

private val DisplayTypeface = FontFamily(Font(R.font.dm_serif_display_regular))
private val InterfaceTypeface = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Editorial display styles and readable interface styles for the native app. */
val TwoHeartsTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.4).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterfaceTypeface,
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
        fontFamily = InterfaceTypeface,
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
     * streak counts. The same editorial face anchors the display hierarchy.
     */
    val numeral = TextStyle(
        fontFamily = DisplayTypeface,
        fontWeight = FontWeight.Normal,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1).sp,
    )

    /** Reading copy for memories, notes and story text. */
    val reading = TextStyle(
        fontFamily = InterfaceTypeface,
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
        fontFamily = InterfaceTypeface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
    )
}
