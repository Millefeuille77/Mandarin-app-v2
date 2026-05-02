// SpeakingFallback.kt — Mandarin Learn
// Phase 10: extracted from SpeakingScreen.kt to keep that file ≤ 300 lines.
// Contains: legacy preview overload, error-code → string mapper, SpeakingContent layout.
// Per CLAUDE.md: files ≤ 300 lines; split motivated by the line-cap rule.

package com.mandarinlearn.ui.speaking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.domain.model.PronunciationResult
import com.mandarinlearn.ui.components.HskLevelChipRow
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Legacy overload for navigation fallback / preview environments without a ViewModel.
 * Shows a LoadingState. In production, [AppNavigation] always has a valid AppContainer
 * so this overload is never used on a real device.
 */
@Composable
fun SpeakingScreen(
    hsk: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_speaking),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}

/**
 * Maps a [SpeakingErrorCode] to a user-friendly localised string.
 * Only place error codes are converted — change strings here, not in the ViewModel.
 * Must be called from a composable context so that [stringResource] is available.
 */
@Composable
internal fun speakingErrorMessage(code: SpeakingErrorCode): String = when (code) {
    SpeakingErrorCode.OFFLINE       -> stringResource(R.string.speaking_offline_error)
    SpeakingErrorCode.NO_PHRASES    -> stringResource(R.string.speaking_no_phrases_error)
    SpeakingErrorCode.NO_API_KEY    -> stringResource(R.string.speaking_error_no_api_key)
    SpeakingErrorCode.TIMEOUT       -> stringResource(R.string.speaking_error_timeout)
    SpeakingErrorCode.RECORD_FAILED -> stringResource(R.string.speaking_error_record_failed)
    SpeakingErrorCode.NO_PERMISSION -> stringResource(R.string.speaking_permission_title)
    SpeakingErrorCode.UNKNOWN       -> stringResource(R.string.error_generic_message)
}

/**
 * Main content layout for [SpeakingScreen] when a phrase and recording state are available.
 * Extracted to keep SpeakingScreen.kt under the 300-line cap.
 */
@Composable
internal fun SpeakingContent(
    phrase: ConversationPhrase,
    selectedHsk: Int,
    recordingState: RecordingState,
    elapsedSeconds: Int,
    pronunciationResult: PronunciationResult?,
    onLevelSelected: (Int) -> Unit,
    onMicTapped: () -> Unit,
    onTryAgain: () -> Unit,
    onNextPhrase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PagePaddingH, vertical = SpacingM),
        verticalArrangement    = Arrangement.spacedBy(SpacingS),
        horizontalAlignment    = Alignment.CenterHorizontally,
    ) {
        HskLevelChipRow(
            selectedLevel   = selectedHsk,
            onLevelSelected = onLevelSelected,
            modifier        = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(SpacingXs))
        PhraseCard(phrase = phrase, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(SpacingM))

        MicSection(
            recordingState = recordingState,
            elapsedSeconds = elapsedSeconds,
            onMicTapped    = onMicTapped,
            modifier       = Modifier.fillMaxWidth(),
        )

        if (pronunciationResult != null) {
            Spacer(Modifier.height(SpacingS))
            ScoreCard(result = pronunciationResult, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(SpacingS))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingS),
            ) {
                MandarinSecondaryButton(
                    text     = stringResource(R.string.speaking_try_again),
                    onClick  = onTryAgain,
                    modifier = Modifier.weight(1f),
                )
                MandarinPrimaryButton(
                    text     = stringResource(R.string.speaking_next_phrase),
                    onClick  = onNextPhrase,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
