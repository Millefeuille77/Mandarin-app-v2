// FlashcardComponents.kt — Mandarin Learn
// FlippableCard and RatingButtonRow for FlashcardScreen.
// Split from FlashcardScreen.kt per 300-line rule. UX_SPECIFICATION.md §4 Screen 3.

package com.mandarinlearn.ui.vocabulary

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandarinlearn.R
import com.mandarinlearn.domain.model.VocabularyWord
import com.mandarinlearn.domain.srs.SrsQuality
import com.mandarinlearn.ui.components.MandarinSecondaryButton
import com.mandarinlearn.ui.theme.CardElevation
import com.mandarinlearn.ui.theme.FlashcardMaxHeight
import com.mandarinlearn.ui.theme.HanziDisplayStyle
import com.mandarinlearn.ui.theme.HanziLargeStyle
import com.mandarinlearn.ui.theme.MinTouchTarget
import com.mandarinlearn.ui.theme.PinyinLargeStyle
import com.mandarinlearn.ui.theme.SpacingL
import com.mandarinlearn.ui.theme.SpacingM
import com.mandarinlearn.ui.theme.SpacingXs
import com.mandarinlearn.ui.theme.onSuccessColor
import com.mandarinlearn.ui.theme.onWarningColor
import com.mandarinlearn.ui.theme.successColor
import com.mandarinlearn.ui.theme.warningColor

/**
 * Animates a rotateY flip (or crossfades when [reduceMotion] = true).
 * Front shows the hanzi; tapping reveals the back.
 */
@Composable
fun FlippableCard(
    card: VocabularyWord,
    isFlipped: Boolean,
    reduceMotion: Boolean,
    onFlip: () -> Unit,
    onPlayAudio: () -> Unit,
    isAudioLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (reduceMotion) {
        Crossfade(targetState = isFlipped, animationSpec = tween(200), label = "card-flip") { flipped ->
            CardSurface(card, flipped, onFlip, onPlayAudio, isAudioLoading, modifier)
        }
    } else {
        val rotation by animateFloatAsState(
            targetValue   = if (isFlipped) 180f else 0f,
            animationSpec = tween(350),
            label         = "card-flip-rotation",
        )
        val showBack = rotation > 90f
        CardSurface(
            card           = card,
            showBack       = showBack,
            onFlip         = onFlip,
            onPlayAudio    = onPlayAudio,
            isAudioLoading = isAudioLoading,
            modifier       = modifier.graphicsLayer {
                rotationY = rotation
                // Flip content back upright when showing back
                if (showBack) rotationY = rotation - 180f
            },
        )
    }
}

@Composable
private fun CardSurface(
    card: VocabularyWord,
    showBack: Boolean,
    onFlip: () -> Unit,
    onPlayAudio: () -> Unit,
    isAudioLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = CardElevation),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier  = modifier
            .fillMaxWidth()
            .height(FlashcardMaxHeight)
            .clickable(enabled = !showBack) { onFlip() },
    ) {
        if (showBack) CardBack(card, onPlayAudio, isAudioLoading)
        else CardFront(card, onFlip, onPlayAudio, isAudioLoading)
    }
}

@Composable
private fun CardFront(
    card: VocabularyWord,
    onFlip: () -> Unit,
    onPlayAudio: () -> Unit,
    isAudioLoading: Boolean,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().height(FlashcardMaxHeight),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(card.character, style = HanziDisplayStyle, color = MaterialTheme.colorScheme.secondary,
             textAlign = TextAlign.Center)
        AudioIconButton(onPlayAudio, isAudioLoading)
        Spacer(Modifier.height(SpacingL))
        Text(stringResource(R.string.flashcard_tap_to_flip), style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        MandarinSecondaryButton(
            text     = stringResource(R.string.flashcard_show_answer),
            onClick  = onFlip,
            modifier = Modifier.fillMaxWidth(0.8f).defaultMinSize(minHeight = MinTouchTarget),
        )
    }
}

@Composable
private fun CardBack(card: VocabularyWord, onPlayAudio: () -> Unit, isAudioLoading: Boolean) {
    val desc = stringResource(R.string.flashcard_back_content_desc,
        card.character, card.pinyin, card.translation, card.exampleChinese, card.exampleEnglish)
    Column(
        modifier            = Modifier.fillMaxWidth().height(FlashcardMaxHeight)
            .semantics { contentDescription = desc },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(card.character, style = HanziLargeStyle, color = MaterialTheme.colorScheme.secondary)
        AudioIconButton(onPlayAudio, isAudioLoading)
        Text(card.pinyin, style = PinyinLargeStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(card.translation, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(SpacingM))
        Text(card.exampleChinese, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
             color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(0.85f))
        Text(card.examplePinyin, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
             color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(0.85f))
        Text(card.exampleEnglish, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
             modifier = Modifier.fillMaxWidth(0.85f))
    }
}

@Composable
private fun AudioIconButton(onPlayAudio: () -> Unit, isLoading: Boolean) {
    val desc = stringResource(R.string.content_desc_play_audio)
    IconButton(onClick = onPlayAudio,
               modifier = Modifier.size(MinTouchTarget).semantics { contentDescription = desc }) {
        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        else Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

/** Four SM-2 rating buttons in a horizontal row with next-interval preview labels. */
@Composable
fun RatingButtonRow(
    nextIntervals: Map<SrsQuality, Int>,
    onRate: (SrsQuality) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(SpacingXs)) {
        RatingBtn(SrsQuality.FORGOT, stringResource(R.string.flashcard_rating_forgot),
            Icons.Filled.Close, nextIntervals[SrsQuality.FORGOT] ?: 1,
            MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError, onRate, Modifier.weight(1f))
        RatingBtn(SrsQuality.HARD, stringResource(R.string.flashcard_rating_hard),
            Icons.Filled.Schedule, nextIntervals[SrsQuality.HARD] ?: 1,
            warningColor(), onWarningColor(), onRate, Modifier.weight(1f))
        RatingBtn(SrsQuality.GOOD, stringResource(R.string.flashcard_rating_good),
            Icons.Filled.Check, nextIntervals[SrsQuality.GOOD] ?: 1,
            MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, onRate, Modifier.weight(1f))
        RatingBtn(SrsQuality.EASY, stringResource(R.string.flashcard_rating_easy),
            Icons.Filled.Star, nextIntervals[SrsQuality.EASY] ?: 1,
            successColor(), onSuccessColor(), onRate, Modifier.weight(1f))
    }
}

@Composable
private fun RatingBtn(
    quality: SrsQuality, label: String, icon: ImageVector, intervalDays: Int,
    containerColor: Color, contentColor: Color, onRate: (SrsQuality) -> Unit, modifier: Modifier,
) {
    val intervalLabel = stringResource(R.string.flashcard_interval_days, intervalDays)
    val desc = stringResource(R.string.flashcard_rating_button_desc, label, intervalLabel)
    Button(
        onClick  = { onRate(quality) },
        colors   = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape    = MaterialTheme.shapes.small,
        modifier = modifier.defaultMinSize(minHeight = MinTouchTarget).semantics { contentDescription = desc },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(intervalLabel, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
