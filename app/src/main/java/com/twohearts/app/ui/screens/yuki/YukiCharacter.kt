package com.twohearts.app.ui.screens.yuki

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.game.*
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

private data class PoseFrame(val pose: YukiPose, val durationMs: Long, val x: Float = 0f, val y: Float = 0f)

private fun restingPose(state: YukiState) = when (state.activity) {
    YukiActivity.IDLE -> when (yukiMood(state)) {
        YukiMood.SLEEPY -> YukiPose.LOAF
        YukiMood.SAD -> YukiPose.SAD
        else -> YukiPose.SIT
    }
    YukiActivity.EATING -> YukiPose.EAT
    YukiActivity.DRINKING -> YukiPose.DRINK
    YukiActivity.PLAYING -> YukiPose.PAW
    YukiActivity.SLEEPING -> YukiPose.SLEEP
    YukiActivity.GROOMING -> YukiPose.GROOM
    YukiActivity.BEING_PETTED, YukiActivity.PURRING -> YukiPose.ROLL
}

private fun posesFor(activity: YukiActivity): List<PoseFrame> = when (activity) {
    YukiActivity.EATING -> listOf(PoseFrame(YukiPose.STAND, 320), PoseFrame(YukiPose.EAT, 1780, -0.75f))
    YukiActivity.DRINKING -> listOf(PoseFrame(YukiPose.WALK, 350), PoseFrame(YukiPose.DRINK, 1450, -0.75f))
    YukiActivity.PLAYING -> listOf(
        PoseFrame(YukiPose.WALK, 320, 0.18f), PoseFrame(YukiPose.WALK_ALT, 340, 0.38f),
        PoseFrame(YukiPose.RUN, 330, 0.6f), PoseFrame(YukiPose.RUN_ALT, 300, 0.72f),
        PoseFrame(YukiPose.RUN_EXTEND, 300, 0.42f), PoseFrame(YukiPose.JUMP, 420, 0.10f, -24f),
        PoseFrame(YukiPose.PAW, 690),
    )
    YukiActivity.GROOMING -> listOf(
        PoseFrame(YukiPose.GROOM, 630), PoseFrame(YukiPose.GROOM_ALT, 580), PoseFrame(YukiPose.GROOM, 790),
    )
    YukiActivity.BEING_PETTED, YukiActivity.PURRING -> listOf(PoseFrame(YukiPose.SIT_ALT, 400), PoseFrame(YukiPose.ROLL, 1000))
    YukiActivity.IDLE, YukiActivity.SLEEPING -> emptyList()
}

@Composable
internal fun YukiRoom(state: YukiState, animate: Boolean, onPet: (() -> Unit)? = null) {
    val colors = LocalTwoHeartsColors.current
    val reduced = LocalTwoHeartsMotion.current.reduced || !animate
    val initialPose = restingPose(state)
    val atBowl = state.activity == YukiActivity.EATING || state.activity == YukiActivity.DRINKING
    val still = PoseFrame(initialPose, 0, x = if (atBowl) -0.75f else 0f)
    var frame by remember(state.activity, state.activityStartedAt, initialPose) { mutableStateOf(still) }
    LaunchedEffect(state.activity, state.activityStartedAt, initialPose, reduced) {
        if (reduced) {
            frame = still
        } else if (state.activity == YukiActivity.IDLE) {
            var previous = initialPose
            val idleOptions = if (yukiMood(state) == YukiMood.SLEEPY) listOf(YukiPose.LOAF, YukiPose.SIT_ALT) else {
                listOf(YukiPose.SIT_ALT, YukiPose.SIT_ALT, YukiPose.IDLE, YukiPose.LOAF, YukiPose.STRETCH, YukiPose.GROOM)
            }
            while (true) {
                delay(Random.nextLong(6_500, 12_500))
                val next = idleOptions.filter { it != previous }.random()
                frame = PoseFrame(next, 0)
                previous = next
            }
        } else {
            for (pose in posesFor(state.activity)) {
                frame = pose
                delay(pose.durationMs)
            }
        }
    }

    val generation = frame.pose
    val context = LocalContext.current.applicationContext
    val bitmap by produceState<Bitmap?>(YukiArt.cached(generation), generation) {
        value = withContext(Dispatchers.IO) {
            YukiArt.load(context, generation) ?: YukiArt.load(context, YukiPose.SIT)
        }
    }
    val travel by animateFloatAsState(frame.x, tween(if (reduced) 0 else 320), label = "yuki room movement")
    val jump by animateFloatAsState(frame.y, tween(if (reduced) 0 else 200), label = "yuki jump")

    Box(
        Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(colors.surfaceWarm, colors.surfaceBlush)))
            .then(if (onPet == null) Modifier else Modifier.clickable(
                role = Role.Button, onClickLabel = "Pet Yuki", onClick = onPet,
            )),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val floor = size.height * .73f
            drawRect(colors.surfaceElevated.copy(alpha = .72f), Offset(0f, floor), Size(size.width, size.height - floor))
            val window = Size(size.width * .28f, size.height * .39f)
            val origin = Offset(size.width * .63f, size.height * .11f)
            drawRoundRect(colors.goldBg, origin, window, CornerRadius(18.dp.toPx()))
            drawLine(colors.goldInk.copy(alpha = .24f), origin + Offset(window.width / 2, 0f), origin + Offset(window.width / 2, window.height), 2.dp.toPx())
            drawLine(colors.goldInk.copy(alpha = .24f), origin + Offset(0f, window.height / 2), origin + Offset(window.width, window.height / 2), 2.dp.toPx())
            drawOval(colors.roseMuted.copy(alpha = .28f), Offset(size.width * .14f, size.height * .78f), Size(size.width * .72f, size.height * .13f))
            drawOval(colors.roseDeep.copy(alpha = .63f), Offset(size.width * .08f, size.height * .81f), Size(size.width * .13f, size.height * .045f))
            if (state.activity == YukiActivity.EATING || state.activity == YukiActivity.DRINKING) {
                val rim = if (state.activity == YukiActivity.DRINKING) colors.sage else colors.goldInk
                drawOval(rim, Offset(size.width * .091f, size.height * .81f), Size(size.width * .108f, size.height * .019f))
            }
            if (state.selectedToy == YukiToy.BALL) {
                drawCircle(colors.goldInk, radius = 8.dp.toPx(), center = Offset(size.width * .84f, size.height * .82f))
            }
        }
        val displayed = bitmap
        if (displayed != null) {
            Image(
                displayed.asImageBitmap(),
                contentDescription = "Yuki, an orange tabby",
                modifier = Modifier.padding(bottom = 36.dp).widthIn(max = 212.dp)
                    .fillMaxWidth(.63f).height(166.dp)
                    .graphicsLayer { translationX = travel * 48.dp.toPx(); translationY = jump.dp.toPx() },
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
            )
        } else Icon(
            ThIcons.Cat, contentDescription = "Yuki",
            tint = colors.roseDeep, modifier = Modifier.padding(bottom = 64.dp).size(56.dp),
        )
    }
}
