package com.twohearts.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.ContentTransform
import androidx.navigation.NavBackStackEntry
import com.twohearts.app.ui.theme.TwoHeartsTokens

/**
 * NavigationTransitions — how movement through TwoHearts is expressed.
 *
 * ## What changed in Phase 2, and why
 *
 * The migrated router applied one transition to every route: a 300ms fade
 * plus a small vertical slide. That had three problems.
 *
 *  1. **Every movement looked identical.** Switching tabs slid like opening
 *     a detail page, so the transition carried no information about what had
 *     actually happened. Movement in an app is meaningful: a lateral switch
 *     between peers is not the same thing as a step down into a detail.
 *  2. **The durations were literal `300`s**, so they ignored the Phase 1
 *     motion tokens *and* the reduce-motion preference that the tokens
 *     encode. Reduced motion collapsed nothing, because nothing consulted it.
 *  3. **The vertical slide was arbitrary.** TwoHearts navigates
 *     horizontally in the user's mental model (pushed detail, back), so a
 *     vertical offset read as a glitch rather than as direction.
 *
 * The model now has three movement kinds, matching the three kinds of
 * movement the product actually has:
 *
 *  - [NavMotion.LATERAL] — a peer switch (the five top-level areas).
 *    A short cross-fade: same altitude, no direction implied.
 *  - [NavMotion.PUSH] — a step down into a detail, editor or modal surface.
 *    Content enters from the trailing edge and leaves toward it.
 *  - [NavMotion.ROOT] — the start destination, which nothing precedes.
 *    A plain fade, because there is no "from" to be relative to.
 *
 * All durations come from [TwoHeartsTokens.Duration]; all of it collapses to
 * a plain cross-fade under reduced motion. Nothing here exceeds 240ms — the
 * directive's "restrained slide ... avoid long animations" allowance is the
 * budget, not a target.
 */
object NavigationTransitions {

    /** The three kinds of movement the app has. */
    enum class NavMotion { LATERAL, PUSH, ROOT }

    /**
     * Classify a movement.
     *
     * A transition to a primary area is lateral; a transition to anything
     * else is a push. This is derived from [NavConfig] rather than hardcoded
     * per route, so a new secondary screen automatically gets the right
     * movement without anyone remembering to configure it.
     */
    fun motionFor(targetRoute: String?): NavMotion = when {
        targetRoute == null -> NavMotion.ROOT
        NavConfig.isPrimaryRoute(targetRoute) -> NavMotion.LATERAL
        else -> NavMotion.PUSH
    }

    /** Duration of the movement, honouring reduced motion. */
    private fun duration(reduced: Boolean, base: Long): Int =
        if (reduced) 0 else base.toInt()

    /**
     * Enter/exit pair for a forward movement into [targetRoute].
     */
    fun forward(
        targetRoute: String?,
        reduced: Boolean,
    ): ContentTransform {
        val d = duration(reduced, TwoHeartsTokens.Duration.standard)
        return when (motionFor(targetRoute)) {
            NavMotion.LATERAL -> fadeIn(tween(d)) togetherWith fadeOut(tween(d))
            NavMotion.ROOT -> fadeIn(tween(d)) togetherWith fadeOut(tween(d))
            NavMotion.PUSH -> slideInHorizontally(
                animationSpec = tween(d),
                initialOffsetX = { it / 8 },
            ) + fadeIn(tween(d)) togetherWith fadeOut(tween(d))
        }
    }

    /**
     * Enter/exit pair for a backward movement (pop) out of [fromRoute].
     *
     * A popped detail slides back toward the trailing edge, mirroring the
     * push, so back feels like the reverse of the step the user took.
     */
    fun backward(
        fromRoute: String?,
        reduced: Boolean,
    ): ContentTransform {
        val d = duration(reduced, TwoHeartsTokens.Duration.standard)
        return when (motionFor(fromRoute)) {
            NavMotion.LATERAL -> fadeIn(tween(d)) togetherWith fadeOut(tween(d))
            NavMotion.ROOT -> fadeIn(tween(d)) togetherWith fadeOut(tween(d))
            NavMotion.PUSH -> fadeIn(tween(d)) togetherWith (
                slideOutHorizontally(
                    animationSpec = tween(d),
                    targetOffsetX = { it / 8 },
                ) + fadeOut(tween(d))
                )
        }
    }
}

/**
 * Convenience for a `NavHost`'s `enterTransition` slot.
 *
 * Kept as a thin extension so the router reads as configuration rather than
 * animation plumbing.
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.thForward(
    targetRoute: String?,
    reduced: Boolean,
): EnterTransition = NavigationTransitions
    .forward(targetRoute, reduced)
    .targetContentEnter

/** Exit half of a forward movement. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.thForwardExit(
    targetRoute: String?,
    reduced: Boolean,
): ExitTransition = NavigationTransitions
    .forward(targetRoute, reduced)
    .initialContentExit

/** Enter half of a backward movement. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.thBackEnter(
    fromRoute: String?,
    reduced: Boolean,
): EnterTransition = NavigationTransitions
    .backward(fromRoute, reduced)
    .targetContentEnter

/** Exit half of a backward movement. */
fun AnimatedContentTransitionScope<NavBackStackEntry>.thBackExit(
    fromRoute: String?,
    reduced: Boolean,
): ExitTransition = NavigationTransitions
    .backward(fromRoute, reduced)
    .initialContentExit