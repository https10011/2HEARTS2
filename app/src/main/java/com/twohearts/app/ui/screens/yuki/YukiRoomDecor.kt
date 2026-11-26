package com.twohearts.app.ui.screens.yuki

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiToy

private const val ROOM_WIDTH = 400f
private const val ROOM_HEIGHT = 300f

internal fun DrawScope.drawYukiRoom(dark: Boolean, activity: YukiActivity, toy: YukiToy) {
    withTransform({ scale(size.width / ROOM_WIDTH, size.height / ROOM_HEIGHT, Offset.Zero) }) {
        val evening = dark || activity == YukiActivity.SLEEPING
        val wallTop = if (dark) Color(0xFF2A202F) else Color(0xFFF9EDDF)
        val wallBase = if (dark) Color(0xFF493343) else Color(0xFFF1D1C5)
        val floor = if (dark) Color(0xFF382A37) else Color(0xFFE2C2AF)
        val seam = if (dark) Color(0xFF6C4F5B) else Color(0xFFC99C8B)
        val rug = if (dark) Color(0xFF674457) else Color(0xFFCB968E)
        val binding = if (dark) Color(0xFF8B6673) else Color(0xFFEBC2AE)
        drawRect(Brush.verticalGradient(listOf(wallTop, wallBase), endY = 209f), size = Size(ROOM_WIDTH, 209f))

        drawWindow(evening, dark)
        drawShelf(dark)
        drawRect(floor, Offset(0f, 209f), Size(ROOM_WIDTH, 91f))
        drawRect(seam.copy(alpha = .46f), Offset(0f, 204f), Size(ROOM_WIDTH, 5f))
        drawLine(seam, Offset(0f, 210f), Offset(400f, 210f), 1.5f)
        drawLine(seam.copy(alpha = .34f), Offset(0f, 230f), Offset(400f, 230f))
        drawLine(seam.copy(alpha = .34f), Offset(0f, 280f), Offset(400f, 280f))
        drawLine(seam.copy(alpha = .25f), Offset(117f, 230f), Offset(56f, 300f))
        drawLine(seam.copy(alpha = .25f), Offset(310f, 230f), Offset(373f, 300f))

        drawOval(seam.copy(alpha = .36f), Offset(43f, 230f), Size(319f, 54f))
        drawOval(rug, Offset(49f, 227f), Size(304f, 52f))
        drawOval(binding, Offset(57f, 233f), Size(288f, 40f), style = Stroke(1.5f))
        drawOval(binding.copy(alpha = .6f), Offset(66f, 239f), Size(267f, 28f), style = Stroke(1f))

        drawBowl(dark, activity)
        drawToy(toy, dark)
    }
}

private fun DrawScope.drawWindow(evening: Boolean, dark: Boolean) {
    val surround = if (dark) Color(0xFF604552) else Color(0xFFDEC1A8)
    val trim = if (dark) Color(0xFFB18C78) else Color(0xFFAB7465)
    val skyTop = if (evening) Color(0xFF342C53) else Color(0xFFF4C0A5)
    val skyBottom = if (evening) Color(0xFF6E5170) else Color(0xFFF7DBB4)
    val opening = Path().apply {
        moveTo(43f, 149f)
        lineTo(43f, 80f)
        cubicTo(43f, 50f, 58f, 33f, 85f, 33f)
        cubicTo(112f, 33f, 128f, 50f, 128f, 80f)
        lineTo(128f, 149f)
        close()
    }
    drawPath(opening, surround, style = Stroke(12f))
    clipPath(opening) {
        drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom), 30f, 149f), size = Size(150f, 149f))
        drawCircle(if (evening) Color(0xFFF3DAA9) else Color(0xFFFFEAC4), 10f, Offset(101f, 64f))
        if (evening) {
            val starlight = Color(0xFFEACAA7)
            drawCircle(starlight, 1.7f, Offset(64f, 66f))
            drawCircle(starlight, 1.3f, Offset(113f, 90f))
            drawCircle(starlight.copy(alpha = .7f), 1f, Offset(55f, 89f))
        }
        val horizon = Path().apply {
            moveTo(39f, 118f)
            quadraticTo(65f, 96f, 87f, 121f)
            quadraticTo(110f, 106f, 131f, 122f)
            lineTo(131f, 152f)
            lineTo(39f, 152f)
            close()
        }
        drawPath(horizon, if (evening) Color(0xFF4E4266) else Color(0xFFDCAB9C))
        drawLine(trim, Offset(85f, 35f), Offset(85f, 149f), 2.5f)
        drawLine(trim, Offset(43f, 98f), Offset(128f, 98f), 2.5f)
    }
    drawPath(opening, trim.copy(alpha = .75f), style = Stroke(2f))
    drawRoundRect(surround, Offset(35f, 147f), Size(100f, 7f), CornerRadius(3f))
}

