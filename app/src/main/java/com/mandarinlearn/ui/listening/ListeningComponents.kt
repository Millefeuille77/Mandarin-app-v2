// ListeningComponents.kt — Mandarin Learn
// Sub-composables for ListeningScreen extracted for 300-line rule compliance.
// Contains: AudioCard, OptionsGrid, FeedbackCard, SessionCompleteContent.
// Per CLAUDE.md: every file ≤ 300 lines.

package com.mandarinlearn.ui.listening

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.theme.AudioPlayButtonSize
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingL
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Audio play card — large play button, replay counter, skip on failure.
 * Per UX_SPECIFICATION.md §4 Screen 7: 96 dp play button, 3 replay cap.
 */
@Composable
internal fun AudioCard(
    isPlaying: Boolean,
    audioFailed: Boolean,
    replayCount: Int,
    onPlay: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canReplay = replayCount < 3
    val playDesc = stringResource(R.string.listening_play_button_desc)
    val replayDesc = stringResource(R.string.listening_replay_desc)

    Card(
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(SpacingL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick  = onPlay,
                enabled  = !isPlaying && (canReplay || replayCount == 0),
                modifier = Modifier
                    .size(AudioPlayButtonSize)
                    .semantics { contentDescription = playDesc },
                contentPadding = PaddingValues(0.dp),
            ) {
                if (isPlaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color    = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Filled.VolumeUp,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                    )
                }
            }
            Spacer(Modifier.height(SpacingXs))
            Text(
                text  = stringResource(R.string.listening_tap_to_play),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (replayCount > 0) {
                Spacer(Modifier.height(SpacingXs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick  = onPlay,
                        enabled  = !isPlaying && canReplay,
                        modifier = Modifier
                            .size(MinTouchTarget)
                            .semantics { contentDescription = replayDesc },
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Replay,
                            contentDescription = null,
                            tint               = if (canReplay)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text  = stringResource(R.string.listening_replays_remaining, 3 - replayCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (audioFailed) {
                Spacer(Modifier.height(SpacingXs))
                Text(
                    text  = stringResource(R.string.listening_audio_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                MandarinSecondaryButton(text = stringResource(R.string.listening_skip), onClick = onSkip)
            }
        }
    }
}

/**
 * 2×2 grid of answer options. Renders before the user has answered.
 */
@Composable
internal fun OptionsGrid(
    options: List<String>,
    selectedIndex: Int?,
    hasAnswered: Boolean,
    correctIndex: Int,
    onSelectAnswer: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        modifier              = modifier.fillMaxWidth(),
        verticalArrangement   = Arrangement.spacedBy(SpacingS),
        horizontalArrangement = Arrangement.spacedBy(SpacingS),
    ) {
        itemsIndexed(options) { index, optionText ->
            val optionLabel = listOf("A", "B", "C", "D").getOrNull(index) ?: ""
            val isSelected  = selectedIndex == index
            val optionDesc  = stringResource(R.string.listening_option_desc, optionLabel, optionText)

            OutlinedButton(
                onClick  = { onSelectAnswer(index) },
                enabled  = !hasAnswered,
                border   = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                ),
                modifier = Modifier
                    .height(88.dp)
                    .semantics { contentDescription = optionDesc },
            ) {
                Text(
                    text      = optionText,
                    style     = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Feedback card shown after the user selects an answer.
 * Uses icon + color + text — never color alone (accessibility rule).
 */
@Composable
internal fun FeedbackCard(
    isCorrect: Boolean,
    correctText: String,
    explanation: String,
    modifier: Modifier = Modifier,
) {
    val feedbackDesc = if (isCorrect) {
        stringResource(R.string.listening_feedback_correct_desc)
    } else {
        stringResource(R.string.listening_feedback_incorrect_desc, correctText)
    }

    Card(
        colors   = CardDefaults.cardColors(
            containerColor = if (isCorrect) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.errorContainer,
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = feedbackDesc },
    ) {
        Row(
            modifier          = Modifier.padding(SpacingM),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector        = if (isCorrect) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint               = if (isCorrect) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onErrorContainer,
                modifier           = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(SpacingXs))
            Column {
                Text(
                    text  = if (isCorrect) stringResource(R.string.listening_correct)
                    else stringResource(R.string.listening_incorrect),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCorrect) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
                )
                if (!isCorrect) {
                    Text(
                        text  = stringResource(R.string.listening_correct_answer, correctText),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                if (explanation.isNotBlank()) {
                    Spacer(Modifier.height(SpacingXs))
                    Text(
                        text  = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCorrect) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

/**
 * End-of-session summary screen.
 */
@Composable
internal fun SessionCompleteContent(
    state: ListeningUiState.SessionComplete,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(PagePaddingH),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.listening_session_complete),
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(SpacingXs))
        Text(
            text  = stringResource(R.string.listening_session_score, state.correctCount, state.totalQuestions),
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SpacingL))
        MandarinPrimaryButton(text = stringResource(R.string.listening_try_again), onClick = onRetry)
        Spacer(Modifier.height(SpacingXs))
        MandarinSecondaryButton(text = stringResource(R.string.action_done), onClick = onDone)
    }
}
