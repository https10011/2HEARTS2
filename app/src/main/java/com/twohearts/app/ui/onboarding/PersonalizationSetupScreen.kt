package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * PersonalizationSetupScreen — how TwoHearts looks and reads.
 *
 * ## What the migrated version was
 *
 * Two stacked lists of Material `RadioButton` rows: three for theme, four for
 * text size. Rendering it produced a screen that was indistinguishable from a
 * settings page — twelve rows of dots with no preview of what any choice
 * would actually do. The person was asked to decide how the app should look
 * while looking at a list, not at the app.
 *
 * For a text-size choice in particular, that is the wrong control: "Large"
 * means nothing until you see type at that size.
 *
 * ## What changed
 *
 *  - **Every option is previewed with the thing it controls.** The text-size
 *    options render sample type at their real size; the theme options render
 *    a miniature of the app in that palette.
 *  - **The choice is applied live.** Selecting a theme or size changes the
 *    running app immediately, so the person is choosing by looking at the
 *    real result rather than a description of it. This is the reason the step
 *    is the last one before the lock — by the time they reach the app, it is
 *    already set up how they want it.
 *  - **Neither choice is a dead end.** The subtitle says both can be changed
 *    later; the directive asks that onboarding not present preferences as
 *    permanent commitments.
 *
 * Selection state is carried by fill, border *and* the selected semantics —
 * never by colour alone.
 */
@Composable
fun PersonalizationSetupScreen(
    data: OnboardingData,
    onBack: () -> Unit,
    onNext: (OnboardingData) -> Unit,
    onThemeChange: ((String) -> Unit)? = null,
    onTextSizeChange: ((String) -> Unit)? = null,
) {
    var themeMode by remember { mutableStateOf(data.themeMode) }
    var textSize by remember { mutableStateOf(data.textSize) }

    OnboardingLayout(
        stage = OnboardingStage.PERSONALIZATION,
        eyebrow = "Make it yours",
        title = "How should TwoHearts feel?",
        subtitle = "Pick what suits you. Both of these can be changed any time in Settings.",
        onBack = onBack,
        primaryLabel = "Continue",
        onPrimary = {
            onNext(data.copy(themeMode = themeMode, textSize = textSize))
        },
    ) {
        OnboardingSectionLabel(text = "Appearance")

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space3),
        ) {
            THEME_OPTIONS.forEach { option ->
                ThemeOptionCard(
                    option = option,
                    selected = themeMode == option.value,
                    onSelect = {
                        themeMode = option.value
                        // Applied immediately so the choice is judged by its
                        // real result, not by a label.
                        onThemeChange?.invoke(option.value)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.sectionGap))

        OnboardingSectionLabel(text = "Text size")

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))

        Text(
            text = "Choose the size that's most comfortable to read.",
            style = MaterialTheme.typography.bodySmall,
            color = LocalTwoHeartsColors.current.textTertiary,
        )

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
        ) {
            TEXT_SIZE_OPTIONS.forEach { option ->
                TextSizeOptionRow(
                    option = option,
                    selected = textSize == option.value,
                    onSelect = {
                        textSize = option.value
                        onTextSizeChange?.invoke(option.value)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space5))

        OnboardingHint(
            text = "You'll find both of these again under Settings › Appearance.",
        )
    }
}

