// ProgressBarLabeled.kt — Mandarin Learn
// Labeled linear progress bar component used by ExamResultScreen and ProgressScreen.
// IMPLEMENTATION_PLAN.md Phase 7 §D.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingXxs

/**
 * A labeled progress bar displaying a section name, score, and progress track.
 *
 * @param label      Section name (e.g. "Listening").
 * @param score      Achieved score.
 * @param maxScore   Maximum score for this section (denominator).
 * @param barColor   Override color; defaults to primary.
 * @param modifier   Optional modifier.
 */
@Composable
fun ProgressBarLabeled(
    label: String,
    score: Int,
    maxScore: Int,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Unspecified,
) {
    val fraction = if (maxScore == 0) 0f else (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)
    val resolvedColor = if (barColor == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        barColor
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingXxs),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = "$score / $maxScore",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        LinearProgressIndicator(
            progress = fraction,
            color    = resolvedColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(vertical = SpacingXxs),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressBarLabeledPreview() {
    MandarinLearnTheme {
        ProgressBarLabeled(label = "Listening", score = 78, maxScore = 100)
    }
}
