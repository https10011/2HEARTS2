package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.data.game.YukiToy
import com.twohearts.app.data.game.activityElapsedMillis
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import java.time.Instant
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun YukiRoom(state: YukiState, animate: Boolean, onPet: (() -> Unit)? = null, refusal: Int = 0) {
    val colors = LocalTwoHeartsColors.current
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
        YukiSprite(state, animate, refusal, Modifier.matchParentSize())
    }
}

@Composable
private fun YukiSprite(state: YukiState, animate: Boolean, refusal: Int, modifier: Modifier) {
    val context = LocalContext.current.applicationContext
    val canAnimate = animate && !LocalTwoHeartsMotion.current.reduced
    val currentState by rememberUpdatedState(state)
    val currentRefusal by rememberUpdatedState(refusal)
    val resting = stillYukiFrame(state)
    val frame = remember { mutableStateOf(resting) }
    var clips by remember(context) { mutableStateOf<Map<YukiMotion, List<YukiPose>>>(emptyMap()) }
    val images = remember {
        mutableStateOf<Map<YukiPose, ImageBitmap?>>(mapOf(resting.pose to YukiArt.cached(resting.pose)?.asImageBitmap()))
    }
    val layer = remember { Paint() }

    LaunchedEffect(context, canAnimate) {
        if (canAnimate && clips.isEmpty()) clips = withContext(Dispatchers.IO) { YukiArt.loadClips(context) }
    }

    val playing = canAnimate && clips.isNotEmpty()
    LaunchedEffect(context, playing, resting) {
        if (!playing) {
            val sprite = YukiArt.cached(resting.pose)?.asImageBitmap() ?: withContext(Dispatchers.IO) {
                (YukiArt.load(context, resting.pose) ?: YukiArt.load(context, YukiPose.SIT))?.asImageBitmap()
            }
            images.value = mapOf(resting.pose to sprite)
            frame.value = resting
        }
    }

    LaunchedEffect(context, playing, clips) {
        if (!playing) return@LaunchedEffect
        val game = YukiMotionController(clips, currentState.createdAt.hashCode().toLong())
        var tick: Long? = null
        var previousState: YukiState? = null
        var previousRefusal = -1
        var bank = images.value.filterValues { it != null }
        var focus: YukiMotion? = null
        while (true) {
            val now = withFrameNanos { it / 1_000_000 }
            if (tick?.let { now - it < 33 } == true) continue
            tick = now
            val latest = currentState
            val lastRefusal = currentRefusal
            if (latest != previousState || lastRefusal != previousRefusal) {
                game.update(latest, lastRefusal, now, latest.activityElapsedMillis(Instant.now()))
                previousState = latest
                previousRefusal = lastRefusal
            }
            val visible = game.sample(now)
            if (focus != visible.focused || visible.pose !in bank || visible.nextPose !in bank) {
                val requested = setOf(YukiPose.SIT, visible.pose, visible.nextPose) +
                    (visible.focused?.let { clips[it] }).orEmpty()
                val cached = bank
                bank = withContext(Dispatchers.IO) {
                    requested.associateWith { pose ->
                        if (pose in cached) cached[pose] else YukiArt.load(context, pose)?.asImageBitmap()
                    }
                }
                focus = visible.focused
                images.value = bank
            }
            frame.value = visible
        }
    }

    if (images.value.values.any { it != null }) {
        Canvas(modifier.semantics { contentDescription = "Yuki, an orange tabby" }) {
            val art = images.value
            val current = frame.value
            val front = art[current.pose] ?: art[YukiPose.SIT] ?: return@Canvas
            val next = art[current.nextPose]
            val samePose = next === front && current.facesRight == current.nextFacesRight
            if (next == null || samePose || current.blend <= 0f) {
                drawSprite(front, current)
            } else if (current.blend >= 1f) {
                drawSprite(next, current, facesRight = current.nextFacesRight)
            } else {
                // Interpolate premultiplied pixels so solid shared areas stay
                // opaque while the outline changes.
                drawContext.canvas.saveLayer(Rect(Offset.Zero, size), layer)
                drawSprite(front, current, 1f - current.blend)
                drawSprite(next, current, current.blend, BlendMode.Plus, current.nextFacesRight)
                drawContext.canvas.restore()
            }
        }
    } else {
        Icon(
            ThIcons.Cat, contentDescription = "Yuki", tint = LocalTwoHeartsColors.current.roseDeep,
            modifier = Modifier.padding(bottom = 64.dp).size(56.dp),
        )
    }
}

private fun DrawScope.drawSprite(
    tile: ImageBitmap, frame: YukiSpriteFrame, alpha: Float = 1f, blendMode: BlendMode = BlendMode.SrcOver,
    facesRight: Boolean = frame.facesRight,
) {
    val fit = minOf(size.width * .84f / YukiArt.WIDTH, size.height * .79f / YukiArt.HEIGHT)
    val center = size.width * (.5f + frame.x)
    val floor = size.height * (.852f - frame.lift)
    val width = (YukiArt.WIDTH * fit).roundToInt().coerceAtLeast(1)
    val height = (YukiArt.HEIGHT * fit).roundToInt().coerceAtLeast(1)
    withTransform({
        translate(center, floor)
        scale(if (facesRight) -1f else 1f, frame.stretch, Offset.Zero)
        translate(-YukiArt.GROUND_X * fit, -YukiArt.GROUND_Y * fit)
    }) {
        drawImage(tile, dstSize = IntSize(width, height), alpha = alpha, blendMode = blendMode)
    }
}
