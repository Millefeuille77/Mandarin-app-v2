// SettingsScreen.kt — Mandarin Learn
// Placeholder for SettingsScreen.
// UX_SPECIFICATION.md §4 Screen 11: theme, font size, audio speed, export/import/reset.
// Full implementation in Phase 9 (Settings & Polish).

package com.mandarinlearn.ui.settings

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
 * SettingsScreen — app preferences and data management.
 * Placeholder: displays screen title. Full implementation in Phase 9.
 *
 * @param onNavigateBack Pop this screen off the back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_9): Replace with full SettingsScreen layout per UX spec §4 Screen 11.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_settings),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MandarinLearnTheme {
        SettingsScreen(onNavigateBack = {})
    }
}
