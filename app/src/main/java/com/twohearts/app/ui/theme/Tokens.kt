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
        /**
         * Tertiary text — captions, timestamps, helper copy.
         * Phase 1: darkened from #9A8D87 (3.00:1 on cream — below the 4.5:1
         * WCAG AA body-text floor) to a value that clears it on every light
         * surface in the system.
         */
        val textTertiary = ComposeColor(0xFF75695F)
        val textOnAccent = ComposeColor(0xFFFFF8F3)

        /**
         * Border hierarchy. The original single `border` token (#E8DAD3)
         * measured 1.27:1 on cream, so it was invisible as a form-field
         * outline. Phase 1 splits the role in two:
         *
         *  - borderSubtle — hairlines and decorative separation only.
         *  - borderStrong — interactive boundaries (inputs, outlined
         *    controls, selected chips). Clears the 3:1 non-text floor.
         */
        val border = ComposeColor(0xFFE8DAD3)
        val borderSubtle = ComposeColor(0xFFE6D8D0)
        val borderStrong = ComposeColor(0xFF9E8878)
        val divider = ComposeColor(0xFFEFE3DC)
        val overlay = ComposeColor(0x802B2420) // rgba(43, 36, 32, 0.5)
        val overlaySoft = ComposeColor(0x402B2420) // rgba(43, 36, 32, 0.25)

        /**
         * Text-safe accent variants. `roseMuted`, `dustyRose`, `pink` and
         * `sage` remain decorative fills (tinted bands, illustration, mood
         * dots) but are too light to carry text. These deeper siblings are
         * the ones to use behind or for text.
         */
        val roseDeep = ComposeColor(0xFF9E4E60)
        val goldInk = ComposeColor(0xFF8F6B26)
        val sageInk = ComposeColor(0xFF5F7252)

        /** Restrained gold — decorative rules and relationship accents. */
        val gold = ComposeColor(0xFFC9A227)
        val goldSoft = ComposeColor(0xFFE8D9A8)
        val goldBg = ComposeColor(0xFFFBF3DD)

        // Feedback
        val success = ComposeColor(0xFF42704E)
        val successBg = ComposeColor(0xFFE9F3EA)
        val warning = ComposeColor(0xFF8A5E14)
        val warningBg = ComposeColor(0xFFFBF1DF)
        val error = ComposeColor(0xFF932F21)
        val errorBg = ComposeColor(0xFFF7E7E3)
        val info = ComposeColor(0xFF44607A)
        val infoBg = ComposeColor(0xFFE8EFF5)

        /**
         * Warm dark surface for transient overlays that must stay dark in
         * both themes (today: the info toast), so a white label always
         * reads against it.
         */
        val inkBase = ComposeColor(0xFF3A302C)

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
            val textTertiary = ComposeColor(0xFFA99A92)
            val textOnAccent = ComposeColor(0xFFFFF8F3)
            val border = ComposeColor(0xFF3E3230)
            val borderSubtle = ComposeColor(0xFF352B28)
            val borderStrong = ComposeColor(0xFF8A7568)
            val divider = ComposeColor(0xFF352B28)
            val burgundy = ComposeColor(0xFFC9808B)
            val burgundyLight = ComposeColor(0xFFD99AA6)
            val burgundyDark = ComposeColor(0xFF7E2C40)
            val roseDeep = ComposeColor(0xFFE0A3B0)
            val goldInk = ComposeColor(0xFFD8B25E)
            val sageInk = ComposeColor(0xFFA8BC96)
            val gold = ComposeColor(0xFFCBA83E)
            val goldSoft = ComposeColor(0xFF5A4A2A)
            val goldBg = ComposeColor(0xFF3A3121)
            val success = ComposeColor(0xFF8FC49C)
            val warning = ComposeColor(0xFFE0B45E)
            val warningBg = ComposeColor(0xFF3D2F17)
            val error = ComposeColor(0xFFF09A88)
            val errorBg = ComposeColor(0xFF452019)
            val info = ComposeColor(0xFF9DBAD4)
            val infoBg = ComposeColor(0xFF24303B)
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

        /**
         * Semantic spacing roles. Using these (rather than raw steps) keeps
         * grouping consistent across screens — the raw scale above stays
         * available for one-off composition.
         */
        /** Screen gutter — left/right inset for all screen content. */
        val screenGutter = space4
        /** Gap between sibling cards / list items in a group. */
        val itemGap = space3
        /** Gap between distinct sections of a screen. */
        val sectionGap = space6
        /** Padding inside a card. */
        val cardPadding = space4
        /** Gap between a label and the control it describes. */
        val labelGap = space2
        /** Gap between tightly related lines (title + caption). */
        val tightGap = space1
    }

    // ─── Corner Radii ────────────────────────────────────────────────

    /**
     * Shape scale. Phase 1 keeps the legacy steps but assigns each a
     * distinct job so screens stop looking like a wall of identical
     * rounded rectangles:
     *
     *  - sm   — inputs, chips, small controls
     *  - md   — buttons, banners, inner surfaces
     *  - lg   — cards, list groups
     *  - xl   — sheets, hero panels
     *  - xxl  — full-bleed decorative panels only
     *  - pill — compact controls and tags ONLY (never large containers)
     */
    object Radius {
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val pill = 9999.dp
    }

    // ─── Elevation ───────────────────────────────────────────────────

    /**
     * A deliberately small elevation ladder. Phase 1 moves most surfaces
     * from shadow to a 1dp border, so shadow is reserved for things that
     * genuinely float above the page.
     *
     *  - flat  — nothing (most surfaces; separate with border or tone)
     *  - hair  — barely-there lift for grouped list cards
     *  - raise — interactive cards and FABs
     *  - lift  — modal/sheet surfaces
     */
    object Elevation {
        val flat = 0.dp
        val hair = 1.dp
        val raise = 3.dp
        val lift = 8.dp
    }

    /** Hairline border widths. */
    object Border {
        /** Decorative separation and card outlines. */
        val hairline = 1.dp
        /** Focus / selected / interactive emphasis. */
        val strong = 1.5.dp
    }

    // ─── Motion Durations (ms) ───────────────────────────────────────

    /**
     * Motion durations. Phase 1 keeps these short and consistent — the
     * previous screens each picked their own numbers, so identical
     * interactions felt different depending on where you were.
     *
     * Decorative entrances should be scaled through
     * [com.twohearts.app.ui.theme.LocalTwoHeartsMotion] so reduced-motion
     * users are respected.
     */
    object Duration {
        /** Press / hover feedback. Fast enough to feel immediate. */
        const val instant = 1L
        /** Very small state changes. */
        const val fast = 100L
        /** Small state changes — chips, selections, reveals. */
        const val quick = 160L
        /** Standard transition — screen and content entrance. */
        const val normal = 200L
        const val standard = 240L
        /** Larger movements — sheets, dialogs, full-screen transitions. */
        const val slow = 320L
        const val gentle = 320L
        /** Ambient/looping decoration (background drift, Yuki idle). */
        const val drift = 6400L
        /** One full turn of a progress spinner. */
        const val spin = 600L
        /** How long a toast stays fully visible. */
        const val toastVisible = 2400L
    }

    // ─── Component Dimensions ────────────────────────────────────────

    object Dimensions {
        /** Minimum comfortable touch target (WCAG 2.5.8 / Material). */
        val touchTargetMin = 44.dp
        val headerHeight = 56.dp
        val bottomNavHeight = 64.dp
        val screenMaxWidth = 480.dp
        val navCenterSize = 58.dp
        val avatarLg = 72.dp
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
