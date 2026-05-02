// ListeningScreen.kt — Mandarin Learn
// Full Phase 5 implementation replacing placeholder.
// UX_SPECIFICATION.md §4 Screen 7: audio play button, 4-option quiz, immediate feedback.
// Sub-composables (AudioCard, OptionsGrid, FeedbackCard, SessionCompleteContent)
// extracted to ListeningComponents.kt per 300-line rule.

package com.mandarinlearn.ui.listening

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.ui.components.EmptyState
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.HskLevelChipRow
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * ListeningScreen — play Chinese audio and choose the matching word from 4 options.
 * Full Phase 5 implementation per UX_SPECIFICATION.md §4 Screen 7.
 *
 * @param viewModel      Injected [ListeningViewModel].
 * @param onNavigateBack Pop back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun ListeningScreen(
    viewModel: ListeningViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // TalkBack announcements for answer feedback (UX spec §4 Screen 7)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ListeningEvent.AnnounceAnswer ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar       = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_listening),
                onNavigateBack = onNavigateBack,
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
                is ListeningUiState.Loading ->
                    LoadingState(modifier = Modifier.fillMaxSize())

                is ListeningUiState.Error ->
                    ErrorState(
                        message  = state.message,
                        onRetry  = {},
                        modifier = Modifier.fillMaxSize(),
                    )

                is ListeningUiState.Empty ->
                    EmptyState(
                        icon                   = Icons.Filled.VolumeUp,
                        iconContentDescription = stringResource(R.string.content_desc_empty_icon),
                        title                  = stringResource(R.string.listening_empty_title),
                        body                   = stringResource(R.string.listening_empty_body),
                        modifier               = Modifier.fillMaxSize(),
                    )

                is ListeningUiState.Content ->
                    ListeningContent(
                        state           = state,
                        onLevelSelected = viewModel::selectLevel,
                        onPlay          = viewModel::playAudio,
                        onSelectAnswer  = viewModel::selectAnswer,
                        onNext          = viewModel::nextQuestion,
                        onSkip          = viewModel::skipQuestion,
                    )

                is ListeningUiState.SessionComplete ->
                    SessionCompleteContent(
                        state   = state,
                        onRetry = viewModel::retrySession,
                        onDone  = onNavigateBack,
                    )
            }
        }
    }
}

@Composable
private fun ListeningContent(
    state: ListeningUiState.Content,
    onLevelSelected: (Int) -> Unit,
    onPlay: () -> Unit,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(SpacingXs))

        // HSK level selector chips
        HskLevelChipRow(
            selectedLevel   = state.hskLevel,
            onLevelSelected = onLevelSelected,
            modifier        = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(SpacingS))

        // "Question 3 of 10" counter
        Text(
            text  = stringResource(R.string.listening_question_counter, state.questionIndex, state.totalQuestions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(SpacingM))

        // Audio play card (extracted to ListeningComponents.kt)
        AudioCard(
            isPlaying   = state.isAudioPlaying,
            audioFailed = state.audioFailed,
            replayCount = state.replayCount,
            onPlay      = onPlay,
            onSkip      = onSkip,
        )

        Spacer(Modifier.height(SpacingM))

        if (!state.hasAnswered) {
            // 2×2 options grid before answering
            OptionsGrid(
                options        = state.options,
                selectedIndex  = state.selectedOptionIndex,
                hasAnswered    = false,
                correctIndex   = state.correctOptionIndex,
                onSelectAnswer = onSelectAnswer,
                modifier       = Modifier.weight(1f),
            )
        } else {
            // Feedback after answering
            FeedbackCard(
                isCorrect   = state.isCorrect,
                correctText = state.options.getOrNull(state.correctOptionIndex) ?: "",
                explanation = state.currentQuestion.explanation,
            )
            Spacer(Modifier.weight(1f))
            MandarinPrimaryButton(
                text     = stringResource(R.string.action_next),
                onClick  = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpacingM),
            )
        }
    }
}

// Legacy overload — preserved for Phase 1 AppNavigation compatibility
@Composable
fun ListeningScreen(
    hsk: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_listening),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}

@Preview(showBackground = true, name = "ListeningScreen — Placeholder")
@Composable
private fun ListeningScreenPlaceholderPreview() {
    MandarinLearnTheme {
        ListeningScreen(hsk = 1, onNavigateBack = {})
    }
}
