// SettingsDataComponents.kt — Mandarin Learn
// Learning, data, and about section composables for SettingsScreen.
// Phase 9: Settings & Polish. UX_SPECIFICATION.md §4 Screen 11.
// Display / audio sections are in SettingsComponents.kt (300-line split).

package com.mandarinlearn.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs
import kotlin.math.roundToInt

// ---- Learning section ----

internal fun LazyListScope.learningSection(
    state: SettingsUiState.Content,
    onSetDailyLimit: (Int) -> Unit,
) {
    sectionHeader(R.string.settings_section_learning)
    item { DailyLimitSliderRow(state.dailyNewCardsLimit, onSetDailyLimit) }
}

@Composable
private fun DailyLimitSliderRow(limit: Int, onSetDailyLimit: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePaddingH, vertical = SpacingXs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text     = stringResource(R.string.settings_daily_limit_label),
                style    = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text  = stringResource(R.string.settings_daily_limit_value, limit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value         = limit.toFloat(),
            onValueChange = { onSetDailyLimit(it.roundToInt()) },
            valueRange    = 5f..50f,
            steps         = 44, // integer steps from 5 to 50
            modifier      = Modifier
                .fillMaxWidth()
                .semantics { stateDescription = "$limit new cards per day" },
        )
    }
}

// ---- Data section ----

internal fun LazyListScope.dataSection(
    state: SettingsUiState.Content,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onResetRequest: () -> Unit,
) {
    sectionHeader(R.string.settings_section_data)
    item {
        val buttonsEnabled = !state.isExporting && !state.isImporting && !state.isResetting
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePaddingH),
            verticalArrangement = Arrangement.spacedBy(SpacingM),
        ) {
            MandarinSecondaryButton(
                text     = stringResource(R.string.settings_export_button),
                onClick  = onExport,
                enabled  = buttonsEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            MandarinSecondaryButton(
                text     = stringResource(R.string.settings_import_button),
                onClick  = onImport,
                enabled  = buttonsEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            // Danger-zone button: error-tinted text, surfaceVariant background.
            // Color alone does not convey severity — the label text does too.
            DangerResetButton(
                onClick  = onResetRequest,
                enabled  = buttonsEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(SpacingXs))
        }
    }
}

@Composable
private fun DangerResetButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = MaterialTheme.shapes.small,
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor   = MaterialTheme.colorScheme.error,
        ),
        modifier = modifier.defaultMinSize(minHeight = MinTouchTarget),
    ) {
        Text(
            text  = stringResource(R.string.settings_reset_button),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

// ---- About section ----

internal fun LazyListScope.aboutSection(state: SettingsUiState.Content) {
    sectionHeader(R.string.settings_section_about)
    item {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = PagePaddingH, vertical = SpacingXs),
            verticalArrangement = Arrangement.spacedBy(SpacingS),
        ) {
            ReadOnlySettingRow(
                label = stringResource(R.string.settings_app_version_label),
                value = stringResource(R.string.settings_app_version_value, state.appVersion),
            )
            val keyStatus = if (state.geminiKeySet) {
                stringResource(R.string.settings_gemini_key_set)
            } else {
                stringResource(R.string.settings_gemini_key_not_set)
            }
            ReadOnlySettingRow(
                label      = stringResource(R.string.settings_gemini_key_label),
                value      = keyStatus,
                helperText = stringResource(R.string.settings_gemini_key_helper),
            )
        }
    }
}

@Composable
internal fun ReadOnlySettingRow(
    label: String,
    value: String,
    helperText: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (helperText != null) {
            Text(
                text  = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
