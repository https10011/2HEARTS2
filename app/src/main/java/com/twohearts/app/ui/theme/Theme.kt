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
    // Text-safe accent variants (see Tokens.Color docs)
    val roseDeep: Color,
    val goldInk: Color,
    val sageInk: Color,
    // Restrained gold relationship accent
    val gold: Color,
    val goldSoft: Color,
    val goldBg: Color,
    // Semantic surfaces
    val surfaceElevated: Color,
    val surfaceWarm: Color,
    val surfaceBlush: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val divider: Color,
    val overlay: Color,
    val overlaySoft: Color,
    // Feedback
    val success: Color,
    val successBg: Color,
    val warning: Color,
    val warningBg: Color,
    val error: Color,
    val errorBg: Color,
    val info: Color,
    val infoBg: Color,
    /** Dark overlay ink that stays dark in both themes (info toast). */
    val inkBase: Color,
)

/**
 * Motion policy for the app. Phase 1 introduces this so components can
 * consult ONE source of truth instead of each screen inventing its own
 * reduce-motion handling.
 *
 * When [reduced] is true, decorative motion (entrances, staggered reveals,
 * scale/bounce accents, drifting background art) is suppressed while
 * essential feedback (loading spinner, state change, press ripple) stays.
 */
@Immutable
data class TwoHeartsMotion(
    val reduced: Boolean = false,
) {
    /** Duration to use for a decorative/entrance animation. */
    fun decorative(durationMs: Long): Long = if (reduced) 0L else durationMs

    /** Duration to use for motion that carries meaning. */
    fun functional(durationMs: Long): Long =
        if (reduced) (durationMs / 2).coerceAtLeast(90L) else durationMs
}

val LocalTwoHeartsMotion = staticCompositionLocalOf { TwoHeartsMotion() }

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
        roseDeep = Color.Unspecified,
        goldInk = Color.Unspecified,
        sageInk = Color.Unspecified,
        gold = Color.Unspecified,
        goldSoft = Color.Unspecified,
        goldBg = Color.Unspecified,
        surfaceElevated = Color.Unspecified,
        surfaceWarm = Color.Unspecified,
        surfaceBlush = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textTertiary = Color.Unspecified,
        textOnAccent = Color.Unspecified,
        borderSubtle = Color.Unspecified,
        borderStrong = Color.Unspecified,
        divider = Color.Unspecified,
        overlay = Color.Unspecified,
        overlaySoft = Color.Unspecified,
        success = Color.Unspecified,
        successBg = Color.Unspecified,
        warning = Color.Unspecified,
        warningBg = Color.Unspecified,
        error = Color.Unspecified,
        errorBg = Color.Unspecified,
        info = Color.Unspecified,
        infoBg = Color.Unspecified,
        inkBase = Color.Unspecified,
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
    roseDeep = TwoHeartsTokens.Color.roseDeep,
    goldInk = TwoHeartsTokens.Color.goldInk,
    sageInk = TwoHeartsTokens.Color.sageInk,
    gold = TwoHeartsTokens.Color.gold,
    goldSoft = TwoHeartsTokens.Color.goldSoft,
    goldBg = TwoHeartsTokens.Color.goldBg,
    surfaceElevated = TwoHeartsTokens.Color.surfaceElevated,
    surfaceWarm = TwoHeartsTokens.Color.surfaceWarm,
    surfaceBlush = TwoHeartsTokens.Color.surfaceBlush,
    textSecondary = TwoHeartsTokens.Color.textSecondary,
    textTertiary = TwoHeartsTokens.Color.textTertiary,
    textOnAccent = TwoHeartsTokens.Color.textOnAccent,
    borderSubtle = TwoHeartsTokens.Color.borderSubtle,
    borderStrong = TwoHeartsTokens.Color.borderStrong,
    divider = TwoHeartsTokens.Color.divider,
    overlay = TwoHeartsTokens.Color.overlay,
    overlaySoft = TwoHeartsTokens.Color.overlaySoft,
    success = TwoHeartsTokens.Color.success,
    successBg = TwoHeartsTokens.Color.successBg,
    warning = TwoHeartsTokens.Color.warning,
    warningBg = TwoHeartsTokens.Color.warningBg,
    error = TwoHeartsTokens.Color.error,
    errorBg = TwoHeartsTokens.Color.errorBg,
    info = TwoHeartsTokens.Color.info,
    infoBg = TwoHeartsTokens.Color.infoBg,
    inkBase = TwoHeartsTokens.Color.inkBase,
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
    roseDeep = TwoHeartsTokens.Color.Dark.roseDeep,
    goldInk = TwoHeartsTokens.Color.Dark.goldInk,
    sageInk = TwoHeartsTokens.Color.Dark.sageInk,
    gold = TwoHeartsTokens.Color.Dark.gold,
    goldSoft = TwoHeartsTokens.Color.Dark.goldSoft,
    goldBg = TwoHeartsTokens.Color.Dark.goldBg,
    surfaceElevated = TwoHeartsTokens.Color.Dark.surfaceElevated,
    surfaceWarm = TwoHeartsTokens.Color.Dark.surfaceWarm,
    surfaceBlush = TwoHeartsTokens.Color.Dark.surfaceBlush,
    textSecondary = TwoHeartsTokens.Color.Dark.textSecondary,
    textTertiary = TwoHeartsTokens.Color.Dark.textTertiary,
    textOnAccent = TwoHeartsTokens.Color.Dark.textOnAccent,
    borderSubtle = TwoHeartsTokens.Color.Dark.borderSubtle,
    borderStrong = TwoHeartsTokens.Color.Dark.borderStrong,
    divider = TwoHeartsTokens.Color.Dark.divider,
    overlay = TwoHeartsTokens.Color.Dark.overlay,
    overlaySoft = TwoHeartsTokens.Color.Dark.overlaySoft,
    success = TwoHeartsTokens.Color.Dark.success,
    successBg = TwoHeartsTokens.Color.Dark.successBg,
    warning = TwoHeartsTokens.Color.Dark.warning,
    warningBg = TwoHeartsTokens.Color.Dark.warningBg,
    error = TwoHeartsTokens.Color.Dark.error,
    errorBg = TwoHeartsTokens.Color.Dark.errorBg,
    info = TwoHeartsTokens.Color.Dark.info,
    infoBg = TwoHeartsTokens.Color.Dark.infoBg,
    inkBase = TwoHeartsTokens.Color.inkBase,
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
    outline = TwoHeartsTokens.Color.borderStrong,
    outlineVariant = TwoHeartsTokens.Color.borderSubtle,
    inverseSurface = TwoHeartsTokens.Color.charcoal,
    inverseOnSurface = TwoHeartsTokens.Color.cream,
    inversePrimary = TwoHeartsTokens.Color.burgundyLight,

    /**
     * Phase 1 — the surface ladder. Anything left unset here falls back to
     * Material's stock baseline (which is a desaturated purple-grey), and
     * that is what made migrated screens read as "generic Material UI".
     * Every slot is now an explicit warm TwoHearts value.
     */
    surfaceBright = TwoHeartsTokens.Color.surface,
    surfaceDim = TwoHeartsTokens.Color.beige,
    surfaceContainer = TwoHeartsTokens.Color.surfaceWarm,
    surfaceContainerHigh = TwoHeartsTokens.Color.surfaceElevated,
    surfaceContainerHighest = TwoHeartsTokens.Color.blush,
    surfaceContainerLow = TwoHeartsTokens.Color.surfaceWarm,
    surfaceContainerLowest = TwoHeartsTokens.Color.surface,

    // Neutral scrim — stock M3 tints this with the seed hue, which pulls a
    // purple cast into ripples, scrims and shadows.
    scrim = Color.Black,
    surfaceTint = TwoHeartsTokens.Color.burgundy,
)

