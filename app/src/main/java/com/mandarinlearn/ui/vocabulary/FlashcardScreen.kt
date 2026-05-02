// FlashcardScreen.kt — Mandarin Learn
// Full implementation replacing Phase 1 placeholder.
// UX_SPECIFICATION.md §4 Screen 3: flip animation, SM-2 rating buttons, audio play.

package com.mandarinlearn.ui.vocabulary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.srs.SrsQuality
import com.mandarinlearn.ui.components.ConfirmDialog
import com.mandarinlearn.ui.components.EmptyState
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingL
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * FlashcardScreen — SM-2 spaced repetition review session.
 *
 * @param viewModel      Injected FlashcardViewModel.
 * @param onNavigateBack Pop back stack (with confirm dialog if mid-session).
 * @param reduceMotion   If true, replace rotateY flip with a crossfade.
 * @param modifier       Optional modifier.
 */
@Composable
fun FlashcardScreen(
    viewModel: FlashcardViewModel,
    onNavigateBack: () -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    // Collect one-time snackbar events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FlashcardEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Back press handling: confirm dialog if reviewing
    BackHandler(enabled = uiState is FlashcardUiState.Reviewing) {
        showExitDialog = true
    }

    if (showExitDialog) {
        ConfirmDialog(
            title       = stringResource(R.string.flashcard_exit_title),
            message     = stringResource(R.string.flashcard_exit_message),
            confirmText = stringResource(R.string.flashcard_exit_confirm),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm   = { showExitDialog = false; onNavigateBack() },
            onDismiss   = { showExitDialog = false },
        )
    }

    val counterText = if (uiState is FlashcardUiState.Reviewing) {
        val s = uiState as FlashcardUiState.Reviewing
        stringResource(R.string.flashcard_counter, s.currentIndex + 1, s.totalCards)
    } else ""

    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_flashcard),
                onNavigateBack = { if (uiState is FlashcardUiState.Reviewing) showExitDialog = true else onNavigateBack() },
                actionIcon     = if (counterText.isNotEmpty()) {
                    { Text(counterText, style = MaterialTheme.typography.bodyMedium) }
                } else null,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier     = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is FlashcardUiState.Loading  -> LoadingState(modifier = Modifier.fillMaxSize())
                is FlashcardUiState.Error    -> ErrorState(
                    message = state.message,
                    onRetry = { /* viewModel re-loads */ },
                    modifier = Modifier.fillMaxSize(),
                )
                is FlashcardUiState.Empty    -> EmptyState(
                    icon                   = Icons.Filled.CheckCircle,
                    iconContentDescription = stringResource(R.string.content_desc_empty_icon),
                    title                  = stringResource(R.string.flashcard_empty_title),
                    body                   = stringResource(R.string.flashcard_empty_message),
                    modifier               = Modifier.fillMaxSize(),
                )
                is FlashcardUiState.Reviewing -> {
                    // Session progress bar
                    val progress = if (state.totalCards > 0)
                        state.currentIndex.toFloat() / state.totalCards else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FlashcardContent(
                        state         = state,
                        onFlip        = viewModel::flipCard,
                        onRate        = viewModel::rateCard,
                        onPlayAudio   = viewModel::playAudio,
                        reduceMotion  = reduceMotion,
                    )
                }
                is FlashcardUiState.SessionComplete -> SessionCompleteContent(
                    state         = state,
                    onBack        = onNavigateBack,
                    onContinue    = { /* TODO(phase_3): request more new cards */ },
                )
            }
        }
    }
}

@Composable
private fun FlashcardContent(
    state: FlashcardUiState.Reviewing,
    onFlip: () -> Unit,
    onRate: (SrsQuality) -> Unit,
    onPlayAudio: () -> Unit,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Card surface (flippable)
        FlippableCard(
            card         = state.currentCard,
            isFlipped    = state.isFlipped,
            reduceMotion = reduceMotion,
            onFlip       = onFlip,
            onPlayAudio  = onPlayAudio,
            isAudioLoading = state.isAudioLoading,
            modifier     = Modifier
                .weight(1f)
                .padding(vertical = SpacingL),
        )

        // Rating buttons — only visible on back
        if (state.isFlipped) {
            RatingButtonRow(
                nextIntervals = state.nextIntervals,
                onRate        = onRate,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingM),
            )
        }
    }
}

@Composable
private fun SessionCompleteContent(
    state: FlashcardUiState.SessionComplete,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(PagePaddingH),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.flashcard_session_complete),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text  = stringResource(
                R.string.flashcard_session_stats,
                state.reviewedCount, state.newCount,
            ),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = SpacingXs),
        )
        com.mandarinlearn.ui.components.MandarinPrimaryButton(
            text     = stringResource(R.string.flashcard_back_to_vocabulary),
            onClick  = onBack,
            modifier = Modifier.padding(top = SpacingL),
        )
        if (state.hasMoreNew) {
            com.mandarinlearn.ui.components.MandarinSecondaryButton(
                text     = stringResource(R.string.flashcard_continue_new),
                onClick  = onContinue,
                modifier = Modifier.padding(top = SpacingXs),
            )
        }
    }
}

// ---- Legacy overload used by Phase 1 AppNavigation (no ViewModel param) ----
@Composable
fun FlashcardScreen(
    hsk: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.screen_flashcard))
    }
}

@Preview(showBackground = true)
@Composable
private fun FlashcardScreenLoadingPreview() {
    MandarinLearnTheme {
        Box(Modifier.fillMaxSize()) {
            LoadingState(modifier = Modifier.fillMaxSize())
        }
    }
}
