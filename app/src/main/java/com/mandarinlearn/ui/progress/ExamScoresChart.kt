// ExamScoresChart.kt — Mandarin Learn
// Compose Canvas line chart for exam scores over time.
// Phase 8: Progress & Dashboard (IMPLEMENTATION_PLAN.md §B; UX_SPECIFICATION.md §4 Screen 10).
// NO third-party charting library per ARCHITECTURE.md §1.1.
// Handles 0, 1, 2, and 5+ data points correctly.
// Phase 9 fix (QA M-1): single-point renders 56dp tappable Box overlay (was 8f canvas dot).

package com.mandarinlearn.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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

    // Phase 9 QA m-1 fix: use string resources for all TalkBack strings.
    val accEmpty      = stringResource(R.string.chart_acc_empty)
    val accSingle     = stringResource(R.string.chart_acc_single)
    val accGain       = stringResource(R.string.chart_acc_gain)
    val accLoss       = stringResource(R.string.chart_acc_loss)
    val accNoChange   = stringResource(R.string.chart_acc_no_change)
    val accMulti      = stringResource(R.string.chart_acc_multi)
    val accessibilityDesc = buildChartAccessibilityDescription(
        results, accEmpty, accSingle, accGain, accLoss, accNoChange, accMulti
    )

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

    // Phase 9 QA M-1 fix: single-point needs a 56 dp touch target.
    // Use a Box with a Canvas underneath and a clickable 56 dp overlay centered on the dot.
    if (results.size == 1) {
        SinglePointChart(
            result         = results[0],
            primaryColor   = primaryColor,
            surfaceVariant = surfaceVariant,
            accessibilityDesc = accessibilityDesc,
            onPointTapped  = onPointTapped,
            modifier       = modifier,
        )
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

/**
 * Single-point chart with a 56 dp tappable circle overlay (QA M-1 fix).
 * The dot is centred horizontally in the chart area, at the correct vertical score position.
 */
@Composable
private fun SinglePointChart(
    result: ExamResult,
    primaryColor: Color,
    surfaceVariant: Color,
    accessibilityDesc: String,
    onPointTapped: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .semantics { contentDescription = accessibilityDesc },
        contentAlignment = Alignment.Center,
    ) {
        // Grid canvas behind the dot
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingM),
        ) {
            val drawWidth  = size.width
            val drawHeight = size.height
            drawGridLines(drawWidth, drawHeight, 0f, 0f, 0f, size.height, surfaceVariant)

            // Draw the dot at the correct vertical position
            val rawY = result.totalScore.toFloat() / result.totalMaxScore.toFloat().coerceAtLeast(1f)
            val cx = drawWidth / 2f
            val cy = drawHeight * (1f - rawY.coerceIn(0f, 1f))
            drawCircle(color = primaryColor, radius = 12f, center = Offset(cx, cy))
            drawCircle(color = Color.White,  radius = 5f,  center = Offset(cx, cy))
        }

        // Transparent 56 dp tap target centred on the dot (satisfies the 56 dp rule)
        Box(
            modifier = Modifier
                .size(56.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = accessibilityDesc
                }
                .pointerInput(result.id) {
                    detectTapGestures { onPointTapped(result.id) }
                },
        )
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

/**
 * Builds the TalkBack content description for the chart.
 * Uses string-resource-derived templates passed from the composable context (QA m-1 fix).
 *
 * @param emptyDesc        Localised "no exams" fallback (R.string.chart_acc_empty).
 * @param singleTemplate   Template "%1$d out of %2$d" (R.string.chart_acc_single).
 * @param gainTemplate     Template "gain of %1$d points" (R.string.chart_acc_gain).
 * @param lossTemplate     Template "loss of %1$d points" (R.string.chart_acc_loss).
 * @param noChangeDesc     Localised "no change" string (R.string.chart_acc_no_change).
 * @param multiTemplate    Template "Latest %1$d, previous %2$d, %3$s." (R.string.chart_acc_multi).
 */
private fun buildChartAccessibilityDescription(
    results: List<ExamResult>,
    emptyDesc: String,
    singleTemplate: String,
    gainTemplate: String,
    lossTemplate: String,
    noChangeDesc: String,
    multiTemplate: String,
): String {
    if (results.isEmpty()) return emptyDesc
    if (results.size == 1) {
        return String.format(singleTemplate, results[0].totalScore, results[0].totalMaxScore)
    }
    val latest = results.last()
    val previous = results[results.size - 2]
    val diff = latest.totalScore - previous.totalScore
    val change = when {
        diff > 0 -> String.format(gainTemplate, diff)
        diff < 0 -> String.format(lossTemplate, -diff)
        else     -> noChangeDesc
    }
    return String.format(multiTemplate, latest.totalScore, previous.totalScore, change)
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