// Material 3 Dark Color Scheme — warm dark surfaces from charcoal/plum family
private val DarkColorScheme = darkColorScheme(
    primary = TwoHeartsTokens.Color.Dark.burgundy,
    onPrimary = TwoHeartsTokens.Color.Dark.bg,
    primaryContainer = TwoHeartsTokens.Color.Dark.burgundyDark,
    onPrimaryContainer = TwoHeartsTokens.Color.Dark.blush,
    secondary = TwoHeartsTokens.Color.Dark.roseDeep,
    onSecondary = TwoHeartsTokens.Color.Dark.bg,
    secondaryContainer = TwoHeartsTokens.Color.Dark.surfaceBlush,
    onSecondaryContainer = TwoHeartsTokens.Color.Dark.pink,
    tertiary = TwoHeartsTokens.Color.dustyRose,
    onTertiary = TwoHeartsTokens.Color.Dark.bg,
    tertiaryContainer = Color(0xFF3D2535),
    onTertiaryContainer = TwoHeartsTokens.Color.dustyRose,
    error = TwoHeartsTokens.Color.Dark.error,
    onError = TwoHeartsTokens.Color.Dark.bg,
    errorContainer = TwoHeartsTokens.Color.Dark.errorBg,
    onErrorContainer = Color(0xFFFFB4AB),
    background = TwoHeartsTokens.Color.Dark.bg,
    onBackground = TwoHeartsTokens.Color.Dark.textPrimary,
    surface = TwoHeartsTokens.Color.Dark.surface,
    onSurface = TwoHeartsTokens.Color.Dark.textPrimary,
    surfaceVariant = TwoHeartsTokens.Color.Dark.surfaceBlush,
    onSurfaceVariant = TwoHeartsTokens.Color.Dark.textSecondary,
    outline = TwoHeartsTokens.Color.Dark.borderStrong,
    outlineVariant = TwoHeartsTokens.Color.Dark.borderSubtle,
    inverseSurface = TwoHeartsTokens.Color.Dark.textPrimary,
    inverseOnSurface = TwoHeartsTokens.Color.Dark.bg,
    inversePrimary = TwoHeartsTokens.Color.Dark.burgundy,

    // Surface ladder — see the light scheme note.
    surfaceBright = TwoHeartsTokens.Color.Dark.surfaceElevated,
    surfaceDim = TwoHeartsTokens.Color.Dark.bg,
    surfaceContainer = TwoHeartsTokens.Color.Dark.surface,
    surfaceContainerHigh = TwoHeartsTokens.Color.Dark.surfaceElevated,
    surfaceContainerHighest = TwoHeartsTokens.Color.Dark.surfaceBlush,
    surfaceContainerLow = TwoHeartsTokens.Color.Dark.surface,
    surfaceContainerLowest = TwoHeartsTokens.Color.Dark.bg,

    scrim = Color.Black,
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
 * @param dynamicColor Whether to use Material You dynamic colors.
 *   Phase 1: defaults to **false**. TwoHearts' burgundy identity is the
 *   product's, not the user's wallpaper, and dynamic colour silently
 *   replaced the entire brand palette on Android 12+ devices. Kept as an
 *   opt-in parameter so a future "use my wallpaper colours" setting can
 *   still be offered.
 * @param textSizeScale Text size multiplier for accessibility
 * @param reduceMotion Whether decorative motion should be suppressed
 * @param content Composable content
 */
@Composable
fun TwoHeartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    textSizeScale: Float = 1f,
    reduceMotion: Boolean = false,
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
        LocalTwoHeartsMotion provides TwoHeartsMotion(reduced = reduceMotion),
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TwoHeartsTypography,
            shapes = TwoHeartsShapes,
            content = content
        )
    }
}

