// Shapes.kt — Mandarin Learn
// Corner-radius shape tokens per UX_SPECIFICATION.md §1.4.
// Card: 12 dp | Button: 8 dp | Pill chip: 24 dp.

package com.mandarinlearn.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape scale for Mandarin Learn.
 *
 * UX spec §1.4 shape values:
 *   Card corner radius: 12 dp  → mapped to M3 medium
 *   Button radius:       8 dp  → mapped to M3 small
 *   Pill chips:         24 dp  → mapped to M3 extraLarge
 */
val MandarinShapes = Shapes(
    // Buttons — 8 dp (UX spec: Button radius)
    small = RoundedCornerShape(8.dp),
    // Cards — 12 dp (UX spec: Card corner radius)
    medium = RoundedCornerShape(12.dp),
    // Bottom sheets, dialogs — slightly larger
    large = RoundedCornerShape(16.dp),
    // Pill chips — 24 dp (UX spec: Pill chips)
    extraLarge = RoundedCornerShape(24.dp),
)
