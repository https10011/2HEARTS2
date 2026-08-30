package com.twohearts.app.ui.theme

import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TwoHearts Design Tokens — Typed constants matching legacy tokens.css
 *
 * This file is the Kotlin equivalent of the CSS custom properties defined
 * in the legacy tokens.css. All values are preserved exactly from the
 * original design system (Stage 10 enhanced version).
 *
 * Source of truth: Archive/Legacy-React-Vite-Capacitor/src/theme/tokens.css
 */
object TwoHeartsTokens {

    // ─── Color Palette ───────────────────────────────────────────────

    object Color {
        // Brand palette — primitives
        val burgundy = ComposeColor(0xFF6A1B2B)
        val burgundyLight = ComposeColor(0xFF8E3147)
        val burgundyDark = ComposeColor(0xFF4A0F1D)

        // Extended burgundy family
        val burgundy50 = ComposeColor(0xFFF9E8EB)
        val burgundy100 = ComposeColor(0xFFEFC4CC)
        val burgundy200 = ComposeColor(0xFFD98A99)
        val burgundy300 = ComposeColor(0xFFB85C6E)
        val burgundy400 = ComposeColor(0xFF9A3D52)
        val burgundy500 = ComposeColor(0xFF7D2439)
        val burgundy600 = ComposeColor(0xFF6A1B2B)
        val burgundy700 = ComposeColor(0xFF521423)
        val burgundy800 = ComposeColor(0xFF3D0E1A)
        val burgundy900 = ComposeColor(0xFF2A0A12)

        // Warm neutrals / supporting palette
        val cream = ComposeColor(0xFFFDF6F0)
        val blush = ComposeColor(0xFFF6E1DE)
        val roseMuted = ComposeColor(0xFFC9808B)
        val pink = ComposeColor(0xFFE8A0B4)
        val beige = ComposeColor(0xFFEDE0D4)
        val charcoal = ComposeColor(0xFF2B2420)
        val neutralSoft = ComposeColor(0xFFF2E9E4)
        val warmIvory = ComposeColor(0xFFFBF4ED)
        val dustyRose = ComposeColor(0xFFC9A0A8)
        val plum = ComposeColor(0xFF7A3F5E)
        val sage = ComposeColor(0xFF8B9E7C)

        // Semantic surface tokens — light theme
        val bg = cream
        val surface = ComposeColor.White
        val surfaceElevated = ComposeColor(0xFFFFFDFB)
        val surfaceWarm = ComposeColor(0xFFFFF8F4)
        val surfaceBlush = ComposeColor(0xFFFFF0EC)
        val textPrimary = charcoal
        val textSecondary = ComposeColor(0xFF6B5D58)
        val textTertiary = ComposeColor(0xFF9A8D87)
        val textOnAccent = ComposeColor(0xFFFFF8F3)
        val border = ComposeColor(0xFFE8DAD3)
        val divider = ComposeColor(0xFFEFE3DC)
        val overlay = ComposeColor(0x802B2420) // rgba(43, 36, 32, 0.5)
        val overlaySoft = ComposeColor(0x402B2420) // rgba(43, 36, 32, 0.25)

        // Feedback
        val success = ComposeColor(0xFF4F7A5A)
        val successBg = ComposeColor(0xFFE9F3EA)
        val warning = ComposeColor(0xFFB07A1E)
        val error = ComposeColor(0xFFA33A2A)
        val errorBg = ComposeColor(0xFFF7E7E3)

