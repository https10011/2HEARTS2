package com.twohearts.app.ui.screens.yuki

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class WardrobeAnchor(val neck: Offset, val crown: Offset, val angle: Float)

private fun spot(neckX: Float, neckY: Float, crownX: Float, crownY: Float, angle: Float = 0f) =
    WardrobeAnchor(Offset(neckX, neckY), Offset(crownX, crownY), angle)

private val seated = spot(.454f, .55f, .452f, .278f)
private val wardrobeAnchors = mapOf(
    YukiPose.SIT to seated,
    YukiPose.SLEEP to spot(.396f, .797f, .337f, .566f, -18f),
    YukiPose.IDLE to spot(.377f, .705f, .361f, .43f),
    YukiPose.STAND to spot(.398f, .552f, .400f, .255f),
    YukiPose.WALK to spot(.335f, .619f, .320f, .322f),
    YukiPose.WALK_ALT to spot(.375f, .55f, .349f, .263f),
    YukiPose.RUN to spot(.273f, .675f, .263f, .385f),
    YukiPose.RUN_ALT to spot(.301f, .665f, .282f, .400f),
    YukiPose.RUN_EXTEND to spot(.268f, .646f, .225f, .373f),
    YukiPose.JUMP to spot(.338f, .581f, .326f, .292f),
    YukiPose.STRETCH to spot(.406f, .765f, .347f, .493f),
    YukiPose.EAT to spot(.396f, .885f, .334f, .646f, -8f),
    YukiPose.DRINK to spot(.418f, .870f, .336f, .628f, -8f),
    YukiPose.PAW to spot(.425f, .587f, .434f, .292f, 14f),
    YukiPose.GROOM to spot(.421f, .611f, .392f, .346f, -16f),
    YukiPose.GROOM_ALT to spot(.453f, .608f, .435f, .310f, -14f),
    YukiPose.SIT_ALT to spot(.454f, .552f, .450f, .284f),
    YukiPose.SAD to spot(.456f, .570f, .455f, .310f),
    YukiPose.ANNOYED to spot(.461f, .545f, .455f, .282f),
    YukiPose.LOAF to spot(.474f, .739f, .449f, .443f),
    YukiPose.ROLL to spot(.364f, .793f, .210f, .712f, -67f),
)

internal fun registeredYukiHeadHeight(pose: YukiPose): Float =
    YukiArt.GROUND_Y - (wardrobeAnchors[pose] ?: seated).crown.y * YukiArt.HEIGHT

/** The mounted artwork follows the registered points through pose blending. */
internal fun DrawScope.drawYukiWardrobe(
    id: String?, frame: YukiSpriteFrame, ground: Offset, ratio: Float, width: Float = 1f,
) {
    if (id == null || id == "none") return
    val from = wardrobeAnchors[frame.pose] ?: seated
    val next = wardrobeAnchors[frame.nextPose] ?: seated
    fun relative(anchor: WardrobeAnchor, facesRight: Boolean): Offset {
        val target = if (id == "crown") anchor.crown else anchor.neck
        val mirrored = if (facesRight) -1f else 1f
        return Offset(
            (target.x * YukiArt.WIDTH - YukiArt.GROUND_X) * ratio * mirrored * width,
            (target.y * YukiArt.HEIGHT - YukiArt.GROUND_Y) * ratio * frame.stretch,
        )
    }

    val first = relative(from, frame.facesRight)
    val last = relative(next, frame.nextFacesRight)
    val location = ground + first + (last - first) * frame.blend
    val fromAngle = if (frame.facesRight) -from.angle else from.angle
    val toAngle = if (frame.nextFacesRight) -next.angle else next.angle
    val angle = fromAngle + (toAngle - fromAngle) * frame.blend
    withTransform({
        translate(location.x, location.y)
        rotate(angle, Offset.Zero)
        scale(ratio * width, ratio, Offset.Zero)
    }) {
        drawAccessory(id)
    }
}

private val bowOutline = Path().apply {
    moveTo(-15f, -7f)
    quadraticTo(-19f, 0f, -15f, 7f)
    lineTo(0f, 1f)
    lineTo(15f, 7f)
    quadraticTo(19f, 0f, 15f, -7f)
    lineTo(0f, -1f)
    close()
}

private val bandanaOutline = Path().apply {
    moveTo(-15f, -5f)
    quadraticTo(0f, 3f, 16f, -6f)
    quadraticTo(10f, 10f, 0f, 17f)
    quadraticTo(-10f, 10f, -15f, -5f)
    close()
}

private val crownOutline = Path().apply {
    moveTo(-14f, 1f)
    lineTo(-17f, -13f)
    lineTo(-6f, -7f)
    lineTo(0f, -18f)
    lineTo(6f, -7f)
    lineTo(17f, -13f)
    lineTo(14f, 1f)
    close()
}

private val heartTag = Path().apply {
    moveTo(0f, 12f)
    cubicTo(-10f, 5f, -5f, 0f, 0f, 4f)
    cubicTo(5f, 0f, 10f, 5f, 0f, 12f)
    close()
}

private val starPin = Path().apply {
    repeat(10) { point ->
        val angle = point * PI / 5 - PI / 2
        val radius = if (point % 2 == 0) 10f else 4.5f
        val x = (cos(angle) * radius).toFloat() + 6f
        val y = (sin(angle) * radius).toFloat() + 7f
        if (point == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun DrawScope.drawAccessory(id: String) {
    val wine = Color(0xFF9B4863)
    val rose = Color(0xFFDB92A1)
    val gold = Color(0xFFE3B96B)
    when (id) {
        "bow-tie" -> {
            drawPath(bowOutline, wine)
            drawCircle(rose, 4f)
        }
        "bandana" -> {
            drawPath(bandanaOutline, Color(0xFF637557))
            drawCircle(Color(0xFFE8D9AE), 2.2f, Offset(0f, 8f))
        }
        "crown" -> {
            drawPath(crownOutline, gold)
            drawRoundRect(Color(0xFFB4834B), Offset(-14f, -1f), Size(28f, 4f), CornerRadius(1.5f))
        }
        "heart-collar" -> {
            drawRoundRect(wine, Offset(-14f, -2f), Size(28f, 5f), CornerRadius(2.5f))
            drawPath(heartTag, gold)
        }
        "star-badge" -> {
            drawPath(starPin, gold)
        }
    }
}
