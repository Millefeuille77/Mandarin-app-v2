// ExamScoresChart.kt — Mandarin Learn
// Compose Canvas line chart for exam scores over time.
// Phase 8: Progress & Dashboard (IMPLEMENTATION_PLAN.md §B; UX_SPECIFICATION.md §4 Screen 10).
// NO third-party charting library per ARCHITECTURE.md §1.1.
// Handles 0, 1, 2, and 5+ data points correctly.

package com.mandarinlearn.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingM

/**
 * Compose Canvas line chart showing exam total_score over time.
 *
 * @param results         Exam results, expected to be sorted by finishedAt ascending.
 * @param onPointTapped   Called with the result id when a chart point is tapped.
 * @param modifier        Optional modifier.
 */
@Composable
fun ExamScoresChart(
    results: List<ExamResult>,
    onPointTapped: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Build accessibility description from results.
    val accessibilityDesc = buildChartAccessibilityDescription(results)

    // Pre-compute tap-zone x positions — recalculated on each draw.
    val pointPositions = remember(results) { mutableListOf<Pair<Float, Long>>() }

    if (results.isEmpty()) {
        // Empty state: placeholder text.
        Box(
            modifier          = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment  = Alignment.Center,
        ) {
            Text(
                text  = stringResource(R.string.progress_chart_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = onSurfaceVariant,
            )
        }
        return
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(SpacingM)
            .semantics { contentDescription = accessibilityDesc }
            .pointerInput(results) {
                detectTapGestures { tapOffset ->
                    // Find nearest point within 24 dp tap radius.
                    val hit = pointPositions.minByOrNull { (x, _) ->
                        kotlin.math.abs(x - tapOffset.x)
                    }
                    if (hit != null && kotlin.math.abs(hit.first - tapOffset.x) < 48f) {
                        onPointTapped(hit.second)
                    }
                }
            },
    ) {
        pointPositions.clear()

        val maxScore = results.maxOf { it.totalMaxScore }.coerceAtLeast(1)
        val chartWidth = size.width
        val chartHeight = size.height
        val padLeft = 16f
        val padRight = 16f
        val padTop = 12f
        val padBottom = 24f
        val drawWidth = chartWidth - padLeft - padRight
        val drawHeight = chartHeight - padTop - padBottom

        // Draw horizontal grid lines at 0, 25%, 50%, 75%, 100% of max.
        drawGridLines(drawWidth, drawHeight, padLeft, padTop, padBottom, chartHeight, surfaceVariant)

        if (results.size == 1) {
            // Single point: draw a dot only.
            val cx = padLeft + drawWidth / 2f
            val rawY = results[0].totalScore.toFloat() / maxScore.toFloat()
            val cy = padTop + drawHeight * (1f - rawY)
            drawCircle(color = primaryColor, radius = 8f, center = Offset(cx, cy))
            pointPositions.add(Pair(cx, results[0].id))
            return@Canvas
        }

        // Multiple points: draw line + dots.
        val xs = results.mapIndexed { i, _ ->
            padLeft + i.toFloat() / (results.size - 1).toFloat() * drawWidth
        }
        val ys = results.map { r ->
            padTop + drawHeight * (1f - r.totalScore.toFloat() / maxScore.toFloat())
        }

        val path = Path()
        path.moveTo(xs[0], ys[0])
        for (i in 1 until results.size) {
            path.lineTo(xs[i], ys[i])
        }
        drawPath(path = path, color = primaryColor, style = Stroke(width = 4f))

        // Draw dots.
        results.forEachIndexed { i, result ->
            drawCircle(
                color  = primaryColor,
                radius = 8f,
                center = Offset(xs[i], ys[i]),
            )
            drawCircle(
                color  = Color.White,
                radius = 4f,
                center = Offset(xs[i], ys[i]),
            )
            pointPositions.add(Pair(xs[i], result.id))
        }
    }
}

private fun DrawScope.drawGridLines(
    drawWidth: Float,
    drawHeight: Float,
    padLeft: Float,
    padTop: Float,
    padBottom: Float,
    chartHeight: Float,
    gridColor: Color,
) {
    val levels = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
    levels.forEach { fraction ->
        val y = padTop + drawHeight * (1f - fraction)
        drawLine(
            color       = gridColor,
            start       = Offset(padLeft, y),
            end         = Offset(padLeft + drawWidth, y),
            strokeWidth = 1f,
        )
    }
}

private fun buildChartAccessibilityDescription(results: List<ExamResult>): String {
    if (results.isEmpty()) return "No exam scores recorded yet."
    if (results.size == 1) {
        return "Latest score ${results[0].totalScore} out of ${results[0].totalMaxScore}."
    }
    val latest = results.last()
    val previous = results[results.size - 2]
    val diff = latest.totalScore - previous.totalScore
    val change = when {
        diff > 0 -> "gain of $diff points"
        diff < 0 -> "loss of ${-diff} points"
        else     -> "no change"
    }
    return "Latest score ${latest.totalScore}, previous ${previous.totalScore}, $change."
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun ExamScoresChartEmptyPreview() {
    MandarinLearnTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.progress_exam_chart_header), style = MaterialTheme.typography.headlineMedium)
            ExamScoresChart(results = emptyList(), onPointTapped = {})
        }
    }
}
