package com.twohearts.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Text scaling levels for accessibility (MasterPrompt §28).
 */
enum class TextScalingLevel(val scale: Float) {
    SMALL(0.88f),
    DEFAULT(1f),
    LARGE(1.12f),
    EXTRA_LARGE(1.28f);
}

/**
 * TwoHearts Extended Color Scheme
 *
 * Beyond Material 3's standard color slots, TwoHearts needs additional
 * semantic colors that don't map to Material's system. This provides them
 * via CompositionLocal so any composable can access the full brand palette.
 */
@Immutable
data class TwoHeartsColors(
    // Brand
    val burgundy: Color,
    val burgundyLight: Color,
    val burgundyDark: Color,
    // Extended burgundy family
    val burgundy50: Color,
    val burgundy100: Color,
    val burgundy200: Color,
    val burgundy300: Color,
    val burgundy400: Color,
    val burgundy500: Color,
    // Neutrals
    val cream: Color,
    val blush: Color,
    val roseMuted: Color,
    val pink: Color,
    val beige: Color,
    val charcoal: Color,
    val neutralSoft: Color,
    val warmIvory: Color,
    val dustyRose: Color,
    val plum: Color,
    val sage: Color,
    // Semantic surfaces
    val surfaceElevated: Color,
    val surfaceWarm: Color,
    val surfaceBlush: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    val divider: Color,
    val overlay: Color,
    val overlaySoft: Color,
    // Feedback
    val success: Color,
    val successBg: Color,
    val warning: Color,
    val error: Color,
    val errorBg: Color,
)

val LocalTwoHeartsColors = staticCompositionLocalOf {
    TwoHeartsColors(
        burgundy = Color.Unspecified,
        burgundyLight = Color.Unspecified,
        burgundyDark = Color.Unspecified,
        burgundy50 = Color.Unspecified,
        burgundy100 = Color.Unspecified,
        burgundy200 = Color.Unspecified,
        burgundy300 = Color.Unspecified,
        burgundy400 = Color.Unspecified,
        burgundy500 = Color.Unspecified,
        cream = Color.Unspecified,
        blush = Color.Unspecified,
        roseMuted = Color.Unspecified,
        pink = Color.Unspecified,
        beige = Color.Unspecified,
        charcoal = Color.Unspecified,
        neutralSoft = Color.Unspecified,
        warmIvory = Color.Unspecified,
        dustyRose = Color.Unspecified,
        plum = Color.Unspecified,
        sage = Color.Unspecified,
        surfaceElevated = Color.Unspecified,
        surfaceWarm = Color.Unspecified,
        surfaceBlush = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textTertiary = Color.Unspecified,
        textOnAccent = Color.Unspecified,
        divider = Color.Unspecified,
        overlay = Color.Unspecified,
        overlaySoft = Color.Unspecified,
        success = Color.Unspecified,
        successBg = Color.Unspecified,
        warning = Color.Unspecified,
        error = Color.Unspecified,
        errorBg = Color.Unspecified,
    )
}

private val LightExtendedColors = TwoHeartsColors(
    burgundy = TwoHeartsTokens.Color.burgundy,
    burgundyLight = TwoHeartsTokens.Color.burgundyLight,
    burgundyDark = TwoHeartsTokens.Color.burgundyDark,
    burgundy50 = TwoHeartsTokens.Color.burgundy50,
    burgundy100 = TwoHeartsTokens.Color.burgundy100,
    burgundy200 = TwoHeartsTokens.Color.burgundy200,
    burgundy300 = TwoHeartsTokens.Color.burgundy300,
    burgundy400 = TwoHeartsTokens.Color.burgundy400,
    burgundy500 = TwoHeartsTokens.Color.burgundy500,
    cream = TwoHeartsTokens.Color.cream,
    blush = TwoHeartsTokens.Color.blush,
    roseMuted = TwoHeartsTokens.Color.roseMuted,
    pink = TwoHeartsTokens.Color.pink,
    beige = TwoHeartsTokens.Color.beige,
    charcoal = TwoHeartsTokens.Color.charcoal,
    neutralSoft = TwoHeartsTokens.Color.neutralSoft,
    warmIvory = TwoHeartsTokens.Color.warmIvory,
    dustyRose = TwoHeartsTokens.Color.dustyRose,
    plum = TwoHeartsTokens.Color.plum,
    sage = TwoHeartsTokens.Color.sage,
    surfaceElevated = TwoHeartsTokens.Color.surfaceElevated,
    surfaceWarm = TwoHeartsTokens.Color.surfaceWarm,
    surfaceBlush = TwoHeartsTokens.Color.surfaceBlush,
    textSecondary = TwoHeartsTokens.Color.textSecondary,
    textTertiary = TwoHeartsTokens.Color.textTertiary,
    textOnAccent = TwoHeartsTokens.Color.textOnAccent,
    divider = TwoHeartsTokens.Color.divider,
    overlay = TwoHeartsTokens.Color.overlay,
    overlaySoft = TwoHeartsTokens.Color.overlaySoft,
    success = TwoHeartsTokens.Color.success,
    successBg = TwoHeartsTokens.Color.successBg,
    warning = TwoHeartsTokens.Color.warning,
    error = TwoHeartsTokens.Color.error,
    errorBg = TwoHeartsTokens.Color.errorBg,
)

