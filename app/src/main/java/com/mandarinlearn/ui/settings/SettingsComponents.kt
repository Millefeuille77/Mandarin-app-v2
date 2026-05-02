// SettingsComponents.kt — Mandarin Learn
// Display and audio section composables for SettingsScreen.
// Phase 9: Settings & Polish. UX_SPECIFICATION.md §4 Screen 11.
// Learning / Data / About sections are in SettingsDataComponents.kt (300-line split).

package com.mandarinlearn.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs
import kotlin.math.roundToInt

// ---- Section header ----

// Takes @StringRes Int (not String) so callers don't need to be @Composable themselves —
// stringResource() can only be called inside a composable scope, and the *Section() helpers
// that call sectionHeader() are LazyListScope extensions, not composables.
internal fun LazyListScope.sectionHeader(@androidx.annotation.StringRes titleRes: Int) {
    item {
        Column {
            HorizontalDivider(modifier = Modifier.padding(horizontal = PagePaddingH))
            Text(
                text     = stringResource(titleRes),
                style    = MaterialTheme.typography.headlineSmall,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingS),
            )
        }
    }
}

// ---- Display section ----

internal fun LazyListScope.displaySection(
    state: SettingsUiState.Content,
    onSetTheme: (String) -> Unit,
    onSetFontIndex: (Int) -> Unit,
    onSetReduceMotion: (Boolean) -> Unit,
) {
    sectionHeader(R.string.settings_section_display)
    item { ThemeSegmentedRow(state.theme, onSetTheme) }
    item { FontSliderRow(state.fontScaleIndex, onSetFontIndex) }
    item {
        SettingsSwitchRow(
            label       = stringResource(R.string.settings_reduce_motion),
            checked     = state.reduceMotion,
            onChecked   = onSetReduceMotion,
            contentDesc = stringResource(R.string.settings_reduce_motion_desc),
        )
    }
}

@Composable
private fun ThemeSegmentedRow(theme: String, onSetTheme: (String) -> Unit) {
    val options = listOf(
        "system" to stringResource(R.string.settings_theme_system),
        "light"  to stringResource(R.string.settings_theme_light),
        "dark"   to stringResource(R.string.settings_theme_dark),
    )
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePaddingH, vertical = SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(SpacingS),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text     = stringResource(R.string.settings_theme_label),
            style    = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        options.forEach { (key, label) ->
            if (theme == key) {
                MandarinPrimaryButton(text = label, onClick = { onSetTheme(key) },
                    modifier = Modifier.weight(1f))
            } else {
                MandarinSecondaryButton(text = label, onClick = { onSetTheme(key) },
                    modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FontSliderRow(fontIndex: Int, onSetFontIndex: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.settings_font_small),
        stringResource(R.string.settings_font_medium),
        stringResource(R.string.settings_font_large),
        stringResource(R.string.settings_font_xl),
    )
    val currentLabel = labels.getOrElse(fontIndex) { labels[1] }
    val sampleSpSize = fontScaleForIndex(fontIndex) * 18f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePaddingH, vertical = SpacingXs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.settings_font_size_label),
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(text = currentLabel, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value         = fontIndex.toFloat(),
            onValueChange = { onSetFontIndex(it.roundToInt()) },
            valueRange    = 0f..3f,
            steps         = 2,
            modifier      = Modifier.fillMaxWidth().semantics { stateDescription = currentLabel },
        )
        Text(
            text  = stringResource(R.string.settings_font_preview),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = TextUnit(sampleSpSize, TextUnitType.Sp)
            ),
        )
        Spacer(modifier = Modifier.height(SpacingXs))
    }
}

// ---- Audio section ----

internal fun LazyListScope.audioSection(
    state: SettingsUiState.Content,
    onSetAudioIndex: (Int) -> Unit,
    onSetShowPinyin: (Boolean) -> Unit,
) {
    sectionHeader(R.string.settings_section_audio)
    item { AudioSpeedRow(state.audioSpeedIndex, onSetAudioIndex) }
    item {
        SettingsSwitchRow(
            label       = stringResource(R.string.settings_show_pinyin_label),
            helperText  = stringResource(R.string.settings_show_pinyin_helper),
            checked     = state.showPinyinDefault,
            onChecked   = onSetShowPinyin,
            contentDesc = stringResource(R.string.settings_show_pinyin_desc),
        )
    }
}

@Composable
private fun AudioSpeedRow(speedIndex: Int, onSetAudioIndex: (Int) -> Unit) {
    val labels = listOf("0.5×", "0.75×", "1.0×", "1.25×")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PagePaddingH, vertical = SpacingXs),
    ) {
        Text(text = stringResource(R.string.settings_audio_speed_label),
            style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(SpacingXs))
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingS)) {
            labels.forEachIndexed { index, label ->
                val desc = if (index == speedIndex) "$label audio speed, selected" else "$label audio speed"
                if (index == speedIndex) {
                    MandarinPrimaryButton(text = label, onClick = { onSetAudioIndex(index) },
                        modifier = Modifier.weight(1f).semantics { contentDescription = desc })
                } else {
                    MandarinSecondaryButton(text = label, onClick = { onSetAudioIndex(index) },
                        modifier = Modifier.weight(1f).semantics { contentDescription = desc })
                }
            }
        }
    }
}

// ---- Shared switch row (used by display + audio sections) ----

@Composable
internal fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    contentDesc: String,
    helperText: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = PagePaddingH, vertical = SpacingXs)
            .semantics(mergeDescendants = true) { contentDescription = contentDesc },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (helperText != null) {
                Text(text = helperText, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            modifier        = Modifier.semantics {
                stateDescription = if (checked) "On" else "Off"
            },
        )
    }
}
