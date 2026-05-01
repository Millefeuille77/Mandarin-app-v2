// LoadingState.kt — Mandarin Learn
// Full-screen loading indicator per UX_SPECIFICATION.md §3.7.
// Centered CircularProgressIndicator (48 dp) + optional caption text.

package com.mandarinlearn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.SpacingM

/**
 * Full-screen loading state per UX spec §3.7.
 *
 * @param message Optional caption text shown below the spinner.
 * @param modifier Optional modifier for the container.
 */
@Composable
fun LoadingState(
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color    = MaterialTheme.colorScheme.primary,
        )
        if (message != null) {
            Spacer(modifier = Modifier.height(SpacingM))
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    MandarinLearnTheme {
        LoadingState(message = "Loading your words…")
    }
}
