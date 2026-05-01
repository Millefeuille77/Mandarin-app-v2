// MandarinPrimaryButton.kt — Mandarin Learn
// Primary button component per UX_SPECIFICATION.md §3.3.
// 56 dp height, full-width default, primary background, label-large text.
// Loading state replaces label with CircularProgressIndicator (no size change).

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.MinTouchTarget

/**
 * Primary action button used throughout the app.
 *
 * @param text Button label text (must come from stringResource — not a raw literal).
 * @param onClick Click callback.
 * @param modifier Modifier applied to the Button. Defaults to full-width.
 * @param enabled Whether the button is interactive. Disabled state has alpha 0.38 via M3.
 * @param isLoading When true, replaces the label with a spinner (button stays same size).
 */
@Composable
fun MandarinPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick  = onClick,
        enabled  = enabled && !isLoading,
        shape    = MaterialTheme.shapes.small,   // 8 dp radius (UX spec §1.4)
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTouchTarget),   // 56 dp minimum (CLAUDE.md rule)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                // Spinner replaces label — button retains its dimensions.
                CircularProgressIndicator(
                    modifier  = Modifier.size(24.dp),
                    color     = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text  = text,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MandarinPrimaryButtonPreview() {
    MandarinLearnTheme {
        MandarinPrimaryButton(text = "Start today's review (12)", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MandarinPrimaryButtonLoadingPreview() {
    MandarinLearnTheme {
        MandarinPrimaryButton(text = "Loading…", onClick = {}, isLoading = true)
    }
}
