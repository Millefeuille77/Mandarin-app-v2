// ExamResultScreen.kt — Mandarin Learn
// Placeholder for ExamResultScreen.
// UX_SPECIFICATION.md §4 Screen 9: score, per-section breakdown, mistake review, history.
// Full implementation in Phase 7 (Exam Section).

package com.mandarinlearn.ui.exam

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
 * ExamResultScreen — displays the result of a completed HSK mock exam.
 * Placeholder: displays screen title. Full implementation in Phase 7.
 *
 * @param resultId       The exam_results row id to display.
 * @param onNavigateBack Pop this screen off the back stack.
 * @param onTryAgain     Navigate to a new ExamScreen for the same HSK level.
 * @param modifier       Optional modifier.
 */
@Composable
fun ExamResultScreen(
    resultId: Long,
    onNavigateBack: () -> Unit,
    onTryAgain: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_7): Replace with full ExamResultScreen layout per UX spec §4 Screen 9.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_exam_result),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamResultScreenPreview() {
    MandarinLearnTheme {
        ExamResultScreen(
            resultId       = 1L,
            onNavigateBack = {},
            onTryAgain     = {},
        )
    }
}
