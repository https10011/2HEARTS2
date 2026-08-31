package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.data.game.*
import com.twohearts.app.ui.components.Header
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * YukiScreen — The main Yuki companion experience.
 *
 * When users tap "Yuki" in Home, they discover an orange cat living
 * inside TwoHearts. "Wait... we have a cat?"
 *
 * This screen manages:
 * - Initial state loading with time decay
 * - Action processing with visual feedback
 * - Level-up celebrations
 * - Streak display
 * - Accessory management
 * - Speech bubbles for Yuki's reactions
 * - Activity animation lifecycle
 *
 * Mirrors legacy YukiScreen.tsx + yuki.css exactly.
 */
@Composable
fun YukiScreen(
    viewModel: YukiViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val speech by viewModel.speech.collectAsState()
    val activeAction by viewModel.activeAction.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val showLevelUp by viewModel.showLevelUp.collectAsState()
    val levelUpLevel by viewModel.levelUpLevel.collectAsState()
    val accessoryUnlocked by viewModel.accessoryUnlocked.collectAsState()
    val initialized by viewModel.initialized.collectAsState()

    // Initialize on first composition
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    if (!initialized || state == null) {
        Scaffold(
            topBar = { Header(title = "Yuki", onBack = onBack) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading Yuki...",
                    color = TwoHeartsTokens.Color.textSecondary
                )
            }
        }
        return
    }

    val yukiState = state!!

    Scaffold(
        topBar = { Header(title = "Yuki", onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TwoHeartsTokens.Spacing.space5)
        ) {
            // Accessory unlock notification
            if (accessoryUnlocked != null) {
                val accessory = getAccessoryById(accessoryUnlocked)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = TwoHeartsTokens.Color.burgundy.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = TwoHeartsTokens.Spacing.space3)
                ) {
                    Text(
                        text = "New accessory unlocked: ${accessory?.name ?: accessoryUnlocked}!",
                        color = TwoHeartsTokens.Color.burgundy,
                        fontSize = TwoHeartsTokens.Typography.sizeSm,
                        modifier = Modifier.padding(TwoHeartsTokens.Spacing.space3)
                    )
                }
            }

            // Yuki's environment
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(TwoHeartsTokens.Radius.xl),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF5C5A7).copy(alpha = 0.3f),
                                    Color(0xFFFDF6F0).copy(alpha = 0.6f),
                                    TwoHeartsTokens.Color.surface
                                )
                            )
                        )
                        .padding(vertical = TwoHeartsTokens.Spacing.space8)
                        .padding(horizontal = TwoHeartsTokens.Spacing.space5),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space5)
                ) {
                    // Speech bubble
                    if (speech != null) {
                        SpeechBubble(text = speech!!)
                    }

                    // Character
                    YukiCharacter(
                        activity = activeAction?.toActivity() ?: yukiState.activity,
                        mood = YukiMood.resolve(yukiState.moodScore),
                        accessory = yukiState.accessory,
                        showLevelUp = showLevelUp,
                        levelUpLevel = levelUpLevel,
                        modifier = Modifier.size(200.dp)
                    )

                    // Name and level
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space1)
                    ) {
                        Text(
                            text = yukiState.name,
                            fontSize = TwoHeartsTokens.Typography.size2xl,
                            fontWeight = FontWeight.Bold,
                            color = TwoHeartsTokens.Color.textPrimary
                        )
                        // Level pill
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space1),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFE8854A),
                                            Color(0xFFF09858)
                                        )
                                    ),
                                    shape = RoundedCornerShape(TwoHeartsTokens.Radius.pill)
                                )
                                .padding(horizontal = TwoHeartsTokens.Spacing.space3, vertical = TwoHeartsTokens.Spacing.space1)
                        ) {
                            Text(text = "\u2B50", fontSize = 10.sp, color = Color.White)
                            Text(
                                text = "Level ${yukiState.level}",
                                fontSize = TwoHeartsTokens.Typography.sizeXs,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // XP progress bar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space1)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(4.dp)
                                .background(TwoHeartsTokens.Color.divider, RoundedCornerShape(TwoHeartsTokens.Radius.pill))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (yukiState.experience.toFloat() / yukiState.xpToNextLevel).coerceIn(0f, 1f))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFE8854A), Color(0xFFF5C563))
                                        ),
                                        shape = RoundedCornerShape(TwoHeartsTokens.Radius.pill)
                                    )
                            )
                        }
                        Text(
                            text = "${yukiState.experience} / ${yukiState.xpToNextLevel} XP",
                            fontSize = 10.sp,
                            color = TwoHeartsTokens.Color.textSecondary
                        )
                    }

                    // Needs bars
                    Column(
                        verticalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        YukiNeedBar(label = "Hunger", value = yukiState.hunger, type = NeedType.HUNGER)
                        YukiNeedBar(label = "Energy", value = yukiState.energy, type = NeedType.ENERGY)
                        YukiNeedBar(label = "Happy", value = yukiState.happiness, type = NeedType.HAPPINESS)
                        YukiNeedBar(label = "Clean", value = yukiState.cleanliness, type = NeedType.CLEAN)
                    }

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = if (yukiState.streak > 0) "${TwoHeartsTokens.Color.textSecondary}${yukiState.streak}" else "${yukiState.streak}",
                            label = "Streak",
                            showFlame = yukiState.streak > 0
                        )
                        StatItem(
                            value = "${yukiState.totalInteractions}",
                            label = "Interactions"
                        )
                        StatItem(
                            value = "${yukiState.level}",
                            label = "Level"
                        )
                    }

                    // Action bar
                    YukiActions(
                        onAction = { action -> viewModel.processAction(action) },
                        disabled = isAnimating,
                        activeAction = activeAction
                    )
                }
            }

            // Spacer before accessories
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space3))

            // Accessories row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                YUKI_ACCESSORIES
                    .filter { yukiState.ownedAccessories.contains(it.id) }
                    .forEach { acc ->
                        val isSelected = (yukiState.accessory ?: "none") == acc.id
                        AccessoryChip(
                            accessory = acc,
                            isSelected = isSelected,
                            onClick = { viewModel.equipAccessory(acc.id) }
                        )
                    }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(TwoHeartsTokens.Spacing.space6))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

