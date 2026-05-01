// Type.kt — Mandarin Learn
// Typography scale per UX_SPECIFICATION.md §1.3.
// All sizes are in sp so they respect the system font scale setting.
// The in-app font multiplier (Settings screen, Phase 9) will be layered on top via
// a CompositionLocal<Float> — no changes needed to this file for that feature.

package com.mandarinlearn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Typography for Mandarin Learn.
 *
 * UX spec typography map → M3 slots:
 *   display-large (32sp Bold)   → displayLarge
 *   header-large  (24sp SemiBold) → headlineLarge
 *   header-medium (20sp SemiBold) → headlineMedium
 *   body-large    (18sp Regular) → bodyLarge      (DEFAULT body text)
 *   body-medium   (16sp Regular) → bodyMedium
 *   label-large   (18sp Medium)  → labelLarge
 *
 * Hanzi-specific styles (hanzi-display 56sp, hanzi-large 28sp, etc.) are defined as
 * extension TextStyles below and used directly in composables — they are outside the
 * Material 3 slot system.
 */
val MandarinTypography = Typography(
    // Score numbers, large stats — 32 sp Bold
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    // Screen titles — 24 sp SemiBold
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    // Section headers, dialog titles — 20 sp SemiBold
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    // Default body text — 18 sp Regular (WCAG: never go below this for body)
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    // Secondary info — 16 sp Regular (minimum for most text)
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Button labels — 18 sp Medium
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
)

// --- Extended Hanzi text styles (not M3 slots) ---
// Used directly in composables that render Chinese characters.

/** Flashcard front character — 56 sp (UX spec: hanzi-display). */
val HanziDisplayStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 56.sp,
    lineHeight = 64.sp,
)

/** Inline characters in lists / passages HSK 1–3 — 28 sp (UX spec: hanzi-large). */
val HanziLargeStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 36.sp,
)

/** Passage characters HSK 4–5 — 24 sp (UX spec: hanzi-medium). */
val HanziMediumStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 32.sp,
)

/** Ruby pinyin above characters — 14 sp (UX spec: pinyin). */
val PinyinStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 18.sp,
)

/** Pinyin under flashcard hanzi — 18 sp (UX spec: pinyin-large). */
val PinyinLargeStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 24.sp,
)
