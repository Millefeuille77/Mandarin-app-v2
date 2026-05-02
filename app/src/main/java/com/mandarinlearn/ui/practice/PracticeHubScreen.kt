// PracticeHubScreen.kt — Mandarin Learn
// Full implementation replacing Phase 1 placeholder.
// UX_SPECIFICATION.md §2: Practice tab root with 4 sub-section entry cards.

package com.mandarinlearn.ui.practice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.HskLevelChipRow
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * PracticeHubScreen — Practice tab root.
 * Shows a level selector and 4 sub-section entry cards (Vocabulary, Reading, Listening, Speaking).
 *
 * @param viewModel              Injected PracticeHubViewModel.
 * @param onNavigateToVocabulary Navigate to VocabularyScreen for the given HSK level.
 * @param onNavigateToReading    Navigate to ReadingListScreen.
 * @param onNavigateToListening  Navigate to ListeningScreen.
 * @param onNavigateToSpeaking   Navigate to SpeakingScreen.
 * @param modifier               Optional modifier.
 */
@Composable
fun PracticeHubScreen(
    viewModel: PracticeHubViewModel,
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToReading: (Int) -> Unit,
    onNavigateToListening: (Int) -> Unit,
    onNavigateToSpeaking: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MandarinTopBar(title = stringResource(R.string.screen_practice_hub))
        },
        modifier = modifier,
    ) { innerPadding ->
        when (val state = uiState) {
            is PracticeHubUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
            is PracticeHubUiState.Error   -> ErrorState(
                message  = state.message,
                onRetry  = {},
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            is PracticeHubUiState.Content -> PracticeHubContent(
                state                  = state,
                onLevelSelected        = viewModel::selectLevel,
                onNavigateToVocabulary = onNavigateToVocabulary,
                onNavigateToReading    = onNavigateToReading,
                onNavigateToListening  = onNavigateToListening,
                onNavigateToSpeaking   = onNavigateToSpeaking,
                modifier               = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PracticeHubContent(
    state: PracticeHubUiState.Content,
    onLevelSelected: (Int) -> Unit,
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToReading: (Int) -> Unit,
    onNavigateToListening: (Int) -> Unit,
    onNavigateToSpeaking: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PagePaddingH, vertical = SpacingM),
        verticalArrangement = Arrangement.spacedBy(SpacingS),
    ) {
        Text(
            text  = stringResource(R.string.practice_hub_select_level),
            style = MaterialTheme.typography.headlineMedium,
        )
        HskLevelChipRow(
            selectedLevel   = state.selectedLevel,
            onLevelSelected = onLevelSelected,
        )
        Spacer(Modifier.height(SpacingXs))
        Text(
            text  = stringResource(R.string.practice_hub_choose_activity),
            style = MaterialTheme.typography.headlineMedium,
        )

        // Vocabulary card (shows due count as a badge when available)
        val vocabSubtitle = if (state.vocabDueCount > 0) {
            stringResource(R.string.practice_hub_vocab_due, state.vocabDueCount)
        } else {
            stringResource(R.string.practice_hub_vocab_subtitle)
        }
        PracticeCard(
            title    = stringResource(R.string.practice_hub_vocab_title),
            subtitle = vocabSubtitle,
            icon     = Icons.Filled.Book,
            onClick  = { onNavigateToVocabulary(state.selectedLevel) },
        )
        PracticeCard(
            title    = stringResource(R.string.practice_hub_reading_title),
            subtitle = stringResource(R.string.practice_hub_reading_subtitle),
            icon     = Icons.Filled.Edit,
            onClick  = { onNavigateToReading(state.selectedLevel) },
        )
        PracticeCard(
            title    = stringResource(R.string.practice_hub_listening_title),
            subtitle = stringResource(R.string.practice_hub_listening_subtitle),
            icon     = Icons.Filled.Headphones,
            onClick  = { onNavigateToListening(state.selectedLevel) },
        )
        PracticeCard(
            title    = stringResource(R.string.practice_hub_speaking_title),
            subtitle = stringResource(R.string.practice_hub_speaking_subtitle),
            icon     = Icons.Filled.Mic,
            onClick  = { onNavigateToSpeaking(state.selectedLevel) },
        )
    }
}

@Composable
private fun PracticeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardDesc = "$title. $subtitle"
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = cardDesc },
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingM),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null, // parent carries description
                modifier           = Modifier.size(MinTouchTarget),
                tint               = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Legacy overload used by Phase 1 AppNavigation
@Composable
fun PracticeHubScreen(
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToReading: (Int) -> Unit,
    onNavigateToListening: (Int) -> Unit,
    onNavigateToSpeaking: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PracticeHubContent(
        state = PracticeHubUiState.Content(selectedLevel = 1, vocabDueCount = 0),
        onLevelSelected        = {},
        onNavigateToVocabulary = onNavigateToVocabulary,
        onNavigateToReading    = onNavigateToReading,
        onNavigateToListening  = onNavigateToListening,
        onNavigateToSpeaking   = onNavigateToSpeaking,
        modifier               = modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
private fun PracticeHubScreenPreview() {
    MandarinLearnTheme {
        PracticeHubScreen(
            onNavigateToVocabulary = {},
            onNavigateToReading    = {},
            onNavigateToListening  = {},
            onNavigateToSpeaking   = {},
        )
    }
}
