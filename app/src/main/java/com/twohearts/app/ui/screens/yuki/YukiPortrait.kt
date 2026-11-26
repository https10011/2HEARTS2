package com.twohearts.app.ui.screens.yuki

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiMood
import com.twohearts.app.ui.components.ThIcons
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.TwoHeartsTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A single registered pose. The Home card has no animation clock. */
@Composable
internal fun YukiPortrait(activity: YukiActivity, mood: YukiMood, modifier: Modifier = Modifier, accessory: String? = null) {
    val pose = yukiRestingPose(activity, mood)
    val ctx = LocalContext.current.applicationContext
    val art by produceState<ImageBitmap?>(YukiArt.cached(pose)?.asImageBitmap(), ctx, pose) {
        value = withContext(Dispatchers.IO) {
            (YukiArt.load(ctx, pose) ?: YukiArt.load(ctx, YukiPose.SIT))?.asImageBitmap()
        }
    }
    val colors = LocalTwoHeartsColors.current
    Box(
        modifier.clip(RoundedCornerShape(TwoHeartsTokens.Radius.md)).background(colors.surfaceBlush),
        contentAlignment = Alignment.Center,
    ) {
        val tile = art
        if (tile == null) {
            Icon(ThIcons.Cat, contentDescription = null, tint = colors.roseDeep, modifier = Modifier.size(22.dp))
        } else {
            Canvas(Modifier.matchParentSize()) {
                val ratio = minOf(size.width / 310f, size.height / 280f)
                val baseline = size.height * .87f
                drawOval(
                    colors.roseMuted.copy(alpha = .31f),
                    Offset(size.width * .12f, baseline - size.height * .08f),
                    Size(size.width * .76f, size.height * .13f),
                )
                translate(
                    size.width * .50f - YukiArt.GROUND_X * ratio,
                    baseline - YukiArt.GROUND_Y * ratio,
                ) {
                    drawImage(tile, dstSize = IntSize((YukiArt.WIDTH * ratio).toInt(), (YukiArt.HEIGHT * ratio).toInt()))
                }
                drawYukiWardrobe(accessory, YukiSpriteFrame(pose), Offset(size.width * .5f, baseline), ratio)
            }
        }
    }
}
