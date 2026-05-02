// HomeScreen.kt — Mandarin Learn
// Full implementation for Phase 8 (Progress & Dashboard).
// UX_SPECIFICATION.md §4 Screen 1: streak, due-count CTA, HSK progress bars, quick-start grid.
// Sub-composables extracted to HomeScreenComponents.kt to honour the 300-line file rule.

package com.mandarinlearn.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MinTouchTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * HomeScreen — Learn tab root.
 *
 * @param viewModel               Injected [HomeViewModel] from AppNavigation factory.
 * @param onNavigateToVocabulary  Navigates to VocabularyScreen for the given HSK level.
 * @param onNavigateToFlashcards  Navigates to FlashcardScreen for the given HSK level.
 * @param onNavigateToReading     Navigates to ReadingListScreen for the given HSK level.
 * @param onNavigateToListening   Navigates to ListeningScreen for the given HSK level.
 * @param onNavigateToSpeaking    Navigates to SpeakingScreen for the given HSK level.
 * @param onNavigateToSettings    Navigates to SettingsScreen.
 * @param modifier                Optional modifier.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToFlashcards: (Int) -> Unit,
    onNavigateToReading: (Int) -> Unit,
    onNavigateToListening: (Int) -> Unit,
    onNavigateToSpeaking: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(
            title = stringResource(R.string.app_name),
            actionIcon = {
                IconButton(
                    onClick  = onNavigateToSettings,
                    modifier = Modifier.size(MinTouchTarget),
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.content_desc_settings_icon),
                        tint               = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        )

        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
            is HomeUiState.Error   -> ErrorState(
                message  = state.message,
                onRetry  = viewModel::retry,
                modifier = Modifier.fillMaxSize(),
            )
            is HomeUiState.Content -> HomeContent(
                state                  = state,
                onNavigateToVocabulary = onNavigateToVocabulary,
                onNavigateToFlashcards = onNavigateToFlashcards,
                onNavigateToReading    = onNavigateToReading,
                onNavigateToListening  = onNavigateToListening,
                onNavigateToSpeaking   = onNavigateToSpeaking,
            )
        }
    }
}

/**
 * Legacy overload for MainScaffold Phase 1 wiring — kept for compile safety while
 * MainScaffold migrates to the ViewModel overload above.
 */
@Composable
fun HomeScreen(
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToFlashcards: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_home),
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun HomeScreenContentPreview() {
    MandarinLearnTheme {
        HomeContent(
            state = HomeUiState.Content(
                currentStreak = 5,
                longestStreak = 12,
                totalDueCount = 18,
                levelRows = listOf(
                    LevelProgressRow(1, 45, 153),
                    LevelProgressRow(2, 10, 150),
                    LevelProgressRow(3, 0, 300),
                    LevelProgressRow(4, 0, 310),
                    LevelProgressRow(5, 0, 300),
                ),
                focusLevel = 1,
            ),
            onNavigateToVocabulary = {},
            onNavigateToFlashcards = {},
            onNavigateToReading    = {},
            onNavigateToListening  = {},
            onNavigateToSpeaking   = {},
        )
    }
}
