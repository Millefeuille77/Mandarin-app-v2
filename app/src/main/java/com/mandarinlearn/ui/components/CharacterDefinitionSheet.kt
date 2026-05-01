// CharacterDefinitionSheet.kt — Mandarin Learn
// Bottom sheet showing full definition for a tapped vocabulary word.
// Used in VocabularyScreen (word row tap) and PassageScreen (Phase 4, character tap).
// UX_SPECIFICATION.md §4 Screen 2 (detail) and §4 Screen 5 (character popup).

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.model.SrsStatus
import com.mandarinlearn.ui.theme.HanziDisplayStyle
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PinyinLargeStyle
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Modal bottom sheet displaying the full definition of a vocabulary word.
 *
 * @param word        The word to display. If null, the sheet is dismissed.
 * @param onDismiss   Called when user closes the sheet.
 * @param onPlayAudio Called when user taps the audio play button.
 * @param modifier    Optional modifier for the sheet content column.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDefinitionSheet(
    word: VocabularyWord?,
    onDismiss: () -> Unit,
    onPlayAudio: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (word == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        CharacterDefinitionContent(
            word        = word,
            onPlayAudio = onPlayAudio,
            modifier    = modifier.padding(horizontal = SpacingM, vertical = SpacingS),
        )
        Spacer(Modifier.height(SpacingM))
    }
}

@Composable
private fun CharacterDefinitionContent(
    word: VocabularyWord,
    onPlayAudio: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // --- Header: hanzi + audio button ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text     = word.character,
                style    = HanziDisplayStyle,
                color    = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
            val playDesc = stringResource(R.string.content_desc_play_audio)
            IconButton(
                onClick  = { onPlayAudio(word.character) },
                modifier = Modifier
                    .size(MinTouchTarget)
                    .semantics { contentDescription = playDesc },
            ) {
                Icon(
                    imageVector        = Icons.Filled.VolumeUp,
                    contentDescription = null, // parent carries description
                    tint               = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // --- Pinyin ---
        Text(
            text  = word.pinyin,
            style = PinyinLargeStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(SpacingXs))

        // --- Part of speech + translation ---
        Text(
            text  = word.partOfSpeech,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = word.translation,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(SpacingS))
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(Modifier.height(SpacingS))

        // --- Example sentence ---
        Text(
            text  = stringResource(R.string.label_example_sentence),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = word.exampleChinese,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text  = word.examplePinyin,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text  = word.exampleEnglish,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDefinitionContentPreview() {
    MandarinLearnTheme {
        CharacterDefinitionContent(
            word = VocabularyWord(
                id              = "hsk1_001",
                hskLevel        = 1,
                character       = "你好",
                pinyin          = "nǐ hǎo",
                translation     = "Hello",
                partOfSpeech    = "interjection",
                exampleChinese  = "你好，我是学生。",
                examplePinyin   = "Nǐ hǎo, wǒ shì xuéshēng.",
                exampleEnglish  = "Hello, I am a student.",
                easeFactor      = 2.5,
                intervalDays    = 0,
                repetitionCount = 0,
                nextReviewDate  = 0L,
                lastReviewedDate = null,
                isIntroduced    = false,
            ),
            onPlayAudio = {},
        )
    }
}
