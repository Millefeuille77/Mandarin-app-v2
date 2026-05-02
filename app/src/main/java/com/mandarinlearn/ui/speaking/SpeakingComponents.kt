// SpeakingComponents.kt — Mandarin Learn
// Sub-composables for SpeakingScreen: PhraseCard, MicSection, ScoreCard, PermissionDenied.
// Extracted to keep SpeakingScreen.kt under the 300-line cap per CLAUDE.md.
// Per UX_SPECIFICATION.md §4 Screen 6.

package com.mandarinlearn.ui.speaking

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.model.PronunciationResult
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MicButtonSize
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs
import com.mandarinlearn.ui.theme.successColor
import com.mandarinlearn.ui.theme.warningColor

/** Phrase card: category, pinyin, hanzi, English. Per UX §4 Screen 6 §3. */
@Composable
fun PhraseCard(
    phrase: ConversationPhrase,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = modifier,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(SpacingM),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = phrase.category.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SpacingXs))
            Text(
                text      = phrase.pinyin,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = phrase.chinese,
                style     = MaterialTheme.typography.displayLarge,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(SpacingXs))
            Text(
                text      = phrase.english,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
        }
    }
}
/** Large mic button (≥ 96 dp) + status text. Pulsing red while recording. Per UX §4 Screen 6 §4. */
@Composable
fun MicSection(
    recordingState: RecordingState,
    elapsedSeconds: Int,
    onMicTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val micDesc = when (recordingState) {
        RecordingState.Idle       -> stringResource(R.string.speaking_mic_desc_idle)
        RecordingState.Recording  -> stringResource(R.string.speaking_mic_desc_recording)
        RecordingState.Processing -> stringResource(R.string.speaking_mic_desc_processing)
        RecordingState.Result     -> stringResource(R.string.speaking_mic_desc_idle)
    }
    val statusText = when (recordingState) {
        RecordingState.Idle       -> stringResource(R.string.speaking_tap_to_record)
        RecordingState.Recording  -> stringResource(R.string.speaking_recording_timer, elapsedSeconds)
        RecordingState.Processing -> stringResource(R.string.speaking_processing)
        RecordingState.Result     -> stringResource(R.string.speaking_tap_to_record)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MicPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label         = "MicPulseAlpha",
    )
    val isEnabled = recordingState == RecordingState.Idle || recordingState == RecordingState.Recording

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingS),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (recordingState == RecordingState.Recording) {
                Box(
                    modifier = Modifier
                        .size(MicButtonSize + 24.dp)
                        .alpha(pulseAlpha)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.3f), CircleShape)
                )
            }
            val micBgColor = if (recordingState == RecordingState.Recording)
                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            IconButton(
                onClick  = onMicTapped,
                enabled  = isEnabled,
                modifier = Modifier
                    .size(MicButtonSize)
                    .background(micBgColor, CircleShape)
                    .semantics { contentDescription = micDesc; liveRegion = LiveRegionMode.Assertive },
            ) {
                if (recordingState == RecordingState.Processing) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(36.dp),
                        color       = Color.White,
                        strokeWidth = 3.dp,
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Filled.Mic,
                        contentDescription = null, // parent carries the description
                        modifier           = Modifier.size(48.dp),
                        tint               = Color.White,
                    )
                }
            }
        }

        Text(
            text      = statusText,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier  = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
        )
    }
}

/**
 * Score card: ≥85 success (green+star), 70–84 primary (blue+check), <70 warning (orange+!).
 * Color is never the sole indicator — paired with icon + text per accessibility rule. UX §4.6.5.
 */
@Composable
fun ScoreCard(
    result: PronunciationResult,
    modifier: Modifier = Modifier,
) {
    val score = result.score
    val (scoreColor, scoreIcon, scoreLabel) = when {
        score >= 85 -> Triple(successColor(), Icons.Filled.Star, stringResource(R.string.speaking_score_excellent))
        score >= 70 -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.Check, stringResource(R.string.speaking_score_good))
        else        -> Triple(warningColor(), Icons.Filled.PriorityHigh, stringResource(R.string.speaking_score_keep_trying))
    }

    val scoreDesc = stringResource(R.string.speaking_score_content_desc, score, scoreLabel, result.feedback)
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = modifier.semantics {
            contentDescription = scoreDesc
            liveRegion = LiveRegionMode.Polite
        },
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(SpacingM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Icon(
                imageVector        = scoreIcon,
                contentDescription = null, // parent carries semantics
                tint               = scoreColor,
                modifier           = Modifier.size(36.dp),
            )
            Text(
                text  = score.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = scoreColor,
            )
            Text(
                text  = scoreLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = scoreColor,
            )
            Spacer(Modifier.height(SpacingXs))
            Text(
                text      = result.feedback,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            if (result.transcribedText.isNotBlank()) {
                Spacer(Modifier.height(SpacingXs))
                Text(
                    text  = stringResource(R.string.speaking_transcription, result.transcribedText),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Permission rationale per UX §5.4. Shows "Grant" or "Open settings" based on [isPermanentlyDenied]. */
@Composable
fun SpeakingPermissionDeniedContent(
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier.padding(horizontal = PagePaddingH),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector        = Icons.Filled.Mic,
            contentDescription = stringResource(R.string.speaking_permission_title),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(SpacingM))
        Text(
            text      = stringResource(R.string.speaking_permission_title),
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SpacingXs))
        Text(
            text      = stringResource(R.string.speaking_permission_rationale),
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SpacingM))
        if (isPermanentlyDenied) {
            MandarinPrimaryButton(
                text    = stringResource(R.string.speaking_open_settings),
                onClick = onOpenSettings,
            )
        } else {
            MandarinPrimaryButton(
                text    = stringResource(R.string.speaking_grant_permission),
                onClick = onRequestPermission,
            )
        }
    }
}
