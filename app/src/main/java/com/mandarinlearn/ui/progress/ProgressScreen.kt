// ProgressScreen.kt — Mandarin Learn
// Placeholder for ProgressScreen.
// UX_SPECIFICATION.md §4 Screen 10: streak, per-HSK progress, exam chart, readiness.
// Full implementation in Phase 8 (Progress & Dashboard).

package com.mandarinlearn.ui.progress

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
 * ProgressScreen — displays learning progress across all HSK levels.
 * Placeholder: displays screen title. Full implementation in Phase 8.
 *
 * @param onNavigateBack Pop this screen off the back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_8): Replace with full ProgressScreen layout per UX spec §4 Screen 10.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_progress),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressScreenPreview() {
    MandarinLearnTheme {
        ProgressScreen(onNavigateBack = {})
    }
}
