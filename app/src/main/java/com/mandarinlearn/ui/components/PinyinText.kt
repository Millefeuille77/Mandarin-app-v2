// PinyinText.kt — Mandarin Learn
// Ruby-style pinyin-above-hanzi composable.
// Implements UX_SPECIFICATION.md §3.6 (PinyinText) and Phase 4 acceptance criteria.
// FlowRow requires Compose BOM 2024.02 (already in gradle/libs.versions.toml).

package com.mandarinlearn.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.PinyinAnnotation
import com.mandarinlearn.ui.theme.HanziLargeStyle
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PinyinStyle

/**
 * Renders a sequence of Chinese characters with optional ruby-style pinyin above each.
 *
 * Layout: a [FlowRow] of stacked [Column]s — each column holds pinyin (top) and hanzi (bottom).
 * When [showPinyin] is false the pinyin row is hidden but space can optionally be collapsed.
 * Punctuation characters (pinyin == "") are rendered without pinyin and are not tappable.
 *
 * Accessibility: each tappable character cell announces its pinyin + hanzi for TalkBack.
 * Punctuation cells have an empty content description so TalkBack skips them.
 *
 * @param annotations      Per-character list of (character, pinyin) pairs.
 * @param showPinyin       Whether the pinyin row is visible.
 * @param fontScale        Multiplier applied on top of theme sp values (0.8–1.6).
 * @param onCharacterClick Called with the character string when a tappable cell is tapped.
 * @param modifier         Optional outer modifier.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PinyinText(
    annotations: List<AnnotatedCharacter>,
    showPinyin: Boolean,
    fontScale: Float,
    onCharacterClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier             = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalArrangement  = Arrangement.Top,
    ) {
        annotations.forEach { annotatedChar ->
            PinyinCharCell(
                annotatedChar    = annotatedChar,
                showPinyin       = showPinyin,
                fontScale        = fontScale,
                onCharacterClick = onCharacterClick,
            )
        }
    }
}

/**
 * A single character cell: pinyin on top (conditional), hanzi on bottom.
 * Min size ensures 56dp touch target for tappable characters (UX spec §1.5).
 */
@Composable
private fun PinyinCharCell(
    annotatedChar: AnnotatedCharacter,
    showPinyin: Boolean,
    fontScale: Float,
    onCharacterClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val character = annotatedChar.annotation.character
    val pinyin    = annotatedChar.annotation.pinyin
    val isTappable = annotatedChar.isTappable

    // Build accessibility description
    val cellDesc = if (isTappable) "$character — $pinyin" else ""

    val cellModifier = modifier
        // Minimum touch target for tappable cells (UX spec §1.5 HARD rule)
        .defaultMinSize(minWidth = 48.dp, minHeight = 56.dp)
        .semantics(mergeDescendants = false) {
            contentDescription = cellDesc
        }
        .then(
            if (isTappable) {
                Modifier.clickable(
                    onClickLabel = character,
                    onClick      = { onCharacterClick(character) },
                )
            } else Modifier
        )

    Column(
        modifier            = cellModifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        // Pinyin row — always occupies space when showPinyin is true; hidden when false.
        // For punctuation (isTappable=false, pinyin="") we still reserve the row height
        // so columns stay aligned in the FlowRow.
        if (showPinyin) {
            Text(
                text      = pinyin, // empty string for punctuation → invisible but preserves height
                style     = PinyinStyle.copy(fontSize = (PinyinStyle.fontSize.value * fontScale).sp),
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 1,
            )
        }
        // Hanzi
        Text(
            text      = character,
            style     = HanziLargeStyle.copy(fontSize = (HanziLargeStyle.fontSize.value * fontScale).sp),
            color     = if (annotatedChar.isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            textAlign = TextAlign.Center,
            maxLines  = 1,
        )
    }
}

// ---- Previews ----------------------------------------------------------------

@Preview(showBackground = true, name = "PinyinText — HSK 1 with pinyin")
@Composable
private fun PinyinTextWithPinyinPreview() {
    MandarinLearnTheme {
        PinyinText(
            annotations = sampleAnnotations().toAnnotatedCharacters(),
            showPinyin  = true,
            fontScale   = 1.0f,
            onCharacterClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true, name = "PinyinText — pinyin hidden")
@Composable
private fun PinyinTextNoPinyinPreview() {
    MandarinLearnTheme {
        PinyinText(
            annotations = sampleAnnotations().toAnnotatedCharacters(),
            showPinyin  = false,
            fontScale   = 1.0f,
            onCharacterClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

private fun sampleAnnotations() = listOf(
    PinyinAnnotation("你", "nǐ"),
    PinyinAnnotation("好", "hǎo"),
    PinyinAnnotation("，", ""),
    PinyinAnnotation("我", "wǒ"),
    PinyinAnnotation("是", "shì"),
    PinyinAnnotation("学", "xué"),
    PinyinAnnotation("生", "shēng"),
    PinyinAnnotation("。", ""),
)
