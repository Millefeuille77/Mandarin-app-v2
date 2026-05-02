// ExamHubScreen.kt — Mandarin Learn
// Full Phase 7 implementation of the Exam tab root.
// UX_SPECIFICATION.md §2 (ExamHubScreen); IMPLEMENTATION_PLAN.md Phase 7 §A.

package com.mandarinlearn.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Exam tab root — lets the user pick an HSK level and start a mock exam.
 * Displays last attempt score and pass/fail history per level.
 *
 * @param viewModel          Provides [ExamHubUiState].
 * @param onNavigateToExam   Navigate to ExamScreen for the given HSK level.
 * @param modifier           Optional modifier.
 */
@Composable
fun ExamHubScreen(
    viewModel: ExamHubViewModel,
    onNavigateToExam: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExamHubContent(state = state, onNavigateToExam = onNavigateToExam, modifier = modifier)
}

/** Fallback overload used by preview / tests without a real ViewModel. */
@Composable
fun ExamHubScreen(
    onNavigateToExam: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExamHubContent(
        state            = ExamHubUiState.Content(
            levelSummaries = (1..5).map { ExamLevelSummary(it, null, 0) }
        ),
        onNavigateToExam = onNavigateToExam,
        modifier         = modifier,
    )
}

@Composable
private fun ExamHubContent(
    state: ExamHubUiState,
    onNavigateToExam: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(title = stringResource(R.string.screen_exam_hub))
        when (state) {
            is ExamHubUiState.Loading  -> LoadingState(modifier = Modifier.fillMaxSize())
            is ExamHubUiState.Error    -> ErrorState(
                message  = state.message,
                onRetry  = { /* Hub reloads on composition; user can navigate away */ },
                modifier = Modifier.fillMaxSize(),
            )
            is ExamHubUiState.Content  -> ExamHubLevelList(
                summaries        = state.levelSummaries,
                onNavigateToExam = onNavigateToExam,
            )
        }
    }
}

@Composable
private fun ExamHubLevelList(
    summaries: List<ExamLevelSummary>,
    onNavigateToExam: (Int) -> Unit,
) {
    LazyColumn(
        contentPadding    = androidx.compose.foundation.layout.PaddingValues(
            horizontal = PagePaddingH, vertical = SpacingM
        ),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        items(summaries, key = { it.hskLevel }) { summary ->
            ExamLevelCard(summary = summary, onStart = { onNavigateToExam(summary.hskLevel) })
        }
    }
}

@Composable
private fun ExamLevelCard(
    summary: ExamLevelSummary,
    onStart: () -> Unit,
) {
    val cardDesc = buildString {
        append(stringResource(R.string.hsk_level_label, summary.hskLevel))
        if (summary.attemptCount > 0 && summary.bestResult != null) {
            append(". ")
            append(
                stringResource(
                    R.string.exam_hub_last_score,
                    summary.bestResult.totalScore,
                    summary.bestResult.totalMaxScore,
                )
            )
            val passLabel = if (summary.bestResult.passed) {
                stringResource(R.string.exam_result_passed)
            } else {
                stringResource(R.string.exam_result_not_yet)
            }
            append(". $passLabel")
        } else {
            append(". ").append(stringResource(R.string.exam_hub_no_attempts))
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDesc },
    ) {
        Column(
            modifier = Modifier.padding(SpacingM),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text  = stringResource(R.string.hsk_level_label, summary.hskLevel),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (summary.attemptCount > 0 && summary.bestResult != null) {
                LastAttemptRow(summary)
            } else {
                Text(
                    text  = stringResource(R.string.exam_hub_no_attempts),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(SpacingS))
            MandarinPrimaryButton(
                text    = stringResource(R.string.exam_hub_start_exam),
                onClick = onStart,
            )
        }
    }
}

@Composable
private fun LastAttemptRow(summary: ExamLevelSummary) {
    val result = summary.bestResult ?: return
    val passIcon = if (result.passed) Icons.Filled.Check else Icons.Filled.Close
    val passColor = if (result.passed) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }
    val passLabel = if (result.passed) {
        stringResource(R.string.exam_result_passed)
    } else {
        stringResource(R.string.exam_result_not_yet)
    }
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        Icon(
            imageVector        = passIcon,
            contentDescription = passLabel,
            tint               = passColor,
            modifier           = Modifier.size(24.dp),
        )
        Text(
            text  = stringResource(R.string.exam_hub_last_score, result.totalScore, result.totalMaxScore),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text  = passLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = passColor,
        )
    }
    Text(
        text  = stringResource(R.string.exam_hub_attempts, summary.attemptCount),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun ExamHubScreenPreview() {
    MandarinLearnTheme {
        ExamHubScreen(onNavigateToExam = {})
    }
}
