// ScoreBadge.kt — Mandarin Learn
// Reusable score / pass-fail badge component.
// UX_SPECIFICATION.md §4 Screen 9; IMPLEMENTATION_PLAN.md Phase 7 §D.
// Color is NEVER the sole indicator — always paired with icon + text (CLAUDE.md rule).

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingXs
import com.mandarinlearn.ui.theme.SpacingXxs

/**
 * Pass/Fail badge used on [ExamResultScreen].
 *
 * Shows color + icon + text — color is never the sole indicator (CLAUDE.md).
 *
 * @param passed         True = PASSED (success), False = NOT YET (error).
 * @param modifier       Optional modifier.
 */
@Composable
fun ScoreBadge(
    passed: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (passed) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }
    val contentColor = if (passed) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onError
    }
    val icon = if (passed) Icons.Filled.Check else Icons.Filled.Close
    val label = if (passed) {
        stringResource(R.string.exam_result_passed)
    } else {
        stringResource(R.string.exam_result_not_yet)
    }
    val iconDesc = if (passed) {
        stringResource(R.string.exam_result_passed_icon_desc)
    } else {
        stringResource(R.string.exam_result_failed_icon_desc)
    }

    Surface(
        color  = containerColor,
        shape  = RoundedCornerShape(24.dp),
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
    ) {
        Row(
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingXxs),
            modifier = Modifier.padding(horizontal = SpacingXs, vertical = SpacingXxs),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = iconDesc,
                tint               = contentColor,
                modifier           = Modifier.size(28.dp),
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.displayLarge,
                color = contentColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScoreBadgePassedPreview() {
    MandarinLearnTheme { ScoreBadge(passed = true) }
}

@Preview(showBackground = true)
@Composable
private fun ScoreBadgeFailedPreview() {
    MandarinLearnTheme { ScoreBadge(passed = false) }
}
