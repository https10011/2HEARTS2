package com.twohearts.app.ui.screens.yuki

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import java.io.IOException

internal enum class YukiPose {
    SIT, SLEEP, IDLE, STAND, WALK, WALK_ALT, RUN, RUN_ALT, RUN_EXTEND, JUMP, STRETCH,
    EAT, DRINK, PAW, GROOM, GROOM_ALT, SIT_ALT, SAD, ANNOYED, LOAF, ROLL,
    SIT_HALF_BLINK, SIT_BLINK, SIT_EAR_FLICK, SIT_TAIL_LEFT, SIT_TAIL_RIGHT;

    val assetName: String get() = "${name.lowercase()}.png"
}

internal object YukiArt {
    const val WIDTH = 416
    const val HEIGHT = 324
    private const val CACHE_BYTES = 5 * 1024 * 1024

    private val cache = object : LruCache<YukiPose, Bitmap>(CACHE_BYTES) {
        override fun sizeOf(key: YukiPose, value: Bitmap): Int = value.allocationByteCount
    }

    fun cached(pose: YukiPose): Bitmap? = cache.get(pose)

    fun load(context: Context, pose: YukiPose): Bitmap? {
        cached(pose)?.let { return it }
        return try {
            val sprite = context.assets.open("yuki/${pose.assetName}").use { BitmapFactory.decodeStream(it) }
            if (sprite == null || sprite.width != WIDTH || sprite.height != HEIGHT) {
                Log.w("TwoHeartsYuki", "Unable to decode registered pose ${pose.name}")
                return null
            }
            cache.put(pose, sprite)
            sprite
        } catch (failure: IOException) {
            Log.w("TwoHeartsYuki", "Unable to open pose ${pose.name}", failure)
            null
        }
    }
}
