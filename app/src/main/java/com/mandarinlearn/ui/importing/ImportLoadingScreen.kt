// ImportLoadingScreen.kt — Mandarin Learn
// Full implementation for Phase 2. Per ARCHITECTURE.md §3.4 and UX_SPECIFICATION.md §5.1.
// Shown on first launch (and version bumps). Blocks navigation until import completes.

package com.mandarinlearn.ui.importing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * ImportLoadingScreen — shown while JSON data is imported into Room on first launch.
 * Navigation is blocked until import completes; [onImportComplete] fires on success.
 *
 * Per ARCHITECTURE.md §3.4:
 * - App logo placeholder text (24 sp)
 * - "Setting up your lessons…" header
 * - Indeterminate → determinate LinearProgressIndicator
 * - Status text (18 sp)
 * - Retry button on error
 *
 * @param onImportComplete Called when import finishes. Navigates to HomeScreen.
 * @param viewModel        Injected by ViewModelProvider with factory from AppContainer.
 * @param modifier         Optional modifier.
 */
@Composable
fun ImportLoadingScreen(
    onImportComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportLoadingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate away as soon as the import is done — no user action needed
    LaunchedEffect(uiState) {
        if (uiState is ImportLoadingUiState.Done) {
            onImportComplete()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // App identity — Phase 10 will replace with a real logo vector
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.import_setting_up),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress indicator: indeterminate until we have a fraction, then determinate
            val progressDescription = stringResource(R.string.content_desc_loading)
            when (val state = uiState) {
                is ImportLoadingUiState.Idle,
                is ImportLoadingUiState.Done -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = progressDescription },
                    )
                }

                is ImportLoadingUiState.Progress -> {
                    LinearProgressIndicator(
                        progress = { state.fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = progressDescription },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }

                is ImportLoadingUiState.Error -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = progressDescription },
                        color = MaterialTheme.colorScheme.error,
                        trackColor = MaterialTheme.colorScheme.errorContainer,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    MandarinPrimaryButton(
                        text = stringResource(R.string.action_retry),
                        onClick = { viewModel.retry() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportLoadingScreenProgressPreview() {
    MandarinLearnTheme {
        // Preview the progress state directly without ViewModel
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.import_setting_up), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.import_importing_vocabulary), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
