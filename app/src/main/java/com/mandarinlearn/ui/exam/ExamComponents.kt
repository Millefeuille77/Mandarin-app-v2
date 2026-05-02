// ExamComponents.kt — Mandarin Learn
// Sub-composables extracted from ExamScreen to keep files ≤ 300 lines.
// UX_SPECIFICATION.md §4 Screen 8; IMPLEMENTATION_PLAN.md Phase 7 §B.

package com.mandarinlearn.ui.exam

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingL
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Displays a single exam question with A/B/C/D radio options.
 * For HSK 5 essay/summary questions the question is marked "Not auto-graded" per spec.
 *
 * Accessibility: each option has Modifier.semantics { selected; role = RadioButton }.
 */
@Composable
fun ExamQuestionCard(
    question: SampleQuestion,
    selectedAnswer: String?,
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWritingEssay = question.questionType.contains("essay", ignoreCase = true) ||
                         question.questionType.contains("summary", ignoreCase = true)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingS),
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
            modifier  = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(SpacingM)) {
                if (isWritingEssay) {
                    // HSK 5 writing — not auto-graded (IMPLEMENTATION_PLAN.md Phase 7 §B note)
                    Text(
                        text  = question.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text  = stringResource(R.string.exam_not_auto_graded),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = SpacingXs),
                    )
                } else {
                    Text(
                        text  = question.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        // Options A / B / C / D — vertical stack, 64 dp min height each
        val optionKeys = listOf("A", "B", "C", "D")
        question.options.take(4).forEachIndexed { idx, optionText ->
            val key = optionKeys.getOrElse(idx) { idx.toString() }
            val isSelected = selectedAnswer == key
            ExamOptionItem(
                optionKey  = key,
                optionText = optionText,
                isSelected = isSelected,
                onSelect   = { if (!isWritingEssay) onAnswer(key) },
            )
        }
    }
}

@Composable
private fun ExamOptionItem(
    optionKey: String,
    optionText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val itemDesc = stringResource(R.string.exam_option_desc, optionKey, optionText)

    Surface(
        color    = bgColor,
        shape    = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small,
            )
            .clickable(onClick = onSelect)
            .semantics {
                contentDescription = itemDesc
                selected = isSelected
                role = Role.RadioButton
            },
    ) {
        Text(
            text  = "$optionKey.  $optionText",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = SpacingM, vertical = SpacingS),
        )
    }
}

/**
 * 30-second break overlay shown between sections.
 * User can tap "Continue" to skip the countdown.
 */
@Composable
fun SectionBreakOverlay(
    completedSectionName: String,
    nextSectionName: String,
    secondsRemaining: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.exam_section_complete, completedSectionName.replaceFirstChar { it.uppercase() }),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (nextSectionName.isNotBlank()) {
            Text(
                text  = stringResource(R.string.exam_next_section, nextSectionName.replaceFirstChar { it.uppercase() }, secondsRemaining),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SpacingXs, bottom = SpacingL),
            )
        }
        MandarinPrimaryButton(
            text    = stringResource(R.string.exam_continue),
            onClick = onContinue,
        )
    }
}
