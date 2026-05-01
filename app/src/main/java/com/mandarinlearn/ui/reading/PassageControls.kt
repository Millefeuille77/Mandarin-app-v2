// PassageControls.kt — Mandarin Learn
// Sticky controls bar and translation card extracted from PassageScreen.kt per 300-line rule.
// UX_SPECIFICATION.md §4 Screen 5: pinyin toggle, font-size slider, "Play all" button,
// and collapsible English translation card.

package com.mandarinlearn.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Sticky controls bar displayed below the top bar in PassageScreen.
 * Contains: Pinyin toggle switch, font-size slider (A− | slider | A+), Play all button.
 * Height is exactly [MinTouchTarget] = 56 dp per UX spec §1.5.
 *
 * Phase 5: [onPlayAll] now wired to real AudioRepository via PassageViewModel.
 * Per QA m-3 (Phase 4 report): was a silent no-op; now calls [onPlayAll] which triggers
 * audio playback and shows a Snackbar on failure.
 *
 * @param showPinyin     Current pinyin visibility state.
 * @param fontScale      Current font scale (0.8–1.6).
 * @param isPlayingAll   True while Play All audio is loading/playing (shows spinner).
 * @param onTogglePinyin Called when user toggles the Pinyin switch.
 * @param onFontScale    Called when the slider value changes.
 * @param onPlayAll      Called when user taps the Play All button.
 * @param modifier       Optional modifier.
 */
@Composable
fun PassageControlsBar(
    showPinyin: Boolean,
    fontScale: Float,
    isPlayingAll: Boolean = false,
    onTogglePinyin: () -> Unit,
    onFontScale: (Float) -> Unit,
    onPlayAll: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val pinyinDesc = if (showPinyin) {
        stringResource(R.string.reading_pinyin_shown)
    } else {
        stringResource(R.string.reading_pinyin_hidden)
    }
    val fontScaleDesc = stringResource(R.string.reading_font_scale_desc, fontScale)
    val playDesc = stringResource(R.string.content_desc_play_audio)

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .height(MinTouchTarget)
            .padding(horizontal = PagePaddingH),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pinyin label + switch
        Text(
            text  = stringResource(R.string.reading_pinyin_toggle_label),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.width(SpacingXs))
        Switch(
            checked         = showPinyin,
            onCheckedChange = { onTogglePinyin() },
            modifier        = Modifier.semantics { stateDescription = pinyinDesc },
        )
        Spacer(Modifier.weight(1f))

        // Font size A− / slider / A+
        Icon(
            imageVector        = Icons.Filled.TextDecrease,
            contentDescription = stringResource(R.string.reading_font_decrease),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp),
        )
        Slider(
            value         = fontScale,
            onValueChange = onFontScale,
            valueRange    = 0.8f..1.6f,
            steps         = 7,
            modifier      = Modifier
                .width(90.dp)
                .semantics { stateDescription = fontScaleDesc },
        )
        Icon(
            imageVector        = Icons.Filled.TextIncrease,
            contentDescription = stringResource(R.string.reading_font_increase),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp),
        )

        // Play all — wired to AudioRepository in Phase 5 (was silent stub in Phase 4)
        IconButton(
            onClick  = onPlayAll,
            enabled  = !isPlayingAll,
            modifier = Modifier
                .size(MinTouchTarget)
                .semantics { contentDescription = playDesc },
        ) {
            if (isPlayingAll) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector        = Icons.Filled.VolumeUp,
                    contentDescription = null, // parent carries description
                    tint               = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Collapsible card showing the English translation of the passage.
 * Tapping the header toggles expansion with a fade animation.
 *
 * @param translation The full English translation string.
 * @param isExpanded  Whether the translation body is currently visible.
 * @param onToggle    Called when the user taps the header.
 * @param modifier    Optional modifier.
 */
@Composable
fun PassageTranslationCard(
    translation: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(SpacingS)) {
            TextButton(
                onClick  = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MinTouchTarget),
            ) {
                Text(
                    text  = if (isExpanded) {
                        stringResource(R.string.reading_hide_translation)
                    } else {
                        stringResource(R.string.reading_show_translation)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter   = fadeIn(),
                exit    = fadeOut(),
            ) {
                Text(
                    text     = translation,
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = SpacingXs, vertical = SpacingXs),
                )
            }
        }
    }
}