private fun DrawScope.drawShelf(dark: Boolean) {
    val wood = if (dark) Color(0xFF76515F) else Color(0xFFC99482)
    val pot = if (dark) Color(0xFFBC8070) else Color(0xFFBE7C66)
    val leaves = if (dark) Color(0xFF9B9E7A) else Color(0xFF797C57)
    drawRoundRect(wood.copy(alpha = .4f), Offset(321f, 176f), Size(55f, 29f), CornerRadius(5f))
    drawRoundRect(wood, Offset(310f, 170f), Size(78f, 9f), CornerRadius(4f))
    drawRoundRect(pot, Offset(345f, 147f), Size(24f, 24f), CornerRadius(5f))
    drawLine(leaves, Offset(357f, 149f), Offset(354f, 116f), 2.5f, StrokeCap.Round)
    val left = Path().apply {
        moveTo(356f, 141f)
        quadraticTo(340f, 139f, 336f, 121f)
        quadraticTo(352f, 120f, 356f, 141f)
        close()
    }
    val right = Path().apply {
        moveTo(356f, 139f)
        quadraticTo(358f, 118f, 375f, 112f)
        quadraticTo(374f, 132f, 356f, 139f)
        close()
    }
    drawPath(left, leaves.copy(alpha = .77f))
    drawPath(right, leaves)
    drawRoundRect(Color(0xFF9E6A78), Offset(320f, 161f), Size(23f, 9f), CornerRadius(2f))
    drawRoundRect(Color(0xFFC4957B), Offset(323f, 155f), Size(19f, 6f), CornerRadius(2f))
}

private fun DrawScope.drawBowl(dark: Boolean, activity: YukiActivity) {
    val dish = if (dark) Color(0xFF98677C) else Color(0xFF9B5763)
    val glaze = if (dark) Color(0xFFD2A3AA) else Color(0xFFF2C4BC)
    drawOval(dish.copy(alpha = .30f), Offset(37f, 257f), Size(47f, 10f))
    drawRoundRect(dish, Offset(40f, 248f), Size(39f, 13f), CornerRadius(6f))
    drawOval(glaze, Offset(39f, 244f), Size(41f, 12f))
    val inside = when (activity) {
        YukiActivity.EATING -> Color(0xFFC99065)
        YukiActivity.DRINKING -> if (dark) Color(0xFFA2AFBA) else Color(0xFFB4BFBC)
        else -> dish.copy(alpha = .79f)
    }
    drawOval(inside, Offset(43f, 247f), Size(33f, 6f))
}

private fun DrawScope.drawToy(toy: YukiToy, dark: Boolean) {
    val gold = if (dark) Color(0xFFC59A6E) else Color(0xFFAB7751)
    if (toy == YukiToy.BALL) {
        drawOval(gold.copy(alpha = .4f), Offset(324f, 260f), Size(24f, 5f))
        drawCircle(gold, 8f, Offset(337f, 254f))
        drawArc(Color(0xFFECC3A3), 145f, 149f, false, Offset(330f, 248f), Size(13f, 13f), style = Stroke(1.2f))
    } else {
        drawLine(gold, Offset(351f, 256f), Offset(332f, 247f), 2.5f, StrokeCap.Round)
        drawLine(gold.copy(alpha = .73f), Offset(332f, 247f), Offset(324f, 254f), 1f, StrokeCap.Round)
        drawOval(Color(0xFF99647E), Offset(313f, 251f), Size(15f, 7f))
    }
}
