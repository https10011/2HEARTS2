package com.twohearts.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// TwoHearts Brand Colors
private val Burgundy = Color(0xFF6A1B2B)
private val BurgundyLight = Color(0xFF8C3A4F)
private val BurgundyDark = Color(0xFF4A0E1C)
private val Cream = Color(0xFFFDF6F0)
private val Blush = Color(0xFFF6E1DE)
private val RoseMuted = Color(0xFFC9808B)
private val Charcoal = Color(0xFF2B2420)

private val LightColorScheme = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = Blush,
    onPrimaryContainer = BurgundyDark,
    secondary = RoseMuted,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    surfaceVariant = Blush,
    onSurfaceVariant = Charcoal,
)

private val DarkColorScheme = darkColorScheme(
    primary = BurgundyLight,
    onPrimary = Color.White,
    primaryContainer = BurgundyDark,
    onPrimaryContainer = Blush,
    secondary = RoseMuted,
    onSecondary = Color.White,
    background = Charcoal,
    onBackground = Cream,
    surface = Color(0xFF1C1917),
    onSurface = Cream,
    surfaceVariant = Color(0xFF2B2420),
    onSurfaceVariant = Cream,
)

@Composable
fun TwoHeartsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TwoHeartsTypography,
        content = content
    )
}
