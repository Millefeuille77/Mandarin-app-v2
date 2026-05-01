// HskLevelChipRow.kt — Mandarin Learn
// Horizontally-scrolling row of 5 HskLevelChips with state hoisting.
// Used by VocabularyScreen, ReadingListScreen, SpeakingScreen, ListeningScreen.
// UX_SPECIFICATION.md §4 Screen 2: "horizontally-scrolling row of 5 HskLevelChips".

package com.mandarinlearn.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * A horizontally-scrollable row of HSK 1–5 chip selectors.
 *
 * @param selectedLevel The currently active HSK level (1–5).
 * @param onLevelSelected Called when the user taps a chip.
 * @param modifier Optional modifier for the row container.
 */
@Composable
fun HskLevelChipRow(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        // HSK levels 1–5 (immutable range — no level 6 in scope per CLAUDE.md)
        (1..5).forEach { level ->
            HskLevelChip(
                level      = level,
                selected   = level == selectedLevel,
                onSelected = onLevelSelected,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HskLevelChipRowPreview() {
    MandarinLearnTheme {
        HskLevelChipRow(selectedLevel = 2, onLevelSelected = {})
    }
}