enum class NeedType { HUNGER, ENERGY, HAPPINESS, CLEAN }

@Composable
private fun YukiNeedBar(label: String, value: Int, type: NeedType) {
    val isLow = value < 25

    val fillColor = when {
        isLow -> TwoHeartsTokens.Color.error
        type == NeedType.HUNGER -> Color(0xFFE8854A)
        type == NeedType.ENERGY -> Color(0xFF7BAF6E)
        type == NeedType.HAPPINESS -> Color(0xFFD4745A)
        type == NeedType.CLEAN -> Color(0xFF7EAFCF)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = TwoHeartsTokens.Typography.sizeXs,
            color = TwoHeartsTokens.Color.textSecondary,
            modifier = Modifier.width(40.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(TwoHeartsTokens.Color.divider, RoundedCornerShape(TwoHeartsTokens.Radius.pill))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (value / 100f).coerceIn(0f, 1f))
                    .background(fillColor, RoundedCornerShape(TwoHeartsTokens.Radius.pill))
            )
        }
        Text(
            text = "$value",
            fontSize = 10.sp,
            color = TwoHeartsTokens.Color.textSecondary,
            modifier = Modifier.width(30.dp).padding(start = TwoHeartsTokens.Spacing.space1)
        )
    }
}

@Composable
private fun StatItem(value: String, label: String, showFlame: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showFlame) {
                Text(text = "\uD83D\uDD25", fontSize = 14.sp)
            }
            Text(
                text = value.removePrefix("\uD83D\uDD25"),
                fontSize = TwoHeartsTokens.Typography.sizeLg,
                fontWeight = FontWeight.Bold,
                color = TwoHeartsTokens.Color.textPrimary
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = TwoHeartsTokens.Color.textSecondary,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun SpeechBubble(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = TwoHeartsTokens.Color.surface
        ),
        shape = RoundedCornerShape(TwoHeartsTokens.Radius.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = TwoHeartsTokens.Typography.sizeSm,
            color = TwoHeartsTokens.Color.textPrimary,
            modifier = Modifier.padding(
                horizontal = TwoHeartsTokens.Spacing.space3,
                vertical = TwoHeartsTokens.Spacing.space2
            )
        )
    }
}

@Composable
private fun AccessoryChip(
    accessory: YukiAccessory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) TwoHeartsTokens.Color.blush else TwoHeartsTokens.Color.surface
    val borderColor = if (isSelected) TwoHeartsTokens.Color.burgundyLight else TwoHeartsTokens.Color.border
    val textColor = if (isSelected) TwoHeartsTokens.Color.textPrimary else TwoHeartsTokens.Color.textSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(TwoHeartsTokens.Radius.pill))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(TwoHeartsTokens.Radius.pill))
            .clickable { onClick() }
            .padding(horizontal = TwoHeartsTokens.Spacing.space2, vertical = TwoHeartsTokens.Spacing.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TwoHeartsTokens.Spacing.space1)
    ) {
        if (accessory.visual.isNotEmpty()) {
            Text(text = accessory.visual, fontSize = 14.sp)
        }
        Text(
            text = accessory.name,
            fontSize = TwoHeartsTokens.Typography.sizeXs,
            color = textColor
        )
    }
}
