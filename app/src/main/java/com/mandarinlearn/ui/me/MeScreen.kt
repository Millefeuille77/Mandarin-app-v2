// MeScreen.kt — Mandarin Learn
// Full implementation for Phase 8 (Progress & Dashboard).
// UX_SPECIFICATION.md §2: Me tab root — links to ProgressScreen and SettingsScreen.
// Shows current streak at a glance; navigation hub for Me tab.

package com.mandarinlearn.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * MeScreen — Me tab root.
 *
 * @param viewModel            [MeViewModel] injected by MainScaffold.
 * @param onNavigateToProgress Navigate to ProgressScreen.
 * @param onNavigateToSettings Navigate to SettingsScreen.
 * @param modifier             Optional modifier.
 */
@Composable
fun MeScreen(
    viewModel: MeViewModel,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(title = stringResource(R.string.nav_me))
        when (val state = uiState) {
            is MeUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
            is MeUiState.Error   -> MeContentFallback(
                onNavigateToProgress = onNavigateToProgress,
                onNavigateToSettings = onNavigateToSettings,
            )
            is MeUiState.Content -> MeContent(
                currentStreak        = state.currentStreak,
                longestStreak        = state.longestStreak,
                onNavigateToProgress = onNavigateToProgress,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
}

/** Legacy overload for MainScaffold until Phase 8 wiring update completes. */
@Composable
fun MeScreen(
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(title = stringResource(R.string.nav_me))
        MeContentFallback(
            onNavigateToProgress = onNavigateToProgress,
            onNavigateToSettings = onNavigateToSettings,
            modifier             = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MeContent(
    currentStreak: Int,
    longestStreak: Int,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH, vertical = SpacingM),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        // Streak at-a-glance
        val streakDesc = stringResource(R.string.home_streak_content_desc, currentStreak, longestStreak)
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = streakDesc },
            horizontalArrangement = Arrangement.spacedBy(SpacingM),
        ) {
            StreakMiniCard(
                label    = stringResource(R.string.home_streak_current),
                value    = currentStreak,
                modifier = Modifier.weight(1f),
            )
            StreakMiniCard(
                label    = stringResource(R.string.home_streak_longest),
                value    = longestStreak,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(SpacingS))
        // Navigation items
        NavRow(
            label      = stringResource(R.string.screen_progress),
            icon       = Icons.Filled.BarChart,
            iconDesc   = stringResource(R.string.me_progress_icon_desc),
            onClick    = onNavigateToProgress,
        )
        NavRow(
            label      = stringResource(R.string.screen_settings),
            icon       = Icons.Filled.Settings,
            iconDesc   = stringResource(R.string.content_desc_settings_icon),
            onClick    = onNavigateToSettings,
        )
    }
}

@Composable
private fun MeContentFallback(
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH, vertical = SpacingM),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        NavRow(
            label    = stringResource(R.string.screen_progress),
            icon     = Icons.Filled.BarChart,
            iconDesc = stringResource(R.string.me_progress_icon_desc),
            onClick  = onNavigateToProgress,
        )
        NavRow(
            label    = stringResource(R.string.screen_settings),
            icon     = Icons.Filled.Settings,
            iconDesc = stringResource(R.string.content_desc_settings_icon),
            onClick  = onNavigateToSettings,
        )
    }
}

@Composable
private fun StreakMiniCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = modifier,
    ) {
        Column(
            modifier            = Modifier.padding(SpacingM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = value.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NavRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowDesc = stringResource(R.string.me_nav_row_desc, label)
    Card(
        onClick   = onClick,
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTouchTarget)
            .semantics { contentDescription = rowDesc },
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = SpacingM, vertical = SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingM),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = iconDesc,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(28.dp),
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun MeScreenPreview() {
    MandarinLearnTheme {
        MeContent(
            currentStreak        = 5,
            longestStreak        = 12,
            onNavigateToProgress = {},
            onNavigateToSettings = {},
        )
    }
}
