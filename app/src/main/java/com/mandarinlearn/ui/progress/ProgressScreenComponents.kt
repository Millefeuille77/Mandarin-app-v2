// ProgressScreenComponents.kt — Mandarin Learn
// Sub-composables for ProgressScreen extracted per the 300-line file rule.
// Phase 8: StreakSummaryCard, WeekActivityBar, LevelCard, SubBar, ChartFilterRow,
//           ReadinessFormulaSheet.
// UX_SPECIFICATION.md §4 Screen 10 (accessibility annotations applied throughout).

package com.mandarinlearn.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun StreakSummaryCard(
    currentStreak: Int,
    longestStreak: Int,
    activeWeekDays: Set<Int>,
    modifier: Modifier = Modifier,
) {
    val desc = stringResource(R.string.home_streak_content_desc, currentStreak, longestStreak)
    Card(
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = modifier
            .fillMaxWidth()
            .semantics { contentDescription = desc },
    ) {
        Column(modifier = Modifier.padding(SpacingM)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(stringResource(R.string.home_streak_current),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currentStreak.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.home_streak_longest),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(longestStreak.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(SpacingS))
            WeekActivityBar(activeWeekDays = activeWeekDays)
        }
    }
}

@Composable
internal fun WeekActivityBar(activeWeekDays: Set<Int>, modifier: Modifier = Modifier) {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        dayLabels.forEachIndexed { index, label ->
            val active = index in activeWeekDays
            val color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    shape  = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier.defaultMinSize(minWidth = 32.dp, minHeight = 32.dp),
                ) {}
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun LevelCard(
    card: LevelProgressCard,
    onReadinessInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val readinessPct = card.readinessPct.toInt()
    val levelDesc = stringResource(R.string.progress_level_card_desc, card.hskLevel, readinessPct)
    Card(
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = modifier
            .fillMaxWidth()
            .semantics { contentDescription = levelDesc },
    ) {
        Column(modifier = Modifier.padding(SpacingM)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.hsk_level_label, card.hskLevel),
                    style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.progress_readiness_badge, readinessPct),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(SpacingXs))
            SubBar(stringResource(R.string.progress_vocab_label),
                card.masteredCount, card.totalVocabCount, card.vocabPct / 100f)
            Spacer(Modifier.height(SpacingXs))
            SubBar(stringResource(R.string.progress_reading_label),
                card.readingCompleted, card.readingTotal, card.readingPct / 100f)
            Spacer(Modifier.height(SpacingXs))
            val examScore = card.bestExamResult?.totalScore ?: 0
            SubBar(stringResource(R.string.progress_exam_label),
                examScore, card.totalMaxScore, card.bestExamPct / 100f)
            if (card.bestExamResult != null) {
                Spacer(Modifier.height(SpacingXs))
                val date = Instant.ofEpochMilli(card.bestExamResult.finishedAt)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                Text(
                    text  = stringResource(R.string.progress_best_exam_date,
                        examScore, card.totalMaxScore, date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(SpacingXs))
            TextButton(
                onClick  = onReadinessInfoClick,
                modifier = Modifier.defaultMinSize(minHeight = MinTouchTarget),
            ) {
                Text(stringResource(R.string.progress_readiness_info_link),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
internal fun SubBar(
    label: String,
    current: Int,
    total: Int,
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$current / $total", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
        }
        LinearProgressIndicator(
            progress   = fraction.coerceIn(0f, 1f),
            color      = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier   = Modifier.fillMaxWidth().height(6.dp),
        )
    }
}

@Composable
internal fun ChartFilterRow(
    selected: Int?,
    onFilterSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick  = { onFilterSelected(null) },
                label    = { Text(stringResource(R.string.progress_chart_filter_all)) },
                modifier = Modifier.defaultMinSize(minHeight = MinTouchTarget),
            )
        }
        items((1..5).toList()) { level ->
            FilterChip(
                selected = selected == level,
                onClick  = { onFilterSelected(level) },
                label    = { Text(stringResource(R.string.hsk_level_label, level)) },
                modifier = Modifier.defaultMinSize(minHeight = MinTouchTarget),
            )
        }
    }
}

@Composable
internal fun ReadinessFormulaSheet(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(SpacingM),
        verticalArrangement = Arrangement.spacedBy(SpacingS),
    ) {
        Text(stringResource(R.string.progress_readiness_info_title),
            style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.progress_readiness_formula),
            style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.progress_readiness_capped),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(
            onClick  = onDismiss,
            modifier = Modifier.defaultMinSize(minHeight = MinTouchTarget),
        ) {
            Text(stringResource(R.string.action_done))
        }
        Spacer(Modifier.height(SpacingM))
    }
}
