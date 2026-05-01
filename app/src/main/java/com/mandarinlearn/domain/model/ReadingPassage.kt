// ReadingPassage.kt — Mandarin Learn
// Domain model for a reading passage with decoded pinyin annotations.

package com.mandarinlearn.domain.model

/**
 * Domain representation of a reading passage.
 * [pinyinAnnotations] is the decoded list (not raw JSON) for use in UI.
 * [vocabularyHighlights] is a list of key vocabulary strings to highlight.
 */
data class ReadingPassage(
    val id: String,
    val hskLevel: Int,
    val title: String,
    val chineseText: String,
    val pinyinAnnotations: List<PinyinAnnotation>,
    val englishTranslation: String,
    val vocabularyHighlights: List<String>,
    val wordCount: Int,
    val isCompleted: Boolean,
    val completedAt: Long?,
)
