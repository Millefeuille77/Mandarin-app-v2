// ExamScreen.kt — Mandarin Learn
// Full Phase 7 ExamScreen implementation.
// UX_SPECIFICATION.md §4 Screen 8; IMPLEMENTATION_PLAN.md Phase 7 §B.
// Split into ExamScreen.kt (< 300 lines) + ExamComponents.kt for sub-composables.

package com.mandarinlearn.ui.exam

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.ConfirmDialog
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingXs
import kotlinx.coroutines.flow.collectLatest

/**
 * Full ExamScreen — timed mock HSK exam that cannot navigate back mid-exam.
 *
 * Back arrow shows [ConfirmDialog] instead of navigating. Timer runs via [ExamViewModel].
 * Section breaks show a 30-second overlay before advancing.
 *
 * @param viewModel          Provides [ExamUiState] and accepts user actions.
 * @param onNavigateToResult Navigate to ExamResultScreen with the generated result id.
 * @param onNavigateBack     Called after the user confirms quit.
 * @param modifier           Optional modifier.
 */
@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    onNavigateToResult: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle one-time events from ViewModel
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ExamEvent.QuitExam -> onNavigateBack()
                is ExamEvent.AnnounceTimer -> { /* Live region handles TalkBack */ }
            }
        }
    }

    // Navigate to result when Done
    val doneState = state as? ExamUiState.Done
    LaunchedEffect(doneState) {
        doneState?.let { onNavigateToResult(it.resultId) }
    }

    // BackHandler intercepts system back — always shows quit dialog
    BackHandler(enabled = state is ExamUiState.ActiveSection || state is ExamUiState.SectionBreak) {
        viewModel.onBackPressed()
    }

    ExamScreenContent(
        state       = state,
        onAnswer    = viewModel::selectAnswer,
        onNext      = viewModel::advance,
        onSkipBreak = viewModel::skipBreak,
        onBack      = viewModel::onBackPressed,
        onConfirmQuit = viewModel::confirmQuit,
        onDismissQuit = viewModel::dismissQuitDialog,
        onReplayAudio = { viewModel.replayCurrentAudio() },
        modifier    = modifier,
    )
}

/** Fallback overload for previews / navigation without a real ViewModel. */
@Composable
fun ExamScreen(hsk: Int, onNavigateToResult: (Long) -> Unit, onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    LoadingState(modifier = modifier.fillMaxSize())
}

@Composable
private fun ExamScreenContent(
    state: ExamUiState,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onSkipBreak: () -> Unit,
    onBack: () -> Unit,
    onConfirmQuit: () -> Unit,
    onDismissQuit: () -> Unit,
    onReplayAudio: () -> Int = { 0 },
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (state) {
            is ExamUiState.Loading, is ExamUiState.Submitting -> {
                MandarinTopBar(title = stringResource(R.string.screen_exam))
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            is ExamUiState.Error -> {
                MandarinTopBar(title = stringResource(R.string.screen_exam))
                ErrorState(message = state.message, onRetry = {}, modifier = Modifier.fillMaxSize())
            }
            is ExamUiState.Done -> {
                MandarinTopBar(title = stringResource(R.string.screen_exam))
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            is ExamUiState.SectionBreak -> {
                val sectionName = state.allSections[state.completedSectionIndex].section.name
                val nextName = state.allSections.getOrNull(state.completedSectionIndex + 1)?.section?.name ?: ""
                MandarinTopBar(
                    title         = stringResource(R.string.screen_exam),
                    onNavigateBack = onBack,
                )
                SectionBreakOverlay(
                    completedSectionName = sectionName,
                    nextSectionName      = nextName,
                    secondsRemaining     = state.breakSecondsRemaining,
                    onContinue           = onSkipBreak,
                    modifier             = Modifier.fillMaxSize(),
                )
            }
            is ExamUiState.ActiveSection -> {
                val examTitle = stringResource(R.string.exam_title_hsk, state.structure.hskLevel)
                MandarinTopBar(
                    title          = examTitle,
                    onNavigateBack = onBack,
                )
                ActiveExamBody(
                    state         = state,
                    onAnswer      = onAnswer,
                    onNext        = onNext,
                    onReplayAudio = onReplayAudio,
                )
                if (state.showQuitDialog) {
                    ConfirmDialog(
                        title       = stringResource(R.string.exam_quit_title),
                        message     = stringResource(R.string.exam_quit_message),
                        confirmText = stringResource(R.string.exam_quit_confirm),
                        dismissText = stringResource(R.string.action_cancel),
                        onConfirm   = onConfirmQuit,
                        onDismiss   = onDismissQuit,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveExamBody(
    state: ExamUiState.ActiveSection,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onReplayAudio: () -> Int = { 0 },
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        ExamProgressHeader(state = state)
        val q = state.currentQuestion
        if (q != null) {
            // Replay button for listening questions (Phase 7 QA M-2 fix)
            if (!q.audioTextChinese.isNullOrBlank()) {
                ReplayAudioButton(onReplayAudio = onReplayAudio)
            }
            ExamQuestionCard(
                question       = q,
                selectedAnswer = state.selectedAnswer,
                onAnswer       = onAnswer,
                modifier       = Modifier.weight(1f),
            )
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text  = stringResource(R.string.exam_no_questions),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val nextLabel = if (state.isLastQuestionInSection && !state.isLastSection) {
            stringResource(R.string.exam_finish_section)
        } else if (state.isLastQuestionInSection && state.isLastSection) {
            stringResource(R.string.exam_submit)
        } else {
            stringResource(R.string.action_next)
        }
        MandarinPrimaryButton(
            text    = nextLabel,
            onClick = onNext,
            enabled = state.selectedAnswer != null,
            modifier = Modifier.padding(bottom = SpacingM),
        )
    }
}

@Composable
private fun ExamProgressHeader(state: ExamUiState.ActiveSection) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
        val sectionLabel = stringResource(
            R.string.exam_section_header,
            state.currentSectionIndex + 1,
            state.allSections.size,
            state.currentSection.section.name.replaceFirstChar { it.uppercase() },
        )
        val timerColor = when {
            state.isTimerCritical -> MaterialTheme.colorScheme.error
            state.isTimerWarning  -> MaterialTheme.colorScheme.tertiary
            else                  -> MaterialTheme.colorScheme.onSurface
        }
        val timerText = stringResource(
            R.string.exam_timer,
            state.timerMinutes,
            state.timerSeconds,
        )
        Text(
            text  = sectionLabel,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text     = timerText,
            style    = MaterialTheme.typography.headlineLarge,
            color    = timerColor,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        LinearProgressIndicator(
            progress = state.progressFraction,
            modifier = Modifier.fillMaxWidth(),
            color    = MaterialTheme.colorScheme.primary,
        )
        val questionCountLabel = stringResource(
            R.string.exam_question_counter,
            state.currentQuestionIndex + 1,
            state.currentSection.questions.size,
        )
        Text(
            text  = questionCountLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReplayAudioButton(onReplayAudio: () -> Int) {
    OutlinedButton(
        onClick  = { onReplayAudio() },
        modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
    ) {
        Icon(
            imageVector        = Icons.Filled.Replay,
            contentDescription = stringResource(R.string.exam_replay_audio_desc),
        )
        Text(
            text     = stringResource(R.string.exam_replay_audio),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamScreenLoadingPreview() {
    MandarinLearnTheme {
        ExamScreen(hsk = 1, onNavigateToResult = {}, onNavigateBack = {})
    }
}