        // Dark theme overrides
        object Dark {
            val bg = ComposeColor(0xFF1A1310)
            val cream = ComposeColor(0xFF1A1310)
            val surface = ComposeColor(0xFF241E1A)
            val surfaceElevated = ComposeColor(0xFF2E2622)
            val surfaceWarm = ComposeColor(0xFF2A2220)
            val surfaceBlush = ComposeColor(0xFF352A28)
            val blush = ComposeColor(0xFF3A2A28)
            val neutralSoft = ComposeColor(0xFF2E2620)
            val beige = ComposeColor(0xFF463A32)
            val pink = ComposeColor(0xFFB08797)
            val textPrimary = ComposeColor(0xFFF5ECE4)
            val charcoal = ComposeColor(0xFFF5ECE4)
            val textSecondary = ComposeColor(0xFFC4B2A9)
            val textTertiary = ComposeColor(0xFF8A7D77)
            val textOnAccent = ComposeColor(0xFFFFF8F3)
            val border = ComposeColor(0xFF3E3230)
            val divider = ComposeColor(0xFF352B28)
            val burgundy = ComposeColor(0xFFC9808B)
            val burgundyLight = ComposeColor(0xFFD99AA6)
            val burgundyDark = ComposeColor(0xFF7E2C40)
            val errorBg = ComposeColor(0xFF452019)
            val successBg = ComposeColor(0xFF25392B)
            val overlay = ComposeColor(0xA6000000) // rgba(0, 0, 0, 0.65)
            val overlaySoft = ComposeColor(0x59000000) // rgba(0, 0, 0, 0.35)
        }
    }

    // ─── Typography ──────────────────────────────────────────────────

    object Typography {
        val fontFamilyBase = "sans-serif"
        val fontFamilyDisplay = "serif"

        val sizeXs = 12.sp
        val sizeSm = 13.sp
        val sizeMd = 16.sp
        val sizeLg = 18.sp
        val sizeXl = 22.sp
        val size2xl = 26.sp
        val size3xl = 32.sp
        val size4xl = 40.sp

        val weightRegular = 400
        val weightMedium = 500
        val weightSemibold = 600
        val weightBold = 700
    }

    // ─── Spacing (4pt base) ──────────────────────────────────────────

    object Spacing {
        val space0 = 0.dp
        val space1 = 4.dp
        val space2 = 8.dp
        val space3 = 12.dp
        val space4 = 16.dp
        val space5 = 20.dp
        val space6 = 24.dp
        val space8 = 32.dp
        val space10 = 40.dp
        val space12 = 48.dp
        val space16 = 64.dp
        val space20 = 80.dp
    }

    // ─── Corner Radii ────────────────────────────────────────────────

    object Radius {
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val pill = 9999.dp
    }

    // ─── Component Dimensions ────────────────────────────────────────

    object Dimensions {
        val touchTargetMin = 44.dp
        val headerHeight = 56.dp
        val bottomNavHeight = 64.dp
        val screenMaxWidth = 480.dp
        val navCenterSize = 58.dp
        val avatarLg = 72.dp
    }

    // ─── Motion Durations (ms) ───────────────────────────────────────

    object Duration {
        const val instant = 1L
        const val fast = 100L
        const val normal = 200L
        const val slow = 320L
        const val drift = 6400L
        const val spin = 600L
    }

    // ─── Easing Curves ───────────────────────────────────────────────

    object Ease {
        // Note: Compose uses specific easing types, not CSS cubic-bezier strings
        // These are the conceptual values — Compose equivalents are used in theme
        const val standard = "cubic-bezier(0.25, 0.46, 0.45, 0.94)"
        const val decelerate = "cubic-bezier(0, 0, 0.2, 1)"
        const val accelerate = "cubic-bezier(0.4, 0, 1, 1)"
        const val emphasized = "cubic-bezier(0.2, 0, 0, 1)"
        const val press = "cubic-bezier(0.3, 0, 0.2, 1)"
        const val spring = "cubic-bezier(0.34, 1.56, 0.64, 1)"
    }

    // ─── Z-Index Layers ──────────────────────────────────────────────

    object ZIndex {
        const val base = 0f
        const val content = 1f
        const val sticky = 5f
        const val nav = 10f
        const val fab = 100f
        const val modal = 1000f
        const val lock = 1100f
    }

    // ─── Text Size Scaling ───────────────────────────────────────────

    /**
     * System-wide text-size setting (MasterPrompt §28).
     * Maps a user-facing option to a scale multiplier.
     */
    enum class TextSizeKey(val label: String, val scale: Float) {
        SMALL("Small", 0.88f),
        DEFAULT("Default", 1f),
        LARGE("Large", 1.12f),
        EXTRA_LARGE("Extra Large", 1.28f)
    }
}
