package hu.ugorjbe.app.ui

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec

internal fun <T> tween(
    durationMillis: Int,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing,
): TweenSpec<T> = androidx.compose.animation.core.tween(
    durationMillis = durationMillis,
    delayMillis = delayMillis,
    easing = easing,
)
