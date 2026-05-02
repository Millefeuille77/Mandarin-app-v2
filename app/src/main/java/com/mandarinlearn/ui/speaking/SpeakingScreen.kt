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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar

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

                is SpeakingUiState.Error -> {
                    // Map error code to localised string — no hardcoded strings in UI layer.
                    val errMsg = speakingErrorMessage(state.errorCode)
                    ErrorState(
                        message  = errMsg,
                        onRetry  = { viewModel.nextPhrase() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

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

/** Lifecycle state for the recording button — used by [SpeakingContent]. */
enum class RecordingState { Idle, Recording, Processing, Result }
// SpeakingContent, legacy overload, and error-code mapper live in SpeakingFallback.kt
// (extracted in Phase 10 to keep this file under the 300-line cap).
