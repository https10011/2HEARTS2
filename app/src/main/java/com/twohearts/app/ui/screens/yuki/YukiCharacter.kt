package com.twohearts.app.ui.screens.yuki

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.data.game.*
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * YukiCharacter — Renders the Yuki cat character with:
 * - Activity-based animation (idle breathing, eating, playing, sleeping, etc.)
 * - Mood-based expression via speech bubble
 * - Accessory overlay
 * - Heart particles for petting
 * - ZZZ particles for sleeping
 * - Level-up celebration overlay
 *
 * Mirrors legacy YukiCharacter.tsx + yuki.css animations.
 * Uses Compose animations for all activity transitions.
 */
@Composable
fun YukiCharacter(
    activity: YukiActivity,
    mood: YukiMood,
    accessory: String?,
    showLevelUp: Boolean,
    levelUpLevel: Int,
    modifier: Modifier = Modifier
) {
    val accessoryData = getAccessoryById(accessory)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Activity-based animation wrapper
        val infiniteTransition = rememberInfiniteTransition(label = "yuki")

        // Breathing/idle animation
        val idleY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idle_breathe"
        )

        // Activity-specific animations
        val activityScale by animateFloatAsState(
            targetValue = when (activity) {
                YukiActivity.PLAYING -> 1.05f
                YukiActivity.EATING -> 1.02f
                else -> 1f
            },
            animationSpec = tween(300),
            label = "activity_scale"
        )

        val activityRotation by animateFloatAsState(
            targetValue = when (activity) {
                YukiActivity.BEING_PETTED -> 3f
                YukiActivity.GROOMING -> -5f
                else -> 0f
            },
            animationSpec = tween(800),
            label = "activity_rotation"
        )

        val activityOffsetY by animateFloatAsState(
            targetValue = when (activity) {
                YukiActivity.PLAYING -> -18f
                YukiActivity.EATING -> -3f
                YukiActivity.SLEEPING -> 4f
                else -> 0f
            },
            animationSpec = tween(700),
            label = "activity_offset"
        )

        // Sleeping opacity
        val sleepOpacity by animateFloatAsState(
            targetValue = if (activity == YukiActivity.SLEEPING) 0.85f else 1f,
            animationSpec = tween(4000),
            label = "sleep_opacity"
        )

        // Determine the cat's vertical offset
        val catOffsetY = when (activity) {
            YukiActivity.IDLE -> idleY
            else -> activityOffsetY
        }

        // Cat character with animations
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer {
                    // Idle breathing or activity transform
                    translationY = catOffsetY * density
                    scaleX = activityScale
                    scaleY = activityScale
                    rotationZ = activityRotation
                    alpha = sleepOpacity
                },
            contentAlignment = Alignment.Center
        ) {
            // Cat emoji placeholder (the cat SVG is not available in Compose)
            // In production, this would be an AsyncImage or painterResource
            Text(
                text = "\uD83D\uDC31", // Orange cat emoji
                fontSize = 80.sp,
                textAlign = TextAlign.Center
            )

            // Accessory overlay
            if (accessoryData != null && accessoryData.visual.isNotEmpty()) {
                Text(
                    text = accessoryData.visual,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Heart particles for petting
            if (activity == YukiActivity.BEING_PETTED) {
                HeartParticles()
            }

            // ZZZ for sleeping
            if (activity == YukiActivity.SLEEPING) {
                ZzzParticles()
            }
        }

        // Mood speech bubble (positioned top-right of character)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-8).dp)
        ) {
            MoodBubble(mood = mood)
        }

        // Level-up overlay
        if (showLevelUp) {
            LevelUpOverlay(level = levelUpLevel)
        }
    }
}

/**
 * MoodBubble — Shows Yuki's current mood description.
 */
@Composable
private fun MoodBubble(mood: YukiMood) {
    Text(
        text = mood.description,
        fontSize = TwoHeartsTokens.Typography.sizeXs,
        color = TwoHeartsTokens.Color.textSecondary,
        modifier = Modifier
            .background(
                color = TwoHeartsTokens.Color.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(TwoHeartsTokens.Radius.pill)
            )
            .padding(horizontal = TwoHeartsTokens.Spacing.space3, vertical = TwoHeartsTokens.Spacing.space1)
    )
}

/**
 * HeartParticles — Animated heart particles for petting.
 */
@Composable
private fun HeartParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "hearts")

    val heart1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart1"
    )
    val heart1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart1_alpha"
    )

    val heart2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart2"
    )
    val heart2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart2_alpha"
    )

    val heart3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart3"
    )
    val heart3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart3_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "❤",
            fontSize = 14.sp,
            color = TwoHeartsTokens.Color.burgundyLight,
            modifier = Modifier
                .offset(x = (-15).dp, y = heart1Y.dp)
                .graphicsLayer { alpha = heart1Alpha }
        )
        Text(
            text = "❤",
            fontSize = 10.sp,
            color = TwoHeartsTokens.Color.burgundyLight,
            modifier = Modifier
                .offset(x = 5.dp, y = heart2Y.dp)
                .graphicsLayer { alpha = heart2Alpha }
        )
        Text(
            text = "❤",
            fontSize = 8.sp,
            color = TwoHeartsTokens.Color.burgundyLight,
            modifier = Modifier
                .offset(x = 20.dp, y = heart3Y.dp)
                .graphicsLayer { alpha = heart3Alpha }
        )
    }
}

/**
 * ZzzParticles — Animated ZZZ for sleeping.
 */
@Composable
private fun ZzzParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "zzz")

    val z1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "z1"
    )
    val z1Alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "z1_alpha"
    )

    val z2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "z2"
    )

    val z3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1200, easing = EaseOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "z3"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "z",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TwoHeartsTokens.Color.textSecondary,
            modifier = Modifier
                .offset(x = 15.dp, y = z1Y.dp)
                .graphicsLayer { alpha = z1Alpha }
        )
        Text(
            text = "z",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TwoHeartsTokens.Color.textSecondary,
            modifier = Modifier
                .offset(x = 23.dp, y = z2Y.dp)
                .graphicsLayer { alpha = 0.7f }
        )
        Text(
            text = "z",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TwoHeartsTokens.Color.textSecondary,
            modifier = Modifier
                .offset(x = 31.dp, y = z3Y.dp)
                .graphicsLayer { alpha = 0.4f }
        )
    }
}

/**
 * LevelUpOverlay — Celebration overlay when Yuki levels up.
 */
@Composable
private fun LevelUpOverlay(level: Int) {
    val scale by remember { Animatable(0.5f) }

    LaunchedEffect(level) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = EaseOutBack)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = TwoHeartsTokens.Color.burgundy.copy(alpha = 0.15f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(TwoHeartsTokens.Radius.xl)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Level $level!",
            fontSize = TwoHeartsTokens.Typography.size2xl,
            fontWeight = FontWeight.Bold,
            color = TwoHeartsTokens.Color.burgundy,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Easing helpers (Compose animation spec)
// ---------------------------------------------------------------------------

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
