// PassageScreen.kt — Mandarin Learn
// Full Phase 4 implementation replacing placeholder.
// UX_SPECIFICATION.md §4 Screen 5: pinyin passage, font slider, pinyin toggle, character-tap popup.
// Controls bar and translation card are in PassageControls.kt (300-line split).

package com.mandarinlearn.ui.reading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.PinyinAnnotation
import com.mandarinlearn.domain.model.ReadingPassage
import com.mandarinlearn.ui.components.AnnotatedCharacter
import com.mandarinlearn.ui.components.CharacterDefinitionSheet
import com.mandarinlearn.ui.components.ErrorState
import com.mandarinlearn.ui.components.LoadingState
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinTopBar
import com.mandarinlearn.ui.components.NoDefinitionSheet
import com.mandarinlearn.ui.components.PinyinText
import com.mandarinlearn.ui.components.toAnnotatedCharacters
import com.mandarinlearn.ui.theme.MandarinLearnTheme
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS

/**
 * PassageScreen — displays a reading passage with pinyin annotations.
 * Full Phase 4 implementation per UX_SPECIFICATION.md §4 Screen 5.
 *
 * Controls bar (pinyin toggle, font slider, play-all) → [PassageControlsBar].
 * Translation card → [PassageTranslationCard].
 * Both extracted into PassageControls.kt to stay within the 300-line file rule.
 *
 * @param viewModel      Injected [PassageViewModel] with the passage loaded by ID.
 * @param onNavigateBack Pop back stack.
 * @param modifier       Optional modifier.
 */
