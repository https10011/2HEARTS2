package com.twohearts.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoTone
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.theme.LocalTwoHeartsColors

/** Front cover and inside inscription. All content can scroll on a short display. */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTwoHeartsColors.current
    BoxWithConstraints(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter,
    ) {
        val enlarged = LocalDensity.current.fontScale >= 1.5f
        val coverHeight = if (enlarged) (maxHeight * .25f).coerceIn(108.dp, 164.dp)
            else (maxHeight * .46f).coerceIn(172.dp, 340.dp)
        Column(
            Modifier.widthIn(max = 620.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier.fillMaxWidth().height(coverHeight).albumCover(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BrandLogo(variant = BrandLogoVariant.MARK, tone = BrandLogoTone.LIGHT, size = if (enlarged) 40 else 66)
                    Spacer(Modifier.height(if (enlarged) 4.dp else 12.dp))
                    Text("TwoHearts", style = if (enlarged) MaterialTheme.typography.titleLarge else MaterialTheme.typography.displayMedium, color = Color(0xFFFFF5DF))
                }
            }
            Column(Modifier.padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 20.dp)) {
                Text("Just the two of you", Modifier.semantics { heading() },
                    style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(10.dp))
                Text("A private place for your photographs, letters and days together.",
                    style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
            Column(Modifier.padding(start = 28.dp, end = 28.dp, bottom = 20.dp)) {
                ThButton(onClick = onGetStarted, text = "Begin", full = true)
                Spacer(Modifier.height(10.dp))
                Text("No accounts. Nothing leaves this device.", style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary)
            }
        }
    }
}

/** Cloth spine, foil ruling and two sewn corners. Geometry is cached at the measured page size. */
private fun Modifier.albumCover(): Modifier = drawWithCache {
    val spine = 15.dp.toPx()
    val edge = 21.dp.toPx()
    val ink = Color(0xFF52162D)
    val foil = Color(0xFFC09976)
    val thin = 1.dp.toPx()
    val far = size.width - edge
    val corner = minOf(size.width, size.height) * .22f
    val stamp = Path().apply {
        moveTo(far - corner, edge)
        lineTo(far, edge)
        lineTo(far, edge + corner)
        close()
        moveTo(edge + spine, size.height - edge - corner)
        lineTo(edge + spine, size.height - edge)
        lineTo(edge + spine + corner, size.height - edge)
        close()
    }
    onDrawBehind {
        drawRect(ink)
        drawLine(Color(0xFF391023), Offset(spine * .52f, 0f), Offset(spine * .52f, size.height), spine)
        drawLine(foil.copy(alpha = .6f), Offset(spine + thin, 0f), Offset(spine + thin, size.height), thin)
        drawPath(stamp, foil.copy(alpha = .48f), style = Stroke(thin))
        drawLine(foil, Offset(edge + spine, edge), Offset(far, edge), thin)
        drawLine(foil, Offset(edge + spine, size.height - edge), Offset(far, size.height - edge), thin)
    }
}
