// SpeakingScreen.kt — Mandarin Learn
// Placeholder for SpeakingScreen.
// UX_SPECIFICATION.md §4 Screen 6: record → Gemini STT → pronunciation score.
// Full implementation in Phase 6 (Speaking Section).

package com.mandarinlearn.ui.speaking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mandarinlearn.R
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * SpeakingScreen — record pronunciation and get Gemini-powered feedback.
 * Placeholder: displays screen title. Full implementation in Phase 6.
 *
 * @param hsk            The HSK level for practice phrases.
 * @param onNavigateBack Pop this screen off the back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun SpeakingScreen(
    hsk: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO(phase_6): Replace with full SpeakingScreen layout per UX spec §4 Screen 6.
    Column(
        modifier              = modifier.fillMaxSize(),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Text(
            text  = stringResource(R.string.screen_speaking),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeakingScreenPreview() {
    MandarinLearnTheme {
        SpeakingScreen(hsk = 1, onNavigateBack = {})
    }
}
