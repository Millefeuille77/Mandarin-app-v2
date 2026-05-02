// SettingsScreen.kt — Mandarin Learn
// Full Phase 9 implementation of SettingsScreen.
// UX_SPECIFICATION.md §4 Screen 11: theme, font, audio, pinyin, daily limit, export, import, reset.
// Sub-composables extracted to SettingsComponents.kt to honour the 300-line file rule.

package com.mandarinlearn.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.ConfirmDialog
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingXl
import com.mandarinlearn.util.FileExportHelper
import java.time.LocalDate

/**
 * SettingsScreen — app preferences and data management.
 * Full Phase 9 implementation per UX_SPECIFICATION.md §4 Screen 11.
 *
 * @param viewModel      Injected [SettingsViewModel].
 * @param onNavigateBack Pop this screen off the back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ---- Snackbar message strings ----
    val snackExportOk    = stringResource(R.string.settings_export_success)
    val snackExportFail  = stringResource(R.string.settings_export_failed)
    val snackImportOk    = stringResource(R.string.settings_import_success)
    val snackImportFail  = stringResource(R.string.settings_import_failed)
    val snackResetOk     = stringResource(R.string.settings_reset_complete)
    val snackResetFail   = stringResource(R.string.settings_reset_failed)

    // ---- One-time events ----
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowSnackbar -> {
                    val msg = when (event.key) {
                        SettingsEvent.SnackbarKey.EXPORT_SUCCESS -> snackExportOk
                        SettingsEvent.SnackbarKey.EXPORT_FAILED  -> snackExportFail
                        SettingsEvent.SnackbarKey.IMPORT_SUCCESS -> snackImportOk
                        SettingsEvent.SnackbarKey.IMPORT_FAILED  -> snackImportFail
                        SettingsEvent.SnackbarKey.RESET_COMPLETE -> snackResetOk
                        SettingsEvent.SnackbarKey.RESET_FAILED   -> snackResetFail
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    // ---- SAF launchers ----
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.data
        if (FileExportHelper.isValidUri(uri)) {
            uri?.let { viewModel.exportProgress(it) }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.data
        if (FileExportHelper.isValidUri(uri)) {
            uri?.let { viewModel.importProgress(it) }
        }
    }

    // ---- Confirm dialog state ----
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri: Uri? by remember { mutableStateOf(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Re-launch import SAF, then show confirm after URI selected
    val importAfterConfirmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri: Uri? = result.data?.data
        if (FileExportHelper.isValidUri(uri)) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_settings),
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier     = modifier,
    ) { innerPadding ->
        when (val state = uiState) {
            is SettingsUiState.Loading -> LoadingState(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            is SettingsUiState.Content -> SettingsContent(
                state            = state,
                innerPadding     = innerPadding,
                onSetTheme       = viewModel::setTheme,
                onSetFontIndex   = viewModel::setFontScaleIndex,
                onSetReduceMotion = viewModel::setReduceMotion,
                onSetAudioIndex  = viewModel::setAudioSpeedIndex,
                onSetShowPinyin  = viewModel::setShowPinyinDefault,
                onSetDailyLimit  = viewModel::setDailyNewCardsLimit,
                onExport         = {
                    val today = LocalDate.now().toString()
                    exportLauncher.launch(FileExportHelper.createDocumentIntent(today))
                },
                onImport         = {
                    importAfterConfirmLauncher.launch(FileExportHelper.openDocumentIntent())
                },
                onResetRequest   = { showResetConfirm = true },
            )
        }
    }

    // ---- Import confirmation dialog ----
    if (showImportConfirm) {
        ConfirmDialog(
            title     = stringResource(R.string.settings_import_confirm_title),
            message   = stringResource(R.string.settings_import_confirm_message),
            confirmText = stringResource(R.string.settings_import_confirm_action),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                showImportConfirm = false
                pendingImportUri?.let { viewModel.importProgress(it) }
                pendingImportUri = null
            },
            onDismiss = {
                showImportConfirm = false
                pendingImportUri = null
            },
        )
    }

    // ---- Reset confirmation dialog ----
    if (showResetConfirm) {
        ConfirmDialog(
            title     = stringResource(R.string.settings_reset_confirm_title),
            message   = stringResource(R.string.settings_reset_confirm_message),
            confirmText = stringResource(R.string.settings_reset_confirm_action),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                showResetConfirm = false
                viewModel.resetAllProgress()
            },
            onDismiss = { showResetConfirm = false },
        )
    }
}

/**
 * Legacy overload used when no ViewModel is available (preview / navigation fallback).
 * Displays only the top bar and title — never shown in production.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_settings),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}

// ---- Content layout ----

@Composable
internal fun SettingsContent(
    state: SettingsUiState.Content,
    innerPadding: PaddingValues,
    onSetTheme: (String) -> Unit,
    onSetFontIndex: (Int) -> Unit,
    onSetReduceMotion: (Boolean) -> Unit,
    onSetAudioIndex: (Int) -> Unit,
    onSetShowPinyin: (Boolean) -> Unit,
    onSetDailyLimit: (Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onResetRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier        = modifier.fillMaxSize().padding(innerPadding),
        contentPadding  = PaddingValues(bottom = SpacingXl),
    ) {
        displaySection(state, onSetTheme, onSetFontIndex, onSetReduceMotion)
        audioSection(state, onSetAudioIndex, onSetShowPinyin)
        learningSection(state, onSetDailyLimit)
        dataSection(state, onExport, onImport, onResetRequest)
        aboutSection(state)
    }
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MandarinLearnTheme {
        SettingsContent(
            state = SettingsUiState.Content(
                appVersion   = "1.0.0",
                geminiKeySet = true,
            ),
            innerPadding     = PaddingValues(),
            onSetTheme       = {},
            onSetFontIndex   = {},
            onSetReduceMotion = {},
            onSetAudioIndex  = {},
            onSetShowPinyin  = {},
            onSetDailyLimit  = {},
            onExport         = {},
            onImport         = {},
            onResetRequest   = {},
        )
    }
}
