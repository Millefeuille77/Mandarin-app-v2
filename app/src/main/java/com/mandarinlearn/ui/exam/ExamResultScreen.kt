// ExamResultScreen.kt — Mandarin Learn
// Full Phase 7 ExamResultScreen implementation.
// UX_SPECIFICATION.md §4 Screen 9; IMPLEMENTATION_PLAN.md Phase 7 §C.

package com.mandarinlearn.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.SectionScore
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.components.ProgressBarLabeled
import com.mandarinlearn.ui.components.ScoreBadge
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingL
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ExamResultScreen — shows score, pass/fail, per-section breakdown, mistakes, and history.
 *
 * @param viewModel      Provides [ExamResultUiState].
 * @param onNavigateBack Pop this screen off the stack.
 * @param onTryAgain     Navigate to a fresh ExamScreen for the same HSK level.
 * @param modifier       Optional modifier.
 */
@Composable
fun ExamResultScreen(
    viewModel: ExamResultViewModel,
    onNavigateBack: () -> Unit,
    onTryAgain: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExamResultContent(
        state          = state,
        onNavigateBack = onNavigateBack,
        onTryAgain     = onTryAgain,
        onToggleMistakes = viewModel::toggleMistakes,
        modifier       = modifier,
    )
}

/** Fallback overload for previews / navigation without a real ViewModel. */
@Composable
fun ExamResultScreen(
    resultId: Long,
    onNavigateBack: () -> Unit,
    onTryAgain: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(title = stringResource(R.string.screen_exam_result), onNavigateBack = onNavigateBack)
        LoadingState(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun ExamResultContent(
    state: ExamResultUiState,
    onNavigateBack: () -> Unit,
    onTryAgain: (Int) -> Unit,
    onToggleMistakes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(
            title          = stringResource(R.string.screen_exam_result),
            onNavigateBack = onNavigateBack,
        )
        when (state) {
            is ExamResultUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
            is ExamResultUiState.Error   -> ErrorState(
                message  = state.message,
                onRetry  = {},
                modifier = Modifier.fillMaxSize(),
            )
            is ExamResultUiState.Content -> ResultBody(
                state            = state,
                onTryAgain       = { onTryAgain(state.result.hskLevel) },
                onToggleMistakes = onToggleMistakes,
            )
        }
    }
}

@Composable
private fun ResultBody(
    state: ExamResultUiState.Content,
    onTryAgain: () -> Unit,
    onToggleMistakes: () -> Unit,
) {
    val result = state.result
    val heroDesc = stringResource(
        R.string.exam_result_hero_desc,
        if (result.passed) stringResource(R.string.exam_result_passed) else stringResource(R.string.exam_result_not_yet),
        result.totalScore,
        result.totalMaxScore,
        result.passingScore,
    )
    LazyColumn(
        contentPadding    = androidx.compose.foundation.layout.PaddingValues(
            horizontal = PagePaddingH, vertical = SpacingM
        ),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        // Hero card
        item {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
                modifier  = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = heroDesc },
            ) {
                Column(
                    modifier              = Modifier.padding(SpacingL),
                    horizontalAlignment   = Alignment.CenterHorizontally,
                    verticalArrangement   = Arrangement.spacedBy(SpacingS),
                ) {
                    ScoreBadge(passed = result.passed)
                    Text(
                        text  = "${result.totalScore} / ${result.totalMaxScore}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text  = stringResource(R.string.exam_result_passing_score, result.passingScore),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        // Per-section breakdown
        items(state.sectionScores) { score ->
            SectionScoreRow(score = score)
        }
        // Mistakes card
        item {
            MistakesCard(
                wrongAnswers     = state.wrongAnswers,
                isExpanded       = state.showMistakesExpanded,
                onToggle         = onToggleMistakes,
            )
        }
        // History card
        if (state.history.size > 1) {
            item { HistoryCard(history = state.history) }
        }
        // Try again
        item {
            MandarinPrimaryButton(
                text    = stringResource(R.string.exam_result_try_again),
                onClick = onTryAgain,
                modifier = Modifier.padding(vertical = SpacingS),
            )
        }
    }
}

@Composable
private fun SectionScoreRow(score: SectionScore) {
    val sectionDesc = stringResource(
        R.string.exam_result_section_desc,
        score.name.replaceFirstChar { it.uppercase() },
        score.score,
        score.maxScore,
    )
    val barColor = if (score.score >= score.maxScore * 0.6f) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
    }
    ProgressBarLabeled(
        label    = score.name.replaceFirstChar { it.uppercase() },
        score    = score.score,
        maxScore = score.maxScore,
        barColor = barColor,
        modifier = Modifier.semantics { contentDescription = sectionDesc },
    )
}

@Composable
private fun MistakesCard(
    wrongAnswers: List<WrongAnswerDetail>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)) {
        Column(modifier = Modifier.padding(SpacingM)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text  = stringResource(R.string.exam_result_mistakes_count, wrongAnswers.size),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Spacer(Modifier.height(SpacingXs))
            MandarinSecondaryButton(
                text    = if (isExpanded) stringResource(R.string.exam_result_hide_mistakes)
                          else stringResource(R.string.exam_result_review_mistakes),
                onClick = onToggle,
            )
            if (isExpanded) {
                wrongAnswers.forEach { wrong ->
                    Spacer(Modifier.height(SpacingXs))
                    Text(text = wrong.questionId, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text  = stringResource(R.string.exam_result_your_answer, wrong.userAnswer.ifBlank { stringResource(R.string.exam_result_no_answer) }),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text  = stringResource(R.string.exam_result_correct_answer, wrong.correctAnswer),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(history: List<ExamResult>) {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    Card(elevation = CardDefaults.cardElevation(defaultElevation = CardElevation)) {
        Column(modifier = Modifier.padding(SpacingM), verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            Text(text = stringResource(R.string.exam_result_history_title), style = MaterialTheme.typography.headlineMedium)
            history.forEach { h ->
                val date = fmt.format(Date(h.finishedAt))
                val passLabel = if (h.passed) stringResource(R.string.exam_result_passed) else stringResource(R.string.exam_result_not_yet)
                Text(
                    text  = "$date — ${h.totalScore}/${h.totalMaxScore} · $passLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (h.passed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamResultScreenLoadingPreview() {
    MandarinLearnTheme {
        ExamResultScreen(resultId = 1L, onNavigateBack = {}, onTryAgain = {})
    }
}
