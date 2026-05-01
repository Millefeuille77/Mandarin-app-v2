// NoDefinitionSheet.kt — Mandarin Learn
// Bottom sheet displayed when a tapped character is not found in HSK 1–5 vocabulary.
// UX_SPECIFICATION.md §4 Screen 5 character popup — "not found" branch.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.HanziDisplayStyle
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Bottom sheet shown when a tapped character is not present in the HSK 1–5 vocabulary table.
 * UX spec §4 Screen 5: "Word not in HSK 1–5 vocabulary list."
 *
 * @param character The hanzi character that was tapped.
 * @param onDismiss Called when the user closes the sheet.
 * @param modifier  Optional modifier for the sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoDefinitionSheet(
    character: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        NoDefinitionContent(
            character = character,
            modifier  = modifier.padding(horizontal = SpacingM, vertical = SpacingS),
        )
        Spacer(Modifier.height(SpacingM))
    }
}

@Composable
private fun NoDefinitionContent(
    character: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text  = character,
            style = HanziDisplayStyle,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(SpacingXs))
        Text(
            text  = stringResource(R.string.reading_no_definition),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoDefinitionSheetPreview() {
    MandarinLearnTheme {
        NoDefinitionContent(character = "啊")
    }
}
