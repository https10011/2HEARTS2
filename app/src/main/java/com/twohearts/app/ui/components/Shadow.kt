package com.twohearts.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Warm near-black — matches the app's charcoal rather than pure #000. */
private val WARM_SHADOW = Color(0x2E2B2420)

/**
 * A warm, low-contrast shadow for the few surfaces that genuinely float.
 *
 * Material's stock elevation drops a grey shadow tinted from the theme's
 * `shadow` colour, which reads cold against the cream and charcoal
 * backgrounds. This draws from warm near-black at low alpha with a soft
 * blur, so lifted surfaces still read as part of the paper.
 *
 * Most surfaces in TwoHearts should NOT use this — see the elevation scale
 * in [com.twohearts.app.ui.theme.TwoHeartsTokens.Elevation]. Depth usually
 * comes from a hairline border or a surface tone instead.
 */
fun Modifier.thSoftShadow(
    shape: RoundedCornerShape,
    elevation: Dp,
    color: Color = WARM_SHADOW,
): Modifier = this.drawBehind {
    if (elevation <= 0.dp) return@drawBehind

    val radiusPx = elevation.toPx()
    val radius = shape.topStart.toPx(size, this)

    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            this.color = Color.Transparent.toArgb()
            setShadowLayer(
                radiusPx * 2f,
                /* dx = */ 0f,
                /* dy = */ radiusPx * 0.6f,
                color.toArgb(),
            )
        }
        canvas.save()
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = radius,
            radiusY = radius,
            paint = paint,
        )
        canvas.restore()
    }
}