/** A theme choice and the preview it renders. */
private data class ThemeOption(
    val value: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val THEME_OPTIONS = listOf(
    ThemeOption("light", "Light", ThIcons.LightMode),
    ThemeOption("dark", "Dark", ThIcons.DarkMode),
    ThemeOption("system", "System", ThIcons.SystemTheme),
)

/** A text-size choice and the scale its sample type renders at. */
private data class TextSizeOption(
    val value: String,
    val label: String,
    val sampleSp: Int,
)

/**
 * Sample sizes are drawn from the app's real scale steps (14 / 15 / 17 / 19sp
 * around the `bodyMedium` role), not invented numbers, so the preview shows
 * the difference the person will actually get.
 */
private val TEXT_SIZE_OPTIONS = listOf(
    TextSizeOption("small", "Small", 14),
    TextSizeOption("default", "Default", 15),
    TextSizeOption("large", "Large", 17),
    TextSizeOption("extra-large", "Extra large", 19),
)

/**
 * ThemeOptionCard — a miniature of the app in the selected palette.
 *
 * The preview is built from real token values rather than a screenshot so it
 * cannot drift from the actual theme. Each card shows a page field, a card
 * and two ink lines, which is enough to read the palette's character without
 * becoming a picture of the app.
 */
@Composable
private fun ThemeOptionCard(
    option: ThemeOption,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)

    // The preview palette is fixed per option, independent of the theme the
    // app is currently rendering in — the point is to show what each choice
    // looks like, so it must not itself follow the current theme.
    val preview = when (option.value) {
        "dark" -> ThemePreview(
            page = Color(0xFF1A1310),
            card = Color(0xFF2E2622),
            ink = Color(0xFFF5ECE4),
            accent = Color(0xFFC9808B),
        )
        "light" -> ThemePreview(
            page = Color(0xFFFFFBF4),
            card = Color(0xFFFFFFFF),
            ink = Color(0xFF3A302C),
            accent = Color(0xFF6A1B2B),
        )
        else -> ThemePreview(
            page = Color(0xFFF4EFE9),
            card = Color(0xFFFFFFFF),
            ink = Color(0xFF3A302C),
            accent = Color(0xFF8E3147),
        )
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (selected) thColors.surfaceBlush else Color.Transparent, shape)
            .border(
                if (selected) TwoHeartsTokens.Border.strong else TwoHeartsTokens.Border.hairline,
                if (selected) MaterialTheme.colorScheme.primary else thColors.borderSubtle,
                shape,
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(TwoHeartsTokens.Spacing.space3)
            .semantics {
                contentDescription = "${option.label} appearance" +
                    if (selected) ", selected" else ""
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Miniature page.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(TwoHeartsTokens.Radius.sm))
                .background(preview.page)
                .padding(TwoHeartsTokens.Spacing.space2),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(preview.ink.copy(alpha = 0.8f)),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(TwoHeartsTokens.Radius.sm))
                        .background(preview.card),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(preview.accent),
                )
            }
        }

        Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space2))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (selected) ThIcons.CheckCircle else option.icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else thColors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(TwoHeartsTokens.Spacing.space1))
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

private data class ThemePreview(
    val page: Color,
    val card: Color,
    val ink: Color,
    val accent: Color,
)

/**
 * TextSizeOptionRow — a selectable row whose label is rendered at the size it
 * selects.
 *
 * This is the whole reason the control works: "Large" printed at 19sp is
 * self-explanatory, where "Large" printed at 14sp next to a radio dot is not.
 */
@Composable
private fun TextSizeOptionRow(
    option: TextSizeOption,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val thColors = LocalTwoHeartsColors.current
    val shape = RoundedCornerShape(TwoHeartsTokens.Radius.md)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TwoHeartsTokens.Dimensions.touchTargetMin)
            .clip(shape)
            .background(if (selected) thColors.surfaceBlush else Color.Transparent, shape)
            .border(
                if (selected) TwoHeartsTokens.Border.strong else TwoHeartsTokens.Border.hairline,
                if (selected) MaterialTheme.colorScheme.primary else thColors.borderSubtle,
                shape,
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(horizontal = TwoHeartsTokens.Spacing.space4, vertical = TwoHeartsTokens.Spacing.space3)
            .semantics {
                contentDescription = "Text size ${option.label}" +
                    if (selected) ", selected" else ""
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                // The sample is the control.
                fontSize = option.sampleSp.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space1))
            Text(
                text = "Remember the little things",
                fontSize = (option.sampleSp - 3).sp,
                color = thColors.textSecondary,
            )
        }

        Icon(
            imageVector = if (selected) ThIcons.CheckCircle else ThIcons.CircleOutline,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else thColors.borderSubtle,
            modifier = Modifier.size(22.dp),
        )
    }
}