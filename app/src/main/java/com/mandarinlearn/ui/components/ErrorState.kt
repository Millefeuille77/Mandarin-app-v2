// ErrorState.kt — Mandarin Learn
// Error-state composable per UX_SPECIFICATION.md §3.9.
// Centered error icon (56 dp), "Something went wrong" title, body, Retry button.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * Error-state composable per UX spec §3.9.
 *
 * @param message User-friendly error message (body-large). Do NOT show stack traces.
 * @param onRetry Called when the user taps "Retry". If null, no retry button is shown.
 * @param modifier Optional modifier.
 */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier              = modifier
            .fillMaxSize()
            .padding(horizontal = PagePaddingH),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center,
    ) {
        Icon(
            imageVector        = Icons.Filled.ErrorOutline,
            contentDescription = stringResource(R.string.content_desc_error_icon),
            tint               = MaterialTheme.colorScheme.error,
            modifier           = Modifier.size(56.dp),   // UX spec §3.9
        )
        Spacer(modifier = Modifier.height(SpacingM))
        Text(
            text      = stringResource(R.string.error_title_something_went_wrong),
            style     = MaterialTheme.typography.headlineMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SpacingXs))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(SpacingM))
            MandarinPrimaryButton(
                text    = stringResource(R.string.action_retry),
                onClick = onRetry,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    MandarinLearnTheme {
        ErrorState(
            message = "Could not load your words. Please try again.",
            onRetry = {},
        )
    }
}
