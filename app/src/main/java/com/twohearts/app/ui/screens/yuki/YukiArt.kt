package com.twohearts.app.ui.screens.yuki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.LruCache

internal enum class YukiPose(val asset: String) {
    SIT("001-Yuki-sitting.png"),
    SLEEP("002-Yuki-Sleeing.png"),
    IDLE("003-Yuki-Idle.png"),
    STAND("004-Yuki-Standing.png"),
    WALK("005-Yuki-Walking.png"),
    WALK_ALT("006-Yuki-Walking-2.png"),
    RUN("007-Yuki-Running.png"),
    RUN_ALT("008-Yuki-Running-2.png"),
    RUN_EXTEND("009-Yuki-Running-3.png"),
    JUMP("010-Yuki-Jumping.png"),
    STRETCH("011-Yuki-Stretching.png"),
    EAT("012-Yuki-Eating.png"),
    DRINK("013-Yuki-Drinking.png"),
    PAW("014-Yuki-Smacking-Playful.png"),
    GROOM("015-Yuki-Grooming.png"),
    GROOM_ALT("017-Yuki-Grooming-2.png"),
    SIT_ALT("018-Yuki-Sitiing-Idle-2.png"),
    SAD("019-Yuki-Sad.png"),
    ANNOYED("020-Yuki-Angry.png"),
    LOAF("021-Yuki-Loaf.png"),
    ROLL("022-Yuki-Playful-2.png"),
}

internal object YukiArt {
    // Cache only the most recently displayed artwork. A sprite never exceeds 512 px.
    private val cache = object : LruCache<YukiPose, Bitmap>(5 * 1024 * 1024) {
        override fun sizeOf(key: YukiPose, value: Bitmap): Int = value.allocationByteCount
    }

    fun cached(pose: YukiPose): Bitmap? = cache.get(pose)

    fun load(context: Context, pose: YukiPose): Bitmap? {
        cached(pose)?.let { return it }
        return try {
            val decoded = context.assets.open("yuki/${pose.asset}").use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 2 })
            } ?: return null
            val longest = maxOf(decoded.width, decoded.height)
            val scaled = if (longest <= 512) decoded else {
                Bitmap.createScaledBitmap(decoded, decoded.width * 512 / longest, decoded.height * 512 / longest, true)
            }
            val transparent = removeCheckerMatte(scaled)
            cache.put(pose, transparent)
            transparent
        } catch (failure: java.io.IOException) {
            android.util.Log.w("TwoHeartsYuki", "Unable to open pose ${pose.name}", failure)
            null
        }
    }

    // The supplied poses contain a solid grey/white checker matte. Remove
    // only pale neutral pixels connected to the frame edge. The interior
    // white fur and the warm-coloured original artwork keep their pixels.
    internal fun removeCheckerMatte(input: Bitmap): Bitmap {
        val width = input.width
        val height = input.height
        val pixels = IntArray(width * height)
        val queue = IntArray(pixels.size)
        val visited = BooleanArray(pixels.size)
        input.getPixels(pixels, 0, width, 0, 0, width, height)
        var length = 0
        var read = 0

        fun visit(index: Int) {
            if (visited[index]) return
            visited[index] = true
            val color = pixels[index]
            val high = maxOf(Color.red(color), Color.green(color), Color.blue(color))
            val low = minOf(Color.red(color), Color.green(color), Color.blue(color))
            if (Color.alpha(color) == 0 || low >= 170 && high - low <= 22) {
                pixels[index] = Color.TRANSPARENT
                queue[length++] = index
            }
        }
        repeat(width) { x ->
            visit(x)
            visit((height - 1) * width + x)
        }
        repeat(height) { y ->
            visit(y * width)
            visit(y * width + width - 1)
        }
        while (read < length) {
            val index = queue[read++]
            val x = index % width
            if (x > 0) visit(index - 1)
            if (x + 1 < width) visit(index + 1)
            if (index >= width) visit(index - width)
            if (index + width < pixels.size) visit(index + width)
        }
        // Decontaminate the narrow grey fringe where the resampled outline
        // meets the pale matte. Preserve fully coloured outline and fur.
        repeat(pixels.size) { index ->
            val color = pixels[index]
            if (Color.alpha(color) == 0) return@repeat
            val x = index % width
            val bordersMatte = (x > 0 && pixels[index - 1] == Color.TRANSPARENT) ||
                (x < width - 1 && pixels[index + 1] == Color.TRANSPARENT) ||
                (index >= width && pixels[index - width] == Color.TRANSPARENT) ||
                (index + width < pixels.size && pixels[index + width] == Color.TRANSPARENT)
            if (!bordersMatte) return@repeat
            val high = maxOf(Color.red(color), Color.green(color), Color.blue(color))
            val low = minOf(Color.red(color), Color.green(color), Color.blue(color))
            if (low > 155 && high - low < 65) {
                val alpha = ((high - low - 18).coerceIn(0, 47) * Color.alpha(color)) / 47
                pixels[index] = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            }
        }
        var left = width - 1
        var right = 0
        var top = height - 1
        var bottom = 0
        pixels.forEachIndexed { index, color ->
            if (Color.alpha(color) > 0) {
                left = minOf(left, index % width)
                right = maxOf(right, index % width)
                top = minOf(top, index / width)
                bottom = maxOf(bottom, index / width)
            }
        }
        val clean = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        if (right <= left || bottom <= top) return input
        return Bitmap.createBitmap(clean, left, top, right - left + 1, bottom - top + 1)
    }
}
