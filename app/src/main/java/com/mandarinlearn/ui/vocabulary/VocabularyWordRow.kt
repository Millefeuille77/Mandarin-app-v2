// VocabularyWordRow.kt — Mandarin Learn
// Word list row and SRS status badge for VocabularyScreen.
// Split from VocabularyScreen.kt to keep both files under 300 lines.

package com.mandarinlearn.ui.vocabulary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.SrsStatus
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.ui.theme.HanziLargeStyle
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.PinyinLargeStyle
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs
import com.mandarinlearn.ui.theme.onSuccessColor
import com.mandarinlearn.ui.theme.successColor

/** Single word row in the vocabulary list — 88 dp min height, tappable. */
@Composable
fun VocabularyWordRow(
    word: VocabularyWord,
    onTap: (VocabularyWord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (word.srsStatus) {
        SrsStatus.NEW      -> stringResource(R.string.srs_status_new)
        SrsStatus.LEARNING -> stringResource(R.string.srs_status_learning)
        SrsStatus.MASTERED -> stringResource(R.string.srs_status_mastered)
    }
    val rowDesc = stringResource(
        R.string.vocab_row_content_desc,
        word.character, word.pinyin, word.translation, statusLabel,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 88.dp)
            .clickable { onTap(word) }
            .padding(horizontal = PagePaddingH, vertical = SpacingS)
            .semantics { contentDescription = rowDesc },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = word.character,
                style    = HanziLargeStyle,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text  = word.pinyin,
                style = PinyinLargeStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text     = word.translation,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(SpacingM))
        SrsBadge(status = word.srsStatus)
    }
}

/** Small colored badge showing New / Learning / Mastered. Color paired with text label. */
@Composable
fun SrsBadge(status: SrsStatus, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (status) {
        SrsStatus.NEW      -> MaterialTheme.colorScheme.surfaceVariant to
                              MaterialTheme.colorScheme.onSurfaceVariant
        SrsStatus.LEARNING -> MaterialTheme.colorScheme.primary to
                              MaterialTheme.colorScheme.onPrimary
        SrsStatus.MASTERED -> successColor() to onSuccessColor()
    }
    val label = when (status) {
        SrsStatus.NEW      -> stringResource(R.string.srs_status_new)
        SrsStatus.LEARNING -> stringResource(R.string.srs_status_learning)
        SrsStatus.MASTERED -> stringResource(R.string.srs_status_mastered)
    }
    Surface(
        color    = bg,
        shape    = MaterialTheme.shapes.extraSmall,
        modifier = modifier,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = textColor,
            modifier = Modifier.padding(horizontal = SpacingXs, vertical = 2.dp),
        )
    }
}
