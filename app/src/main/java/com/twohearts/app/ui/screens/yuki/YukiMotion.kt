package com.twohearts.app.ui.screens.yuki

import com.twohearts.app.data.game.YukiActivity
import com.twohearts.app.data.game.YukiMood
import com.twohearts.app.data.game.YukiState
import com.twohearts.app.data.game.yukiMood
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

internal enum class YukiMotion(
    val holds: LongArray,
    val priority: Int = 1,
    private val travel: FloatArray = floatArrayOf(),
    private val lift: FloatArray = floatArrayOf(),
    val facesRight: Boolean = false,
) {
    SETTLE(longArrayOf(200, 240, 240), priority = 2),
    BLINK(longArrayOf(55, 50, 85, 55, 75)),
    EAR(longArrayOf(80, 115, 120)),
    TAIL(longArrayOf(115, 180, 95, 180, 105)),
    ATTENTION(longArrayOf(140, 440, 240)),
    WALK(longArrayOf(160, 250, 270, 250), travel = floatArrayOf(0f, -.027f, -.066f, -.072f)),
    CHASE(
        longArrayOf(200, 320, 240, 270, 290, 300, 620, 760), priority = 4, facesRight = true,
        travel = floatArrayOf(0f, .018f, .042f, .064f, .088f, .105f, .105f, .083f),
        lift = floatArrayOf(0f, 0f, 0f, .020f, .026f, .088f, 0f, 0f),
    ),
    FEED(
        longArrayOf(300, 300, 1430, 200, 170), priority = 4,
        travel = floatArrayOf(-.01f, -.10f, -.14f, -.14f, -.111f),
    ),
    DRINK(
        longArrayOf(280, 280, 1100, 170, 170), priority = 4,
        travel = floatArrayOf(-.01f, -.10f, -.14f, -.14f, -.111f),
    ),
    GROOM(longArrayOf(180, 570, 700, 640, 210), priority = 4),
    PET(longArrayOf(140, 250, 180, 900, 230), priority = 4),
    NAP(longArrayOf(210, 360, 430), priority = 4),
    DROWSE(longArrayOf(650, 2_300, 620)),
    WAKE(longArrayOf(260, 260, 360, 270, 250), priority = 3),
    QUIET(longArrayOf(180, 350, 250)),
    INTERRUPTED(longArrayOf(120, 530, 180), priority = 2);

    val duration = holds.sum()
    fun x(index: Int) = travel.getOrElse(index) { 0f }
    fun y(index: Int) = lift.getOrElse(index) { 0f }
}

internal data class YukiSpriteFrame(
    val pose: YukiPose,
    val nextPose: YukiPose = pose,
    val blend: Float = 0f,
    val x: Float = 0f,
    val lift: Float = 0f,
    val stretch: Float = 1f,
    val facesRight: Boolean = false,
    val nextFacesRight: Boolean = facesRight,
    val focused: YukiMotion? = null,
)

internal fun stillYukiFrame(state: YukiState): YukiSpriteFrame {
    val pose = when (state.activity) {
        YukiActivity.IDLE -> moodPose(yukiMood(state))
        YukiActivity.EATING -> YukiPose.EAT
        YukiActivity.DRINKING -> YukiPose.DRINK
        YukiActivity.PLAYING -> YukiPose.PAW
        YukiActivity.SLEEPING -> YukiPose.SLEEP
        YukiActivity.GROOMING -> YukiPose.GROOM
        YukiActivity.BEING_PETTED, YukiActivity.PURRING -> YukiPose.ROLL
    }
    val atBowl = state.activity == YukiActivity.EATING || state.activity == YukiActivity.DRINKING
    return YukiSpriteFrame(pose = pose, x = if (atBowl) -.14f else 0f)
}

private fun moodPose(mood: YukiMood) = when (mood) {
    YukiMood.SLEEPY -> YukiPose.LOAF
    YukiMood.SAD -> YukiPose.SAD
    else -> YukiPose.SIT
}

