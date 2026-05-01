// HomeScreen.kt — Mandarin Learn
// Placeholder for the HomeScreen (Learn tab root).
// UX_SPECIFICATION.md §4 Screen 1: full implementation in Phase 8 (Progress & Dashboard).
// Phase 1: shows screen name only. Navigation callbacks are wired through MainScaffold.

package com.mandarinlearn.ui.home

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
 * HomeScreen — Learn tab root.
 * Placeholder: displays screen title only.
 * Full implementation in Phase 8.
 *
 * @param onNavigateToVocabulary Navigates to VocabularyScreen for the given HSK level.
 * @param onNavigateToFlashcards Navigates to FlashcardScreen for the given HSK level.
 * @param onNavigateToSettings   Navigates to SettingsScreen.
 * @param modifier               Optional modifier.
 */
@Composable
fun HomeScreen(
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToFlashcards: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_8): Replace with full HomeScreen layout per UX spec §4 Screen 1.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_home),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MandarinLearnTheme {
        HomeScreen(
            onNavigateToVocabulary = {},
            onNavigateToFlashcards = {},
            onNavigateToSettings   = {},
        )
    }
}
