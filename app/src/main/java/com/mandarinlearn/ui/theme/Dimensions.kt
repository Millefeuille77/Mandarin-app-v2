// Dimensions.kt — Mandarin Learn
// Spacing, touch-target, and elevation tokens per UX_SPECIFICATION.md §1.4 and §1.5.
// Use these constants throughout the app — never hardcode values inline.

package com.mandarinlearn.ui.theme

import androidx.compose.ui.unit.dp

// --- Touch targets (HARD rule — ARCHITECTURE.md §1.5) ---
/** Minimum touch-target size for ALL interactive elements. */
val MinTouchTarget = 56.dp

// --- Standard spacings (4 dp grid — UX spec §1.4) ---
val SpacingXxs  = 4.dp
val SpacingXs   = 8.dp
val SpacingS    = 12.dp
val SpacingM    = 16.dp
val SpacingL    = 24.dp
val SpacingXl   = 32.dp

/** Standard horizontal page padding (UX spec §1.4). */
val PagePaddingH = 16.dp

// --- Component dimensions ---
/** Top bar height (UX spec §3.1). */
val TopBarHeight = 64.dp

/** Bottom navigation height (UX spec §3.2 / §1.6). */
val BottomNavHeight = 80.dp

/** Bottom nav icon size (UX spec §3.2). */
val BottomNavIconSize = 28.dp

/** Standard card elevation (UX spec §1.4). */
val CardElevation = 2.dp

/** Bottom navigation shadow elevation (UX spec §1.4). */
val BottomNavElevation = 3.dp

/** Flashcard maximum height (UX spec §4 Screen 3). */
val FlashcardMaxHeight = 480.dp

/** Mic button size on SpeakingScreen (UX spec §4 Screen 6). */
val MicButtonSize = 96.dp

/** Audio play button size on ListeningScreen (UX spec §4 Screen 7). */
val AudioPlayButtonSize = 96.dp

/** OfflineBanner height (UX spec §3.10). */
val OfflineBannerHeight = 48.dp
