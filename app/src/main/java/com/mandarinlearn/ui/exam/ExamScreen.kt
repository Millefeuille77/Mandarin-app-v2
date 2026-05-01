// ExamScreen.kt — Mandarin Learn
// Placeholder for ExamScreen.
// UX_SPECIFICATION.md §4 Screen 8: timed mock HSK exam, section-by-section.
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
 * ExamScreen — timed mock HSK exam, cannot navigate back mid-exam.
 * Placeholder: displays screen title. Full implementation in Phase 7.
 *
 * @param hsk                 The HSK level for this exam.
 * @param onNavigateToResult  Navigate to ExamResultScreen with the new result ID.
 * @param onNavigateBack      Shows confirm-quit dialog then pops on confirm.
 * @param modifier            Optional modifier.
 */
@Composable
fun ExamScreen(
    hsk: Int,
    onNavigateToResult: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_7): Replace with full ExamScreen layout per UX spec §4 Screen 8.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_exam),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamScreenPreview() {
    MandarinLearnTheme {
        ExamScreen(
            hsk                = 1,
            onNavigateToResult = {},
            onNavigateBack     = {},
        )
    }
}