private val DarkExtendedColors = TwoHeartsColors(
    burgundy = TwoHeartsTokens.Color.Dark.burgundy,
    burgundyLight = TwoHeartsTokens.Color.Dark.burgundyLight,
    burgundyDark = TwoHeartsTokens.Color.Dark.burgundyDark,
    burgundy50 = TwoHeartsTokens.Color.burgundy50,
    burgundy100 = TwoHeartsTokens.Color.burgundy100,
    burgundy200 = TwoHeartsTokens.Color.burgundy200,
    burgundy300 = TwoHeartsTokens.Color.burgundy300,
    burgundy400 = TwoHeartsTokens.Color.burgundy400,
    burgundy500 = TwoHeartsTokens.Color.burgundy500,
    cream = TwoHeartsTokens.Color.Dark.cream,
    blush = TwoHeartsTokens.Color.Dark.blush,
    roseMuted = TwoHeartsTokens.Color.roseMuted,
    pink = TwoHeartsTokens.Color.Dark.pink,
    beige = TwoHeartsTokens.Color.Dark.beige,
    charcoal = TwoHeartsTokens.Color.Dark.charcoal,
    neutralSoft = TwoHeartsTokens.Color.Dark.neutralSoft,
    warmIvory = TwoHeartsTokens.Color.warmIvory,
    dustyRose = TwoHeartsTokens.Color.dustyRose,
    plum = TwoHeartsTokens.Color.plum,
    sage = TwoHeartsTokens.Color.sage,
    surfaceElevated = TwoHeartsTokens.Color.Dark.surfaceElevated,
    surfaceWarm = TwoHeartsTokens.Color.Dark.surfaceWarm,
    surfaceBlush = TwoHeartsTokens.Color.Dark.surfaceBlush,
    textSecondary = TwoHeartsTokens.Color.Dark.textSecondary,
    textTertiary = TwoHeartsTokens.Color.Dark.textTertiary,
    textOnAccent = TwoHeartsTokens.Color.Dark.textOnAccent,
    divider = TwoHeartsTokens.Color.Dark.divider,
    overlay = TwoHeartsTokens.Color.Dark.overlay,
    overlaySoft = TwoHeartsTokens.Color.Dark.overlaySoft,
    success = TwoHeartsTokens.Color.success,
    successBg = TwoHeartsTokens.Color.Dark.successBg,
    warning = TwoHeartsTokens.Color.warning,
    error = TwoHeartsTokens.Color.error,
    errorBg = TwoHeartsTokens.Color.Dark.errorBg,
)

// Material 3 Light Color Scheme — matching legacy tokens exactly
private val LightColorScheme = lightColorScheme(
    primary = TwoHeartsTokens.Color.burgundy,
    onPrimary = Color.White,
    primaryContainer = TwoHeartsTokens.Color.blush,
    onPrimaryContainer = TwoHeartsTokens.Color.burgundyDark,
    secondary = TwoHeartsTokens.Color.roseMuted,
    onSecondary = Color.White,
    secondaryContainer = TwoHeartsTokens.Color.surfaceBlush,
    onSecondaryContainer = TwoHeartsTokens.Color.burgundy,
    tertiary = TwoHeartsTokens.Color.plum,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E0EE),
    onTertiaryContainer = TwoHeartsTokens.Color.plum,
    error = TwoHeartsTokens.Color.error,
    onError = Color.White,
    errorContainer = TwoHeartsTokens.Color.errorBg,
    onErrorContainer = TwoHeartsTokens.Color.error,
    background = TwoHeartsTokens.Color.cream,
    onBackground = TwoHeartsTokens.Color.charcoal,
    surface = TwoHeartsTokens.Color.surface,
    onSurface = TwoHeartsTokens.Color.charcoal,
    surfaceVariant = TwoHeartsTokens.Color.surfaceBlush,
    onSurfaceVariant = TwoHeartsTokens.Color.textSecondary,
    outline = TwoHeartsTokens.Color.border,
    outlineVariant = TwoHeartsTokens.Color.divider,
    inverseSurface = TwoHeartsTokens.Color.charcoal,
    inverseOnSurface = TwoHeartsTokens.Color.cream,
    inversePrimary = TwoHeartsTokens.Color.burgundyLight,
    surfaceTint = TwoHeartsTokens.Color.burgundy,
)

