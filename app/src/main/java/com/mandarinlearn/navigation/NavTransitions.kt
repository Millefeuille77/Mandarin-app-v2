// NavTransitions.kt — Mandarin Learn
// Shared enter/exit animations for navigation transitions.
// UX_SPECIFICATION.md §1.7: only fadeIn/fadeOut (200ms) and slideInHorizontally (250ms).

package com.mandarinlearn.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/** Duration for fade transitions (UX spec §1.7). */
private const val FADE_DURATION_MS = 200

/** Duration for horizontal slide transitions (UX spec §1.7). */
private const val SLIDE_DURATION_MS = 250

/**
 * Standard screen-enter animation: slide in from the right with a fade.
 * Used for forward navigation (e.g., tapping an item in a list).
 */
val slideInFromRight: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(SLIDE_DURATION_MS)
    ) + fadeIn(animationSpec = tween(FADE_DURATION_MS))

/**
 * Standard screen-exit animation: slide out to the left with a fade.
 * Used when navigating forward.
 */
val slideOutToLeft: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(SLIDE_DURATION_MS)
    ) + fadeOut(animationSpec = tween(FADE_DURATION_MS))

/**
 * Back-navigation enter: slide in from the left with a fade.
 * Used when the back button reveals the previous screen.
 */
val slideInFromLeft: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(SLIDE_DURATION_MS)
    ) + fadeIn(animationSpec = tween(FADE_DURATION_MS))

/**
 * Back-navigation exit: slide out to the right with a fade.
 */
val slideOutToRight: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(SLIDE_DURATION_MS)
    ) + fadeOut(animationSpec = tween(FADE_DURATION_MS))

/** Simple fade-in for tab-level transitions (no horizontal slide). */
val tabFadeIn: EnterTransition =
    fadeIn(animationSpec = tween(FADE_DURATION_MS))

/** Simple fade-out for tab-level transitions. */
val tabFadeOut: ExitTransition =
    fadeOut(animationSpec = tween(FADE_DURATION_MS))
