package com.twohearts.app

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.math.abs
import kotlin.math.hypot

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransformationBrandingTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val output = File("build/phase0-screens").apply { mkdirs() }

    @Test
    fun compiledLauncherForegroundIsCenteredInsideTheAdaptiveSafeZone() {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)
        assertEquals(576, bitmap.width)
        assertEquals(576, bitmap.height)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val visible = Rect(bitmap.width, bitmap.height, 0, 0)
        var radius = 0.0
        for (index in pixels.indices) {
            if (Color.alpha(pixels[index]) == 0) continue
            val x = index % bitmap.width
            val y = index / bitmap.width
            visible.left = minOf(visible.left, x)
            visible.top = minOf(visible.top, y)
            visible.right = maxOf(visible.right, x + 1)
            visible.bottom = maxOf(visible.bottom, y + 1)
            radius = maxOf(radius, hypot(x + 0.5 - bitmap.width / 2, y + 0.5 - bitmap.height / 2))
        }
        assertTrue(visible.width() > bitmap.width / 2)
        assertTrue(abs(visible.exactCenterX() - bitmap.width / 2) <= 1f)
        assertTrue(abs(visible.exactCenterY() - bitmap.height / 2) <= 1f)
        assertTrue(radius <= bitmap.width * 33.0 / 108.0)

        for ((name, id) in listOf("launcher" to R.mipmap.ic_launcher, "launcher-round" to R.mipmap.ic_launcher_round)) {
            val launcher = context.getDrawable(id) as AdaptiveIconDrawable
            assertNotNull(launcher.foreground)
            assertNotNull(launcher.monochrome)
            val drawable = draw(launcher, 192)
            assertEquals(Color.TRANSPARENT, drawable.getPixel(0, 0))
            assertTrue(Color.alpha(drawable.getPixel(96, 96)) == 255)
            save(name, drawable)
        }
    }

    @Test
    fun compiledSplashUsesTheSameMarkWithThemeSpecificInk() {
        val variants = listOf(
            Triple(Configuration.UI_MODE_NIGHT_NO, "light", Color.rgb(0x74, 0x21, 0x3b)),
            Triple(Configuration.UI_MODE_NIGHT_YES, "dark", Color.rgb(0xff, 0xf8, 0xf3)),
        )
        for ((mode, name, ink) in variants) {
            val config = Configuration(context.resources.configuration)
            config.uiMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv() or mode
            val themed = ContextThemeWrapper(context.createConfigurationContext(config), R.style.Theme_TwoHearts)
            val attrs = themed.obtainStyledAttributes(
                intArrayOf(android.R.attr.windowSplashScreenAnimatedIcon, android.R.attr.windowSplashScreenBackground),
            )
            val mark = checkNotNull(attrs.getDrawable(0))
            val background = attrs.getColor(1, Color.TRANSPARENT)
            attrs.recycle()
            assertEquals(themed.getColor(R.color.twohearts_launch_surface), background)
            val rendered = draw(mark, 192)
            val pixels = IntArray(rendered.width * rendered.height)
            rendered.getPixels(pixels, 0, rendered.width, 0, 0, rendered.width, rendered.height)
            val tintedPixels = pixels.count {
                Color.alpha(it) >= 230 &&
                    abs(Color.red(it) - Color.red(ink)) <= 3 &&
                    abs(Color.green(it) - Color.green(ink)) <= 3 &&
                    abs(Color.blue(it) - Color.blue(ink)) <= 3
            }
            assertTrue("Expected the $name ink across the visible strokes, got $tintedPixels pixels", tintedPixels > 500)

            val preview = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888)
            Canvas(preview).apply {
                drawColor(background)
                drawBitmap(rendered, 0f, 0f, null)
            }
            save("splash-$name", preview)
        }
    }

    private fun draw(drawable: Drawable, size: Int): Bitmap =
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
            drawable.setBounds(0, 0, size, size)
            drawable.draw(Canvas(it))
        }

    private fun save(name: String, bitmap: Bitmap) {
        File(output, "transformation-5-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