// Material 3 Dark Color Scheme — warm dark surfaces from charcoal/plum family
private val DarkColorScheme = darkColorScheme(
    primary = TwoHeartsTokens.Color.Dark.burgundy,
    onPrimary = Color.White,
    primaryContainer = TwoHeartsTokens.Color.Dark.burgundyDark,
    onPrimaryContainer = TwoHeartsTokens.Color.Dark.blush,
    secondary = TwoHeartsTokens.Color.roseMuted,
    onSecondary = Color.White,
    secondaryContainer = TwoHeartsTokens.Color.Dark.surfaceBlush,
    onSecondaryContainer = TwoHeartsTokens.Color.Dark.pink,
    tertiary = TwoHeartsTokens.Color.dustyRose,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3D2535),
    onTertiaryContainer = TwoHeartsTokens.Color.dustyRose,
    error = TwoHeartsTokens.Color.error,
    onError = Color.White,
    errorContainer = TwoHeartsTokens.Color.Dark.errorBg,
    onErrorContainer = Color(0xFFFFB4AB),
    background = TwoHeartsTokens.Color.Dark.bg,
    onBackground = TwoHeartsTokens.Color.Dark.textPrimary,
    surface = TwoHeartsTokens.Color.Dark.surface,
    onSurface = TwoHeartsTokens.Color.Dark.textPrimary,
    surfaceVariant = TwoHeartsTokens.Color.Dark.surfaceBlush,
    onSurfaceVariant = TwoHeartsTokens.Color.Dark.textSecondary,
    outline = TwoHeartsTokens.Color.Dark.border,
    outlineVariant = TwoHeartsTokens.Color.Dark.divider,
    inverseSurface = TwoHeartsTokens.Color.Dark.textPrimary,
    inverseOnSurface = TwoHeartsTokens.Color.Dark.bg,
    inversePrimary = TwoHeartsTokens.Color.Dark.burgundy,
    surfaceTint = TwoHeartsTokens.Color.Dark.burgundy,
)

/**
 * TwoHeartsTheme — The complete design system theme.
 *
 * Provides:
 * - Material 3 color scheme (light/dark)
 * - Extended brand colors via LocalTwoHeartsColors
 * - Typography (TwoHeartsTypography)
 * - Text scaling (via Density multiplier)
 *
 * Follows legacy tokens.css exactly for all color values.
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use Material You dynamic colors (default true)
 * @param textSizeScale Text size multiplier for accessibility
 * @param content Composable content
 */
@Composable
fun TwoHeartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    textSizeScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // Apply text size scaling via Density
    val currentDensity = LocalDensity.current
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * textSizeScale
    )

    CompositionLocalProvider(
        LocalTwoHeartsColors provides extendedColors,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TwoHeartsTypography,
            content = content
        )
    }
}

/**
 * Stage 2 convenience overload with named parameters for toggle-based previews.
 *
 * @param darkMode Whether dark mode is active
 * @param textScalingLevel Text scaling level preset
 */
@Composable
fun TwoHeartsTheme(
    darkMode: Boolean,
    textScalingLevel: TextScalingLevel = TextScalingLevel.DEFAULT,
    content: @Composable () -> Unit
) {
    TwoHeartsTheme(
        darkTheme = darkMode,
        dynamicColor = false,
        textSizeScale = textScalingLevel.scale,
        content = content
    )
}

/**
 * Convenience accessor for the extended TwoHearts color palette.
 * Usage: val thColors = LocalTwoHeartsColors.current
 */
val MaterialTheme.thColors: TwoHeartsColors
    @Composable
    get() = LocalTwoHeartsColors.current
