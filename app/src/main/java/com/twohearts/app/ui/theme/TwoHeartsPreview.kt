package com.twohearts.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TwoHearts Design System Preview
 *
 * Renders the complete color palette, typography scale, spacing,
 * and component styles to verify the design system works correctly.
 * Used for Stage 1 verification.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TwoHeartsDesignPreview(modifier: Modifier = Modifier) {
    val thColors = LocalTwoHeartsColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "TwoHearts Design System",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        // ─── Brand Colors ────────────────────────────────────────────
        SectionTitle("Brand Colors")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch("Burgundy", thColors.burgundy)
            ColorSwatch("Burgundy Light", thColors.burgundyLight)
            ColorSwatch("Burgundy Dark", thColors.burgundyDark)
            ColorSwatch("Rose Muted", thColors.roseMuted)
            ColorSwatch("Blush", thColors.blush)
            ColorSwatch("Cream", thColors.cream)
        }

        // ─── Extended Burgundy ───────────────────────────────────────
        SectionTitle("Extended Burgundy Family")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch("50", thColors.burgundy50)
            ColorSwatch("100", thColors.burgundy100)
            ColorSwatch("200", thColors.burgundy200)
            ColorSwatch("300", thColors.burgundy300)
            ColorSwatch("400", thColors.burgundy400)
            ColorSwatch("500", thColors.burgundy500)
        }

        // ─── Neutrals ────────────────────────────────────────────────
        SectionTitle("Warm Neutrals")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch("Charcoal", thColors.charcoal)
            ColorSwatch("Beige", thColors.beige)
            ColorSwatch("Pink", thColors.pink)
            ColorSwatch("Neutral Soft", thColors.neutralSoft)
            ColorSwatch("Warm Ivory", thColors.warmIvory)
            ColorSwatch("Dusty Rose", thColors.dustyRose)
            ColorSwatch("Plum", thColors.plum)
            ColorSwatch("Sage", thColors.sage)
        }

        // ─── Surfaces ────────────────────────────────────────────────
        SectionTitle("Surfaces")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
            ColorSwatch("Elevated", thColors.surfaceElevated)
            ColorSwatch("Warm", thColors.surfaceWarm)
            ColorSwatch("Blush", thColors.surfaceBlush)
            ColorSwatch("Background", MaterialTheme.colorScheme.background)
        }

        // ─── Feedback ────────────────────────────────────────────────
        SectionTitle("Feedback Colors")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch("Success", thColors.success)
            ColorSwatch("Success BG", thColors.successBg)
            ColorSwatch("Warning", thColors.warning)
            ColorSwatch("Error", thColors.error)
            ColorSwatch("Error BG", thColors.errorBg)
        }

        // ─── Typography ──────────────────────────────────────────────
        SectionTitle("Typography Scale")

        Text(
            text = "Display Large — The quick brown fox",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "Display Medium — The quick brown fox",
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = "Display Small — The quick brown fox",
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = "Headline Large — The quick brown fox",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Headline Medium — The quick brown fox",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Headline Small — The quick brown fox",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Title Large — The quick brown fox",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Title Medium — The quick brown fox",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Title Small — The quick brown fox",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "Body Large — The quick brown fox jumps over the lazy dog",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Body Medium — The quick brown fox jumps over the lazy dog",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Body Small — The quick brown fox jumps over the lazy dog",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Label Large — ACTION",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "Label Medium — LABEL",
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = "Label Small — LABEL",
            style = MaterialTheme.typography.labelSmall
        )

        // ─── Spacing ─────────────────────────────────────────────────
        SectionTitle("Spacing Scale (4pt base)")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(
                "1" to 4.dp, "2" to 8.dp, "3" to 12.dp, "4" to 16.dp,
                "6" to 24.dp, "8" to 32.dp, "10" to 40.dp, "12" to 48.dp
            ).forEach { (label, size) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ─── Radii ───────────────────────────────────────────────────
        SectionTitle("Corner Radii")

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                "sm" to 8.dp, "md" to 12.dp, "lg" to 16.dp, "xl" to 24.dp
            ).forEach { (label, radius) ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(radius))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        // ─── Component Dimensions ────────────────────────────────────
        SectionTitle("Component Dimensions")

        Text(
            text = "Touch target min: ${TwoHeartsTokens.Dimensions.touchTargetMin}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Header height: ${TwoHeartsTokens.Dimensions.headerHeight}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Bottom nav height: ${TwoHeartsTokens.Dimensions.bottomNavHeight}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Avatar large: ${TwoHeartsTokens.Dimensions.avatarLg}",
            style = MaterialTheme.typography.bodyMedium
        )

        // ─── Text on Accent ──────────────────────────────────────────
        SectionTitle("Text on Accent")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Text on Primary — TwoHearts",
                color = thColors.textOnAccent,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Composable
private fun TwoHeartsDesignPreviewLight() {
    TwoHeartsTheme(darkTheme = false, dynamicColor = false) {
        TwoHeartsDesignPreview()
    }
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
private fun TwoHeartsDesignPreviewDark() {
    TwoHeartsTheme(darkTheme = true, dynamicColor = false) {
        TwoHeartsDesignPreview()
    }
}
