// MeScreen.kt — Mandarin Learn
// Placeholder for MeScreen (Me tab root).
// UX_SPECIFICATION.md §2: links to ProgressScreen and SettingsScreen.
// Full implementation in Phase 8 (Progress & Dashboard).

package com.mandarinlearn.ui.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * MeScreen — Me tab root, links to Progress and Settings.
 * Placeholder: displays screen title. Full implementation in Phase 8.
 *
 * @param onNavigateToProgress Navigate to ProgressScreen.
 * @param onNavigateToSettings Navigate to SettingsScreen.
 * @param modifier             Optional modifier.
 */
@Composable
fun MeScreen(
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_8): Replace with full MeScreen layout (Progress + Settings nav items).
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_me),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeScreenPreview() {
    MandarinLearnTheme {
        MeScreen(
            onNavigateToProgress = {},
            onNavigateToSettings = {},
        )
    }
}
