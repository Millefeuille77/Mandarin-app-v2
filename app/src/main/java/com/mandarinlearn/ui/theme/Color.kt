// Color.kt — Mandarin Learn
// Design system color tokens per UX_SPECIFICATION.md §1.2 (WCAG AAA verified — 7:1 contrast).
// These are the raw color values; MaterialTheme binds them via Theme.kt.
// NO inline hex literals are allowed anywhere else in the codebase — always use these names.

package com.mandarinlearn.ui.theme

import androidx.compose.ui.graphics.Color

// --- Light theme palette (UX spec §1.2) ---
val Primary            = Color(0xFF1565C0)
val OnPrimary          = Color(0xFFFFFFFF)
val Secondary          = Color(0xFF5D4037)
val OnSecondary        = Color(0xFFFFFFFF)
val Background         = Color(0xFFFAFAFA)
val OnBackground       = Color(0xFF1A1A1A)
val Surface            = Color(0xFFFFFFFF)
val OnSurface          = Color(0xFF1A1A1A)
val SurfaceVariant     = Color(0xFFEEEEEE)
val OnSurfaceVariant   = Color(0xFF424242)
val Error              = Color(0xFFC62828)
val OnError            = Color(0xFFFFFFFF)

// --- Dark theme palette (UX spec §1.2) ---
val PrimaryDark        = Color(0xFF90CAF9)
val OnPrimaryDark      = Color(0xFF0D47A1)
val SecondaryDark      = Color(0xFFBCAAA4)
val OnSecondaryDark    = Color(0xFF3E2723)
val BackgroundDark     = Color(0xFF121212)
val OnBackgroundDark   = Color(0xFFF5F5F5)
val SurfaceDark        = Color(0xFF1E1E1E)
val OnSurfaceDark      = Color(0xFFF5F5F5)
val SurfaceVariantDark = Color(0xFF2A2A2A)
val OnSurfaceVariantDark = Color(0xFFBDBDBD)
val ErrorDark          = Color(0xFFEF9A9A)
val OnErrorDark        = Color(0xFFB71C1C)

// --- Semantic / extended colors (not in M3 baseline — used via ColorScheme.extensions) ---
// Success: correct answers, "Easy" button, "Passed" badge
val Success            = Color(0xFF2E7D32)
val OnSuccess          = Color(0xFFFFFFFF)
val SuccessDark        = Color(0xFFA5D6A7)
val OnSuccessDark      = Color(0xFF1B5E20)

// Warning: "Hard" button, time warnings, timer < 5 min
val Warning            = Color(0xFFEF6C00)
val OnWarning          = Color(0xFFFFFFFF)
val WarningDark        = Color(0xFFFFCC80)
val OnWarningDark      = Color(0xFFE65100)
