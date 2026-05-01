// HskLevelChip.kt — Mandarin Learn
// Individual HSK-level selector chip per UX_SPECIFICATION.md §3.5.
// 80 dp × 56 dp pill, primary when selected, surfaceVariant when unselected.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * HSK level selector chip (e.g. "HSK 1").
 *
 * @param level HSK level 1–5.
 * @param selected Whether this chip is currently selected.
 * @param onSelected Called when the chip is tapped.
 * @param modifier Optional modifier.
 */
@Composable
fun HskLevelChip(
    level: Int,
    selected: Boolean,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick  = { onSelected(level) },
        label    = {
            Text(
                text  = stringResource(R.string.hsk_level_label, level),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        shape    = MaterialTheme.shapes.extraLarge,  // 24 dp pill radius
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor   = MaterialTheme.colorScheme.primary,
            selectedLabelColor       = MaterialTheme.colorScheme.onPrimary,
            containerColor           = MaterialTheme.colorScheme.surfaceVariant,
            labelColor               = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            // 80 dp × 56 dp per UX spec §3.5.
            .defaultMinSize(minWidth = 80.dp, minHeight = 56.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun HskLevelChipSelectedPreview() {
    MandarinLearnTheme {
        HskLevelChip(level = 1, selected = true, onSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HskLevelChipUnselectedPreview() {
    MandarinLearnTheme {
        HskLevelChip(level = 2, selected = false, onSelected = {})
    }
}
