// ReadingListScreen.kt — Mandarin Learn
// Full Phase 4 implementation replacing placeholder.
// UX_SPECIFICATION.md §4 Screen 4: passage list with HSK selector, difficulty note, completion tag.

package com.mandarinlearn.ui.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.PinyinAnnotation
import com.mandarinlearn.domain.model.ReadingPassage
import com.mandarinlearn.ui.components.EmptyState
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.HskLevelChipRow
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXs

/**
 * ReadingListScreen — list of reading passages for the selected HSK level.
 * Full implementation per UX_SPECIFICATION.md §4 Screen 4.
 *
 * @param viewModel            Injected [ReadingListViewModel] with real data.
 * @param onNavigateToPassage  Navigate to PassageScreen for the given passage ID.
 * @param onNavigateBack       Pop this screen off the back stack.
 * @param modifier             Optional modifier.
 */
@Composable
fun ReadingListScreen(
    viewModel: ReadingListViewModel,
    onNavigateToPassage: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_reading_list),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is ReadingListUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is ReadingListUiState.Error   -> ErrorState(
                    message  = state.message,
                    onRetry  = { viewModel.selectLevel(1) },
                    modifier = Modifier.fillMaxSize(),
                )
                is ReadingListUiState.Content -> ReadingListContent(
                    state             = state,
                    onLevelSelected   = viewModel::selectLevel,
                    onPassageTapped   = onNavigateToPassage,
                )
            }
        }
    }
}

/**
 * Legacy overload retained for backward-compat with AppNavigation before Phase 4 nav wiring.
 * This overload creates a no-op ViewModel stand-in — callers should prefer the ViewModel version.
 */
@Composable
fun ReadingListScreen(
    hsk: Int,
    onNavigateToPassage: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Placeholder body — Phase 4 nav wiring removes usage of this overload from AppNavigation.
    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_reading_list),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}

@Composable
private fun ReadingListContent(
    state: ReadingListUiState.Content,
    onLevelSelected: (Int) -> Unit,
    onPassageTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HskLevelChipRow(
            selectedLevel   = state.selectedLevel,
            onLevelSelected = onLevelSelected,
            modifier        = Modifier.padding(vertical = SpacingXs),
        )
        Text(
            text     = stringResource(R.string.reading_pinyin_note),
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
        )
        if (state.passages.isEmpty()) {
            EmptyState(
                icon                   = Icons.Filled.MenuBook,
                iconContentDescription = stringResource(R.string.content_desc_empty_icon),
                title                  = stringResource(R.string.reading_empty_title),
                body                   = stringResource(R.string.reading_empty_body),
                modifier               = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SpacingS),
                modifier            = Modifier.padding(horizontal = PagePaddingH),
            ) {
                item { Spacer(Modifier.height(SpacingXs)) }
                items(state.passages, key = { it.id }) { passage ->
                    PassageListCard(
                        passage  = passage,
                        onTapped = { onPassageTapped(passage.id) },
                    )
                }
                item { Spacer(Modifier.height(SpacingM)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassageListCard(
    passage: ReadingPassage,
    onTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val readStatus = if (passage.isCompleted) {
        stringResource(R.string.reading_status_read)
    } else {
        stringResource(R.string.reading_status_not_read)
    }
    val cardDesc = stringResource(
        R.string.reading_card_content_desc,
        passage.title,
        passage.wordCount,
        readStatus,
    )

    Card(
        onClick    = onTapped,
        elevation  = CardDefaults.cardElevation(defaultElevation = CardElevation),
        colors     = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier   = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDesc },
    ) {
        Column(modifier = Modifier.padding(SpacingM)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text     = passage.title,
                    style    = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                )
                if (passage.isCompleted) {
                    SuggestionChip(
                        onClick = {},
                        label   = { Text(stringResource(R.string.reading_status_read)) },
                        colors  = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(SpacingXs))
            // First ~40 characters of the passage as a preview
            Text(
                text     = passage.chineseText.take(40),
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(SpacingXs))
            Text(
                text  = stringResource(R.string.reading_word_count, passage.wordCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---- Previews ----------------------------------------------------------------

@Preview(showBackground = true, name = "ReadingListScreen — Content")
@Composable
private fun ReadingListScreenPreview() {
    MandarinLearnTheme {
        ReadingListContent(
            state = ReadingListUiState.Content(
                passages = listOf(
                    ReadingPassage(
                        id                   = "hsk1_reading_001",
                        hskLevel             = 1,
                        title                = stringResource(R.string.preview_passage_title_1),
                        chineseText          = "你好，我是学生。我叫李明。",
                        pinyinAnnotations    = listOf(PinyinAnnotation("你", "nǐ")),
                        englishTranslation   = "Hello, I am a student. My name is Li Ming.",
                        vocabularyHighlights = listOf("你好", "学生"),
                        wordCount            = 8,
                        isCompleted          = false,
                        completedAt          = null,
                    ),
                    ReadingPassage(
                        id                   = "hsk1_reading_002",
                        hskLevel             = 1,
                        title                = stringResource(R.string.preview_passage_title_2),
                        chineseText          = "我家有爸爸、妈妈和我。",
                        pinyinAnnotations    = listOf(PinyinAnnotation("我", "wǒ")),
                        englishTranslation   = "My family has dad, mom, and me.",
                        vocabularyHighlights = listOf("家", "爸爸", "妈妈"),
                        wordCount            = 7,
                        isCompleted          = true,
                        completedAt          = 20000L,
                    ),
                ),
                selectedLevel = 1,
            ),
            onLevelSelected = {},
            onPassageTapped = {},
        )
    }
}
