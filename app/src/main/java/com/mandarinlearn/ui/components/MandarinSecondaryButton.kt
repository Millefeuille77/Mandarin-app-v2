// MandarinSecondaryButton.kt — Mandarin Learn
// Secondary button component per UX_SPECIFICATION.md §3.4.
// Same dimensions as primary (56 dp height, full-width), surfaceVariant background.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.MinTouchTarget

/**
 * Secondary action button used for less prominent actions (e.g. "Try again", "Cancel").
 *
 * @param text Button label text (must come from stringResource).
 * @param onClick Click callback.
 * @param modifier Modifier applied to the Button. Defaults to full-width.
 * @param enabled Whether the button is interactive.
 */
@Composable
fun MandarinSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = MaterialTheme.shapes.small,  // 8 dp radius
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTouchTarget),  // 56 dp minimum
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MandarinSecondaryButtonPreview() {
    MandarinLearnTheme {
        MandarinSecondaryButton(text = "Try again", onClick = {})
    }
}
