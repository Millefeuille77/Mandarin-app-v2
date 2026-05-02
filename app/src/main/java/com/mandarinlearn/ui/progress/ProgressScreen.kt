// ProgressScreen.kt — Mandarin Learn
// Full implementation for Phase 8 (Progress & Dashboard).
// UX_SPECIFICATION.md §4 Screen 10: streak, per-HSK cards, exam chart, readiness.
// Sub-composables extracted to ProgressScreenComponents.kt to honour the 300-line rule.

package com.mandarinlearn.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingXl
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * ProgressScreen — displays learning progress across all HSK levels.
 *
 * @param viewModel            Injected [ProgressViewModel] from AppNavigation.
 * @param onNavigateBack       Pop this screen off the back stack.
 * @param onNavigateToExamResult  Routes to ExamResultScreen when a chart point is tapped.
 * @param modifier             Optional modifier.
 */
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExamResult: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        MandarinTopBar(
            title          = stringResource(R.string.screen_progress),
            onNavigateBack = onNavigateBack,
        )
        when (val state = uiState) {
            is ProgressUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
            is ProgressUiState.Error   -> ErrorState(
                message  = state.message,
                onRetry  = viewModel::retry,
                modifier = Modifier.fillMaxSize(),
            )
            is ProgressUiState.Content -> ProgressContent(
                state                  = state,
                onChartFilterChanged   = viewModel::setChartFilter,
                onNavigateToExamResult = onNavigateToExamResult,
            )
        }
    }
}

/** Legacy overload retained for AppNavigation until Phase 8 wiring lands fully. */
@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_progress),
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

// ---- Content ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgressContent(
    state: ProgressUiState.Content,
    onChartFilterChanged: (Int?) -> Unit,
    onNavigateToExamResult: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReadinessSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        item {
            StreakSummaryCard(
                currentStreak  = state.currentStreak,
                longestStreak  = state.longestStreak,
                activeWeekDays = state.activeWeekDays,
                modifier       = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
            )
        }
        items(state.levelCards) { card ->
            LevelCard(
                card                 = card,
                onReadinessInfoClick = { showReadinessSheet = true },
                modifier             = Modifier.padding(horizontal = PagePaddingH),
            )
        }
        item {
            Text(
                text     = stringResource(R.string.progress_exam_chart_header),
                style    = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
            )
        }
        item {
            ChartFilterRow(
                selected         = state.chartFilterLevel,
                onFilterSelected = onChartFilterChanged,
                modifier         = Modifier.padding(horizontal = PagePaddingH),
            )
        }
        item {
            ExamScoresChart(
                results       = state.chartResults,
                onPointTapped = onNavigateToExamResult,
                modifier      = Modifier.padding(horizontal = PagePaddingH),
            )
        }
    }

    if (showReadinessSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReadinessSheet = false },
            sheetState       = rememberModalBottomSheetState(),
        ) {
            ReadinessFormulaSheet(onDismiss = { showReadinessSheet = false })
        }
    }
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun ProgressScreenPreview() {
    MandarinLearnTheme {
        ProgressContent(
            state = ProgressUiState.Content(
                currentStreak    = 5,
                longestStreak    = 12,
                activeWeekDays   = setOf(1, 2, 4),
                levelCards       = listOf(
                    LevelProgressCard(1, 45, 153, 6, 8, null, 200, 42f),
                    LevelProgressCard(2, 0, 150, 0, 8, null, 200, 0f),
                ),
                chartResults     = emptyList(),
                chartFilterLevel = null,
            ),
            onChartFilterChanged   = {},
            onNavigateToExamResult = {},
        )
    }
}
