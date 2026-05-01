// Theme.kt — Mandarin Learn
// MandarinLearnTheme wraps MaterialTheme with the custom color scheme and typography.
// Supports light/dark mode automatically (and manual override in Phase 9 / Settings).
// UX_SPECIFICATION.md §1.1: build on MaterialTheme (M3) with a MandarinLearnTheme wrapper.

package com.mandarinlearn.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Light color scheme (UX spec §1.2) ---
private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    secondary          = Secondary,
    onSecondary        = OnSecondary,
    background         = Background,
    onBackground       = OnBackground,
    surface            = Surface,
    onSurface          = OnSurface,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = OnSurfaceVariant,
    error              = Error,
    onError            = OnError,
)

// --- Dark color scheme (UX spec §1.2) ---
private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryDark,
    onPrimary          = OnPrimaryDark,
    secondary          = SecondaryDark,
    onSecondary        = OnSecondaryDark,
    background         = BackgroundDark,
    onBackground       = OnBackgroundDark,
    surface            = SurfaceDark,
    onSurface          = OnSurfaceDark,
    surfaceVariant     = SurfaceVariantDark,
    onSurfaceVariant   = OnSurfaceVariantDark,
    error              = ErrorDark,
    onError            = OnErrorDark,
)

/**
 * App-level theme wrapper.
 *
 * @param darkTheme If true, use dark color scheme. Defaults to system setting.
 *                  Phase 9 (Settings) will inject this via DataStore.
 * @param content The composable content to theme.
 */
@Composable
fun MandarinLearnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Update the system status-bar color to match the theme background.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = MandarinTypography,
        shapes      = MandarinShapes,
        content     = content,
    )
}

// --- Extended color accessors (for Success/Warning which are not in M3 baseline) ---
// Usage: MaterialTheme.colorScheme.success (via extension property below).
// Full integration in later phases when these colors are needed in composables.

/** Returns the semantic success color for the current theme. */
@Composable
fun successColor() = if (isSystemInDarkTheme()) SuccessDark else Success

/** Returns the semantic on-success color for the current theme. */
@Composable
fun onSuccessColor() = if (isSystemInDarkTheme()) OnSuccessDark else OnSuccess

/** Returns the semantic warning color for the current theme. */
@Composable
fun warningColor() = if (isSystemInDarkTheme()) WarningDark else Warning

/** Returns the semantic on-warning color for the current theme. */
@Composable
fun onWarningColor() = if (isSystemInDarkTheme()) OnWarningDark else OnWarning
