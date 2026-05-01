// ExamHubScreen.kt — Mandarin Learn
// Placeholder for ExamHubScreen (Exam tab root).
// UX_SPECIFICATION.md §2: links to ExamScreen for each HSK level.
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
 * ExamHubScreen — Exam tab root, lets the user choose an HSK level and start a mock exam.
 * Placeholder: displays screen title. Full implementation in Phase 7.
 *
 * @param onNavigateToExam Navigate to ExamScreen for the given HSK level.
 * @param modifier         Optional modifier.
 */
@Composable
fun ExamHubScreen(
    onNavigateToExam: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_7): Replace with full ExamHubScreen layout per UX spec §2.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_exam_hub),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamHubScreenPreview() {
    MandarinLearnTheme {
        ExamHubScreen(onNavigateToExam = {})
    }
}
