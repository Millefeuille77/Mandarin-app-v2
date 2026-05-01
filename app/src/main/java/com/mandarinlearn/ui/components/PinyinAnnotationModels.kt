// PinyinAnnotationModels.kt — Mandarin Learn
// Data models used by PinyinText composable for ruby-style pinyin rendering.
// Implements UX_SPECIFICATION.md §3.6 (PinyinText composable).
// PinyinAnnotation domain model lives in domain/model/; this file holds UI-layer helpers.

package com.mandarinlearn.ui.components

import com.mandarinlearn.domain.model.PinyinAnnotation

/**
 * Wraps a [PinyinAnnotation] with display properties needed by the PinyinText composable.
 *
 * @param annotation     The underlying domain annotation (character + pinyin).
 * @param isTappable     True if the character should trigger onCharacterClick.
 *                       Punctuation characters (pinyin == "") are NOT tappable per UX spec §3.6.
 * @param isHighlighted  True if this character is in the passage's vocabularyHighlights list.
 */
data class AnnotatedCharacter(
    val annotation: PinyinAnnotation,
    val isTappable: Boolean,
    val isHighlighted: Boolean = false,
) {
    /** True for punctuation: pinyin is empty string. */
    val isPunctuation: Boolean get() = annotation.pinyin.isEmpty()
}

/**
 * Converts a list of [PinyinAnnotation] domain objects into [AnnotatedCharacter] UI models.
 * Punctuation (empty pinyin) is marked as non-tappable.
 * Characters present in [highlights] are flagged as highlighted.
 *
 * @param annotations  Raw domain annotations from ReadingPassage.
 * @param highlights   Optional set of strings that should be highlighted (vocabulary words).
 */
fun List<PinyinAnnotation>.toAnnotatedCharacters(
    highlights: Set<String> = emptySet(),
): List<AnnotatedCharacter> = map { annotation ->
    AnnotatedCharacter(
        annotation    = annotation,
        isTappable    = annotation.pinyin.isNotEmpty(), // punctuation is not tappable
        isHighlighted = highlights.contains(annotation.character),
    )
}
