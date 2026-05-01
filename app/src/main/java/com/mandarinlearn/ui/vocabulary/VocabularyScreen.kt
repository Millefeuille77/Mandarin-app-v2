// VocabularyScreen.kt — Mandarin Learn
// Full implementation replacing Phase 1 placeholder.
// UX_SPECIFICATION.md §4 Screen 2: HSK selector, word list, search, stats, start-flashcards.

package com.mandarinlearn.ui.vocabulary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.ui.components.CharacterDefinitionSheet
import com.mandarinlearn.ui.components.EmptyState
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.HskLevelChipRow
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * VocabularyScreen — browse and search HSK vocabulary.
 * Implements UX_SPECIFICATION.md §4 Screen 2 fully.
 *
 * @param viewModel              The screen ViewModel (injected from AppContainer via factory).
 * @param onNavigateToFlashcards Navigate to FlashcardScreen for the current HSK level.
 * @param onNavigateBack         Pop this screen off the back stack.
 * @param modifier               Optional modifier.
 */
@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    onNavigateToFlashcards: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedWord by remember { mutableStateOf<VocabularyWord?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_vocabulary),
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is VocabularyUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is VocabularyUiState.Error   -> ErrorState(
                    message  = state.message,
                    onRetry  = { viewModel.selectLevel(1) },
                    modifier = Modifier.fillMaxSize(),
                )
                is VocabularyUiState.Content -> VocabularyContent(
                    state             = state,
                    onLevelSelected   = viewModel::selectLevel,
                    onSearchChanged   = viewModel::updateSearch,
                    onWordTapped      = { selectedWord = it },
                    onStartFlashcards = { onNavigateToFlashcards(state.selectedLevel) },
                )
            }
        }
    }

    // Detail bottom sheet
    selectedWord?.let { word ->
        CharacterDefinitionSheet(
            word        = word,
            onDismiss   = { selectedWord = null },
            onPlayAudio = { selectedWord = null },
        )
    }
}

@Composable
private fun VocabularyContent(
    state: VocabularyUiState.Content,
    onLevelSelected: (Int) -> Unit,
    onSearchChanged: (String) -> Unit,
    onWordTapped: (VocabularyWord) -> Unit,
    onStartFlashcards: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HskLevelChipRow(
            selectedLevel    = state.selectedLevel,
            onLevelSelected  = onLevelSelected,
            modifier         = Modifier.padding(vertical = SpacingXs),
        )
        OutlinedTextField(
            value         = state.searchQuery,
            onValueChange = onSearchChanged,
            placeholder   = { Text(stringResource(R.string.vocab_search_placeholder)) },
            leadingIcon   = {
                Icon(Icons.Filled.Search,
                     contentDescription = stringResource(R.string.content_desc_search))
            },
            trailingIcon  = if (state.searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChanged("") },
                               modifier = Modifier.heightIn(min = MinTouchTarget)) {
                        Icon(Icons.Filled.Clear,
                             contentDescription = stringResource(R.string.content_desc_clear_search))
                    }
                }
            } else null,
            singleLine    = true,
            modifier      = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
                .padding(horizontal = PagePaddingH),
        )
        Text(
            text     = stringResource(R.string.vocab_stats_strip,
                           state.totalCount, state.masteredCount, state.dueCount),
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
        )
        val buttonLabel = if (state.dueCount > 0)
            stringResource(R.string.vocab_start_review, state.dueCount)
        else stringResource(R.string.vocab_start_new, state.newCount)

        MandarinPrimaryButton(
            text     = buttonLabel,
            onClick  = onStartFlashcards,
            enabled  = state.dueCount > 0 || state.newCount > 0,
            modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
        )
        if (state.words.isEmpty() && state.searchQuery.isNotBlank()) {
            EmptyState(
                icon                   = Icons.Filled.Inbox,
                iconContentDescription = stringResource(R.string.content_desc_empty_icon),
                title                  = stringResource(R.string.vocab_empty_search_title),
                body                   = stringResource(R.string.vocab_empty_search_message,
                                             state.searchQuery),
                modifier               = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn {
                items(state.words, key = { it.id }) { word ->
                    VocabularyWordRow(word = word, onTap = onWordTapped)
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                }
            }
        }
    }
}

// Legacy overload used by Phase 1 AppNavigation (no ViewModel param)
@Composable
fun VocabularyScreen(
    hsk: Int,
    onNavigateToFlashcards: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.screen_vocabulary))
    }
}

@Preview(showBackground = true)
@Composable
private fun VocabularyContentPreview() {
    MandarinLearnTheme {
        VocabularyContent(
            state = VocabularyUiState.Content(
                words         = listOf(
                    VocabularyWord("hsk1_001", 1, "你好", "nǐ hǎo", "Hello",
                        "interjection", "你好！", "Nǐ hǎo!", "Hello!",
                        2.5, 0, 0, 0L, null, false),
                    VocabularyWord("hsk1_002", 1, "谢谢", "xiè xiè", "Thank you",
                        "verb", "谢谢你。", "Xiè xiè nǐ.", "Thank you.",
                        2.5, 6, 2, 0L, null, true),
                ),
                selectedLevel = 1,
                searchQuery   = "",
                totalCount    = 153,
                masteredCount = 12,
                dueCount      = 5,
                newCount      = 10,
            ),
            onLevelSelected   = {},
            onSearchChanged   = {},
            onWordTapped      = {},
            onStartFlashcards = {},
        )
    }
}
