// SpeakingScreen.kt — Mandarin Learn
// Full Phase 6 implementation replacing the Phase 1 placeholder.
// UX_SPECIFICATION.md §4 Screen 6: record → Gemini STT → pronunciation score.
// Permission flow per UX §5.4. State machine: Idle/Recording/Processing/Result/Error.

package com.mandarinlearn.ui.speaking

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.ConversationPhrase
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.HskLevelChipRow
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * SpeakingScreen — Phase 6 full implementation.
 *
 * Shows a Chinese phrase, a large mic button to record, and a score card after evaluation.
 * Handles the RECORD_AUDIO permission rationale flow per UX §5.4.
 *
 * @param viewModel      Injected via AppNavigation using [SpeakingViewModel.factory].
 * @param onNavigateBack Pops this screen off the back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun SpeakingScreen(
    viewModel: SpeakingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Snackbar for recording-limit events
    val limitMsg = stringResource(R.string.speaking_recording_limit)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SpeakingEvent.RecordingLimitReached ->
                    snackbarHostState.showSnackbar(limitMsg)
            }
        }
    }

    // Permission launcher — requests RECORD_AUDIO from the system dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // shouldShowRationale is false when permanently denied
        val activity = context as? android.app.Activity
        val showRationale = activity
            ?.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
            ?: false
        viewModel.onPermissionResult(granted, showRationale)
    }

    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_speaking),
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier     = modifier,
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SpeakingScreenState",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { state ->
            when (state) {
                is SpeakingUiState.Loading ->
                    LoadingState(modifier = Modifier.fillMaxSize())

                is SpeakingUiState.PermissionDenied ->
                    SpeakingPermissionDeniedContent(
                        isPermanentlyDenied = state.isPermanentlyDenied,
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onOpenSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                is SpeakingUiState.Error ->
                    ErrorState(
                        message  = state.message,
                        onRetry  = { viewModel.nextPhrase() },
                        modifier = Modifier.fillMaxSize(),
                    )

                is SpeakingUiState.Idle ->
                    SpeakingContent(
                        phrase         = state.phrase,
                        selectedHsk    = state.selectedHsk,
                        recordingState = RecordingState.Idle,
                        elapsedSeconds = 0,
                        pronunciationResult = null,
                        onLevelSelected = viewModel::selectLevel,
                        onMicTapped     = viewModel::onMicTapped,
                        onTryAgain      = viewModel::tryAgain,
                        onNextPhrase    = viewModel::nextPhrase,
                        modifier        = Modifier.fillMaxSize(),
                    )

                is SpeakingUiState.Recording ->
                    SpeakingContent(
                        phrase         = state.phrase,
                        selectedHsk    = state.selectedHsk,
                        recordingState = RecordingState.Recording,
                        elapsedSeconds = state.elapsedSeconds,
                        pronunciationResult = null,
                        onLevelSelected = viewModel::selectLevel,
                        onMicTapped     = viewModel::onMicTapped,
                        onTryAgain      = viewModel::tryAgain,
                        onNextPhrase    = viewModel::nextPhrase,
                        modifier        = Modifier.fillMaxSize(),
                    )

                is SpeakingUiState.Processing ->
                    SpeakingContent(
                        phrase         = state.phrase,
                        selectedHsk    = state.selectedHsk,
                        recordingState = RecordingState.Processing,
                        elapsedSeconds = 0,
                        pronunciationResult = null,
                        onLevelSelected = viewModel::selectLevel,
                        onMicTapped     = viewModel::onMicTapped,
                        onTryAgain      = viewModel::tryAgain,
                        onNextPhrase    = viewModel::nextPhrase,
                        modifier        = Modifier.fillMaxSize(),
                    )

                is SpeakingUiState.Result ->
                    SpeakingContent(
                        phrase         = state.phrase,
                        selectedHsk    = state.selectedHsk,
                        recordingState = RecordingState.Result,
                        elapsedSeconds = 0,
                        pronunciationResult = state.result,
                        onLevelSelected = viewModel::selectLevel,
                        onMicTapped     = viewModel::onMicTapped,
                        onTryAgain      = viewModel::tryAgain,
                        onNextPhrase    = viewModel::nextPhrase,
                        modifier        = Modifier.fillMaxSize(),
                    )
            }
        }
    }
}

/** Internal enum representing recording lifecycle for [SpeakingContent]. */
enum class RecordingState { Idle, Recording, Processing, Result }

@Composable
private fun SpeakingContent(
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

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun SpeakingScreenIdlePreview() {
    val samplePhrase = ConversationPhrase(
        id = "cp_001", hskLevel = 1, category = "Greetings",
        chinese = "你好", pinyin = "nǐ hǎo", english = "Hello",
        usageContext = "Standard greeting",
    )
    MandarinLearnTheme {
        SpeakingContent(
            phrase         = samplePhrase,
            selectedHsk    = 1,
            recordingState = RecordingState.Idle,
            elapsedSeconds = 0,
            pronunciationResult = null,
            onLevelSelected = {}, onMicTapped = {}, onTryAgain = {}, onNextPhrase = {},
        )
    }
}