/**
 * Drives presentation in monotonic milliseconds. The caller supplies elapsed wall
 * time for a care action once at entry or resume. Resting clips never write saves.
 */
internal class YukiMotionController(
    private val clips: Map<YukiMotion, List<YukiPose>>,
    seed: Long,
) {
    private val random = Random(seed)
    private var activity = YukiActivity.IDLE
    private var activityStartedAt: String? = null
    private var mood = YukiMood.NEUTRAL
    private var lastRefusal = 0
    private var initialized = false
    private var nextIdle = 0L
    private var previousIdle: YukiMotion? = null
    private var running: Playback? = null
    private var frame = YukiSpriteFrame(YukiPose.SIT)

    fun update(state: YukiState, refusal: Int, now: Long, elapsedActivity: Long) {
        val resolvedMood = yukiMood(state)
        val activityChanged = !initialized || activity != state.activity || activityStartedAt != state.activityStartedAt
        val waking = initialized && activity == YukiActivity.SLEEPING && state.activity == YukiActivity.IDLE
        if (!initialized) {
            frame = if (elapsedActivity < 110) YukiSpriteFrame(moodPose(resolvedMood)) else stillYukiFrame(state)
            nextIdle = now + idleDelay()
            lastRefusal = refusal
        }
        activity = state.activity
        activityStartedAt = state.activityStartedAt
        mood = resolvedMood
        initialized = true
        if (activityChanged) {
            val motion = when (activity) {
                YukiActivity.IDLE -> if (waking) YukiMotion.WAKE else null
                YukiActivity.SLEEPING -> YukiMotion.NAP
                YukiActivity.EATING -> YukiMotion.FEED
                YukiActivity.DRINKING -> YukiMotion.DRINK
                YukiActivity.PLAYING -> YukiMotion.CHASE
                YukiActivity.GROOMING -> YukiMotion.GROOM
                YukiActivity.BEING_PETTED, YukiActivity.PURRING -> YukiMotion.PET
            }
            val elapsed = if (waking) 0L else elapsedActivity
            running = null
            if (motion != null && elapsed < minOf(motion.duration, activity.durationMs.takeIf { it > 0 } ?: Long.MAX_VALUE)) {
                start(motion, now, elapsed)
            } else {
                val still = stillYukiFrame(state)
                if (frame.visiblePose != still.pose || frame.x != still.x || frame.visibleFacesRight) {
                    running = Playback(null, listOf(frame.visiblePose, still.pose), now, frame)
                }
            }
            nextIdle = now + idleDelay()
        }
        if (refusal != lastRefusal) {
            lastRefusal = refusal
            if (activity == YukiActivity.IDLE && mood != YukiMood.SLEEPY) start(YukiMotion.INTERRUPTED, now)
        }
    }

    fun sample(time: Long): YukiSpriteFrame {
        val active = running
        if (active != null && time - active.since < active.duration) {
            return active.frameAt(time).let { it.copy(stretch = breathing(time, it.pose)) }.also { frame = it }
        }
        if (active != null) {
            running = null
            if (active.motion == null) frame = YukiSpriteFrame(active.poses.last())
            nextIdle = time + idleDelay()
        }
        if (activity == YukiActivity.IDLE && time >= nextIdle) {
            val selected = pickIdle()
            previousIdle = selected
            start(selected, time)
            nextIdle = time + idleDelay()
            running?.let {
                return it.frameAt(time).copy(stretch = breathing(time, frame.pose)).also { frame = it }
            }
        }
        val pose = if (activity == YukiActivity.SLEEPING) YukiPose.SLEEP else moodPose(mood)
        if (pose != frame.visiblePose || frame.x != 0f || frame.visibleFacesRight) {
            val track = Playback(null, listOf(frame.visiblePose, pose), time, frame)
            running = track
            return track.frameAt(time).copy(stretch = breathing(time, pose)).also { frame = it }
        }
        return YukiSpriteFrame(pose, stretch = breathing(time, pose)).also { frame = it }
    }

    private fun start(motion: YukiMotion, time: Long, elapsed: Long = 0) {
        val current = running?.motion
        if (current != null && motion.priority < current.priority) return
        val frames = clips[motion] ?: return
        val poses = if (motion == YukiMotion.QUIET && mood == YukiMood.SLEEPY) {
            frames.map { if (it == YukiPose.SAD) YukiPose.LOAF else it }
        } else frames
        running = Playback(motion, poses, time - elapsed, frame)
    }

    private fun pickIdle(): YukiMotion {
        val choices = when (mood) {
            YukiMood.SLEEPY -> sleepyIdle
            YukiMood.SAD -> quietIdle
            YukiMood.HUNGRY -> hungryIdle
            else -> socialIdle
        }.filterNot { it.first == previousIdle }
        var chance = random.nextInt(choices.sumOf { it.second })
        for ((motion, weight) in choices) {
            chance -= weight
            if (chance < 0) return motion
        }
        return choices.first().first
    }

    private fun idleDelay(): Long = random.nextLong(2_900L, 6_401L)

    private fun breathing(time: Long, pose: YukiPose): Float {
        val period = if (pose == YukiPose.SLEEP) 4200 else 3300
        return 1f + sin((time % period) * 2.0 * PI / period).toFloat() * .012f
    }

    private class Playback(
        val motion: YukiMotion?,
        val poses: List<YukiPose>,
        val since: Long,
        val incoming: YukiSpriteFrame,
    ) {
        val duration = motion?.duration ?: 210L

        fun frameAt(time: Long): YukiSpriteFrame {
            val age = (time - since).coerceAtLeast(0L)
            if (motion == null) {
                val progress = ease(age / duration.toFloat())
                return YukiSpriteFrame(
                    poses.first(), poses.last(), progress,
                    x = mix(incoming.x, 0f, progress), lift = mix(incoming.lift, 0f, progress),
                    facesRight = incoming.visibleFacesRight, nextFacesRight = false,
                )
            }

            var index = 0
            var within = age
            while (index < poses.lastIndex && within >= motion.holds[index]) within -= motion.holds[index++]
            val hold = motion.holds[index]
            val phase = ease(within / hold.toFloat())
            val x = mix(if (index == 0) incoming.x else motion.x(index - 1), motion.x(index), phase)
            val y = mix(if (index == 0) incoming.lift else motion.y(index - 1), motion.y(index), phase)
            val toNext = minOf(hold, 95L)
            val morph = ease((within - hold + toNext) / toNext.toFloat())
            val following = poses.getOrElse(index + 1) { poses[index] }
            if (index == 0 && age < 90 && (incoming.visiblePose != poses[index] || incoming.visibleFacesRight != motion.facesRight)) {
                return YukiSpriteFrame(
                    incoming.visiblePose, poses[index], ease(age / 90f), x, y,
                    facesRight = incoming.visibleFacesRight, nextFacesRight = motion.facesRight, focused = motion,
                )
            }
            return YukiSpriteFrame(poses[index], following, morph, x, y, facesRight = motion.facesRight, focused = motion)
        }
    }

    private companion object {
        val socialIdle = listOf(
            YukiMotion.BLINK to 5, YukiMotion.TAIL to 4, YukiMotion.EAR to 3,
            YukiMotion.ATTENTION to 3, YukiMotion.WALK to 2, YukiMotion.SETTLE to 1,
        )
        val hungryIdle = listOf(YukiMotion.BLINK to 5, YukiMotion.EAR to 2, YukiMotion.ATTENTION to 4)
        val sleepyIdle = listOf(YukiMotion.DROWSE to 3, YukiMotion.QUIET to 2)
        val quietIdle = listOf(YukiMotion.QUIET to 3, YukiMotion.EAR to 2)
    }
}

private val YukiSpriteFrame.visiblePose get() = if (blend > .5f) nextPose else pose
private val YukiSpriteFrame.visibleFacesRight get() = if (blend > .5f) nextFacesRight else facesRight
private fun ease(progress: Float): Float = progress.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
private fun mix(from: Float, to: Float, fraction: Float) = from + (to - from) * fraction