@Composable
fun PassageScreen(
    viewModel: PassageViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Phase 5: collect one-time snackbar events from PassageViewModel (e.g. Play all failure)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PassageEvent.ShowSnackbar -> {
                    // Map string key to resource — currently only "audio_coming_soon"
                    snackbarHostState.showSnackbar("Audio coming soon")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            val title = (uiState as? PassageUiState.Content)?.passage?.title
                ?: stringResource(R.string.screen_passage)
            MandarinTopBar(
                title          = title,
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier     = modifier,
    ) { innerPadding ->
        when (val state = uiState) {
            is PassageUiState.Loading -> LoadingState(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            is PassageUiState.Error   -> ErrorState(
                message  = state.message,
                onRetry  = {},
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            is PassageUiState.Content -> PassageContent(
                state               = state,
                snackbarHostState   = snackbarHostState,
                onTogglePinyin      = viewModel::togglePinyin,
                onFontScale         = viewModel::setFontScale,
                onCharTapped        = viewModel::onCharacterTapped,
                onDismissSheet      = viewModel::dismissDefinition,
                onToggleTranslation = viewModel::toggleTranslation,
                onPlayAll           = viewModel::playAll,
                onMarkAsRead        = {
                    viewModel.markAsRead {
                        // PassageViewModel calls markCompleted; the Room flow updates isCompleted
                        // which the screen reads to flip the button label.
                    }
                },
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

/**
 * Legacy overload retained for backward-compat with AppNavigation entries that
 * do not yet have access to a real ViewModel (preview / test environments).
 */
@Composable
fun PassageScreen(
    passageId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            MandarinTopBar(
                title          = stringResource(R.string.screen_passage),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}

@Composable
private fun PassageContent(
    state: PassageUiState.Content,
    snackbarHostState: SnackbarHostState,
    onTogglePinyin: () -> Unit,
    onFontScale: (Float) -> Unit,
    onCharTapped: (String) -> Unit,
    onDismissSheet: () -> Unit,
    onToggleTranslation: () -> Unit,
    onMarkAsRead: () -> Unit,
    onPlayAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyState = rememberLazyListState()

    val annotatedChars: List<AnnotatedCharacter> = remember(state.passage.pinyinAnnotations) {
        state.passage.pinyinAnnotations.toAnnotatedCharacters(
            highlights = state.passage.vocabularyHighlights.toSet()
        )
    }

    // Full passage as one string for TalkBack single-swipe reading (UX spec §4 Screen 5)
    val passageDesc = state.passage.chineseText

    Column(modifier = modifier) {
        // Sticky controls bar: pinyin toggle + font slider + play-all (Phase 5 wired)
        PassageControlsBar(
            showPinyin     = state.showPinyin,
            fontScale      = state.fontScale,
            isPlayingAll   = state.isPlayingAll,
            onTogglePinyin = onTogglePinyin,
            onFontScale    = onFontScale,
            onPlayAll      = onPlayAll,
        )

        LazyColumn(
            state    = lazyState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = PagePaddingH)
                .semantics { contentDescription = passageDesc },
        ) {
            item {
                Spacer(Modifier.height(SpacingM))
                PinyinText(
                    annotations      = annotatedChars,
                    showPinyin       = state.showPinyin,
                    fontScale        = state.fontScale,
                    onCharacterClick = onCharTapped,
                    modifier         = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(SpacingM))
            }
            item {
                PassageTranslationCard(
                    translation = state.passage.englishTranslation,
                    isExpanded  = state.showTranslation,
                    onToggle    = onToggleTranslation,
                )
                Spacer(Modifier.height(SpacingM))
            }
        }

        // Footer: "Mark as read" button (UX spec §4 Screen 5)
        MandarinPrimaryButton(
            text      = if (state.passage.isCompleted) {
                stringResource(R.string.reading_already_read)
            } else {
                stringResource(R.string.reading_mark_as_read)
            },
            onClick   = onMarkAsRead,
            enabled   = !state.passage.isCompleted && !state.isMarkingRead,
            isLoading = state.isMarkingRead,
            modifier  = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingS),
        )
    }

    // Character definition popup sheets
    if (state.selectedWord != null) {
        CharacterDefinitionSheet(
            word        = state.selectedWord,
            onDismiss   = onDismissSheet,
            onPlayAudio = { /* audio available in Phase 5 */ },
        )
    } else if (state.noDefinition && state.tappedCharacter.isNotEmpty()) {
        NoDefinitionSheet(
            character = state.tappedCharacter,
            onDismiss = onDismissSheet,
        )
    }
}

// ---- Preview ----------------------------------------------------------------

@Preview(showBackground = true, name = "PassageScreen — Content")
@Composable
private fun PassageContentPreview() {
    MandarinLearnTheme {
        PassageContent(
            state = PassageUiState.Content(
                passage = ReadingPassage(
                    id                   = "hsk1_reading_001",
                    hskLevel             = 1,
                    title                = stringResource(R.string.preview_passage_title_1),
                    chineseText          = "你好，我是学生。",
                    pinyinAnnotations    = listOf(
                        PinyinAnnotation("你", "nǐ"),
                        PinyinAnnotation("好", "hǎo"),
                        PinyinAnnotation("，", ""),
                        PinyinAnnotation("我", "wǒ"),
                        PinyinAnnotation("是", "shì"),
                        PinyinAnnotation("学", "xué"),
                        PinyinAnnotation("生", "shēng"),
                        PinyinAnnotation("。", ""),
                    ),
                    englishTranslation   = "Hello, I am a student.",
                    vocabularyHighlights = listOf("你好", "学生"),
                    wordCount            = 5,
                    isCompleted          = false,
                    completedAt          = null,
                ),
                showPinyin      = true,
                fontScale       = 1.0f,
                selectedWord    = null,
                noDefinition    = false,
                tappedCharacter = "",
                isMarkingRead   = false,
                showTranslation = false,
            ),
            snackbarHostState   = remember { SnackbarHostState() },
            onTogglePinyin      = {},
            onFontScale         = {},
            onCharTapped        = {},
            onDismissSheet      = {},
            onToggleTranslation = {},
            onMarkAsRead        = {},
        )
    }
}
