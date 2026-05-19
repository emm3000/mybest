package com.emm.mybest.core.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private const val PUSH_POP_DURATION_MS = 280
private const val TAB_SWITCH_DURATION_MS = 200

/**
 * Fraction of screen width used for the parallax slide.
 * ~10 % feels premium; full-width replacement would be too aggressive.
 */
private const val SLIDE_FRACTION = 0.10f

private val pushEasing: Easing = Easing { fraction ->
    // Cubic ease-out: decelerates into place.
    val t = fraction - 1f
    t * t * t + 1f
}

/** Forward push: incoming slides in from the right (+), outgoing drifts left (−). */
internal fun pushTransition(): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> (fullWidth * SLIDE_FRACTION).toInt() },
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    ) + fadeIn(
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth * SLIDE_FRACTION).toInt() },
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    ) + fadeOut(
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    )

/** Back pop: incoming slides in from the left (−), outgoing drifts right (+). */
internal fun popTransition(): ContentTransform =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth * SLIDE_FRACTION).toInt() },
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    ) + fadeIn(
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * SLIDE_FRACTION).toInt() },
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    ) + fadeOut(
        animationSpec = tween(durationMillis = PUSH_POP_DURATION_MS, easing = pushEasing),
    )

/** Tab switch (Home ↔ History ↔ Insights ↔ Timeline): pure crossfade, no motion. */
internal fun tabSwitchTransition(): ContentTransform =
    fadeIn(
        animationSpec = tween(durationMillis = TAB_SWITCH_DURATION_MS),
    ) togetherWith fadeOut(
        animationSpec = tween(durationMillis = TAB_SWITCH_DURATION_MS),
    )
