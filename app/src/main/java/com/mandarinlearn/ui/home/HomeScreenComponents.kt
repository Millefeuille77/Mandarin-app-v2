// HomeScreenComponents.kt — Mandarin Learn
// Sub-composables for HomeScreen. Phase 8. UX spec §4 Screen 1.
package com.mandarinlearn.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.ui.components.MandarinPrimaryButton
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PagePaddingH
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingS
import com.mandarinlearn.ui.theme.SpacingXl
import com.mandarinlearn.ui.theme.SpacingXs
import java.util.Calendar

@Composable
internal fun HomeContent(
    state: HomeUiState.Content,
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToFlashcards: (Int) -> Unit,
    onNavigateToReading: (Int) -> Unit,
    onNavigateToListening: (Int) -> Unit,
    onNavigateToSpeaking: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier            = modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        item { GreetingCard() }
        item {
            StreakRow(
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                modifier      = Modifier.padding(horizontal = PagePaddingH),
            )
        }
        item {
            ReviewCard(
                dueCount   = state.totalDueCount,
                onReviewNow = { onNavigateToFlashcards(state.focusLevel) },
                onLearnNew  = { onNavigateToVocabulary(state.focusLevel) },
                modifier    = Modifier.padding(horizontal = PagePaddingH),
            )
        }
        item {
            Text(
                text     = stringResource(R.string.home_hsk_progress_header),
                style    = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
            )
        }
        items(state.levelRows) { row ->
            HskProgressRow(
                row      = row,
                onClick  = { onNavigateToVocabulary(row.hskLevel) },
                modifier = Modifier.padding(horizontal = PagePaddingH),
            )
        }
        item {
            Text(
                text     = stringResource(R.string.home_practice_header),
                style    = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = PagePaddingH, vertical = SpacingXs),
            )
        }
        item {
            QuickStartGrid(
                focusLevel            = state.focusLevel,
                onNavigateToVocabulary = onNavigateToVocabulary,
                onNavigateToReading   = onNavigateToReading,
                onNavigateToListening = onNavigateToListening,
                onNavigateToSpeaking  = onNavigateToSpeaking,
                modifier = Modifier.padding(horizontal = PagePaddingH),
            )
        }
    }
}

@Composable
internal fun GreetingCard(modifier: Modifier = Modifier) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> stringResource(R.string.home_greeting_morning)
        hour < 17 -> stringResource(R.string.home_greeting_afternoon)
        else      -> stringResource(R.string.home_greeting_evening)
    }
    Card(
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = modifier.fillMaxWidth().padding(horizontal = PagePaddingH, vertical = SpacingXs),
    ) {
        Text(text = greeting, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(SpacingM))
    }
}

@Composable
internal fun StreakRow(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier,
) {
    val desc = stringResource(R.string.home_streak_content_desc, currentStreak, longestStreak)
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .semantics { contentDescription = desc },
        horizontalArrangement = Arrangement.spacedBy(SpacingM),
    ) {
        StreakCard(stringResource(R.string.home_streak_current), currentStreak, Modifier.weight(1f))
        StreakCard(stringResource(R.string.home_streak_longest), longestStreak, Modifier.weight(1f))
    }
}

@Composable
private fun StreakCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation), modifier = modifier) {
        Column(modifier = Modifier.padding(SpacingM), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun ReviewCard(
    dueCount: Int,
    onReviewNow: () -> Unit,
    onLearnNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(SpacingM)) {
            if (dueCount > 0) {
                Text(stringResource(R.string.home_due_count, dueCount),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.home_due_label),
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(SpacingS))
                MandarinPrimaryButton(text = stringResource(R.string.home_review_now),
                    onClick = onReviewNow)
            } else {
                Text(stringResource(R.string.home_all_caught_up),
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(SpacingS))
                MandarinSecondaryButton(text = stringResource(R.string.home_learn_new),
                    onClick = onLearnNew)
            }
        }
    }
}

@Composable
internal fun HskProgressRow(
    row: LevelProgressRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowDesc = stringResource(R.string.home_hsk_row_content_desc,
        row.hskLevel, row.masteredCount, row.totalCount)
    Card(
        onClick   = onClick,
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MinTouchTarget)
            .semantics { contentDescription = rowDesc },
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = SpacingM, vertical = SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.hsk_level_label, row.hskLevel),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.2f))
            LinearProgressIndicator(
                progress   = row.masteredFraction,
                color      = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier   = Modifier
                    .weight(0.6f)
                    .height(8.dp)
                    .padding(horizontal = SpacingXs),
            )
            Text(
                text     = stringResource(R.string.home_hsk_row_count, row.masteredCount, row.totalCount),
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.2f),
            )
        }
    }
}

@Composable
internal fun QuickStartGrid(
    focusLevel: Int,
    onNavigateToVocabulary: (Int) -> Unit,
    onNavigateToReading: (Int) -> Unit,
    onNavigateToListening: (Int) -> Unit,
    onNavigateToSpeaking: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingM)) {
            QuickStartCard(stringResource(R.string.practice_hub_vocab_title),
                Icons.Filled.School, stringResource(R.string.home_quick_vocab_desc),
                { onNavigateToVocabulary(focusLevel) }, Modifier.weight(1f))
            QuickStartCard(stringResource(R.string.practice_hub_reading_title),
                Icons.Filled.MenuBook, stringResource(R.string.home_quick_reading_desc),
                { onNavigateToReading(focusLevel) }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(SpacingM))
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingM)) {
            QuickStartCard(stringResource(R.string.practice_hub_listening_title),
                Icons.Filled.Headphones, stringResource(R.string.home_quick_listening_desc),
                { onNavigateToListening(focusLevel) }, Modifier.weight(1f))
            QuickStartCard(stringResource(R.string.practice_hub_speaking_title),
                Icons.Filled.Mic, stringResource(R.string.home_quick_speaking_desc),
                { onNavigateToSpeaking(focusLevel) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickStartCard(
    label: String,
    icon: ImageVector,
    iconDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick   = onClick,
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        modifier  = modifier
            .height(120.dp)
            .defaultMinSize(minWidth = MinTouchTarget, minHeight = MinTouchTarget),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(SpacingM),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, iconDesc, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(SpacingXs))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