/**
 * Convenience overload with named parameters for toggle-based previews.
 *
 * @param darkMode Whether dark mode is active
 * @param textScalingLevel Text scaling level preset
 * @param reduceMotion Whether decorative motion should be suppressed
 */
@Composable
fun TwoHeartsTheme(
    darkMode: Boolean,
    textScalingLevel: TextScalingLevel = TextScalingLevel.DEFAULT,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    TwoHeartsTheme(
        darkTheme = darkMode,
        dynamicColor = false,
        textSizeScale = textScalingLevel.scale,
        reduceMotion = reduceMotion,
        content = content
    )
}

/**
 * Maps the persisted appearance settings onto the theme.
 *
 * Phase 1: the Appearance settings screen wrote `themeMode`, `textSize` and
 * `reduceMotion` to DataStore, and [com.twohearts.app.services.appstate.AppStateService]
 * exposed them as state — but nothing ever read them back into the theme.
 * `MainActivity` hardcoded `darkMode = false` and
 * `TextScalingLevel.DEFAULT`, so choosing "Dark" or "Large" changed nothing
 * until the process restarted, and then was ignored again. This overload
 * closes that loop.
 *
 * @param darkMode resolved dark-mode flag (caller decides what "system" means)
 * @param textSize persisted text-size key: "small" | "default" | "large" | "extra-large"
 * @param reduceMotion persisted reduce-motion preference
 */
@Composable
fun TwoHeartsTheme(
    darkMode: Boolean,
    textSize: String,
    reduceMotion: Boolean,
    content: @Composable () -> Unit
) {
    TwoHeartsTheme(
        darkTheme = darkMode,
        dynamicColor = false,
        textSizeScale = textScalingLevelFor(textSize).scale,
        reduceMotion = reduceMotion,
        content = content
    )
}

/**
 * Resolves a persisted text-size key to a scaling level, falling back to
 * DEFAULT for any unrecognised value so a bad preference can never shrink
 * or blow up the UI.
 *
 * Both `extra-large` and `extra_large` are accepted: the Appearance screen
 * historically wrote the underscore form while other call sites used the
 * hyphenated one, and a silent mismatch here would leave the largest text
 * setting doing nothing.
 */
fun textScalingLevelFor(key: String): TextScalingLevel = when (key) {
    "small" -> TextScalingLevel.SMALL
    "large" -> TextScalingLevel.LARGE
    "extra-large", "extra_large" -> TextScalingLevel.EXTRA_LARGE
    else -> TextScalingLevel.DEFAULT
}

/**
 * Convenience accessor for the extended TwoHearts color palette.
 * Usage: val thColors = LocalTwoHeartsColors.current
 */
val MaterialTheme.thColors: TwoHeartsColors
    @Composable
    get() = LocalTwoHeartsColors.current

/**
 * Convenience accessor for the app's motion policy.
 * Usage: val motion = LocalTwoHeartsMotion.current
 */
val MaterialTheme.thMotion: TwoHeartsMotion
    @Composable
    get() = LocalTwoHeartsMotion.current
