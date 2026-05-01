// ReadingDto.kt — Mandarin Learn
// Data Transfer Object for deserialising hsk{1-5}_readings.json.

package com.mandarinlearn.data.local.import.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the reading passage JSON schema from the Research agent.
 * [pinyinAnnotations] is a list of per-character annotation objects.
 * [vocabularyHighlights] is a list of key vocabulary strings.
 */
@Serializable
data class ReadingDto(
    val id: String,
    val title: String,
    @SerialName("hsk_level")
    val hskLevel: Int,
    @SerialName("chinese_text")
    val chineseText: String,
    @SerialName("pinyin_annotations")
    val pinyinAnnotations: List<PinyinAnnotationDto>,
    @SerialName("english_translation")
    val englishTranslation: String,
    @SerialName("vocabulary_highlights")
    val vocabularyHighlights: List<String>,
    @SerialName("word_count")
    val wordCount: Int,
)

/**
 * One character + its pinyin. Empty pinyin string for punctuation (per CLAUDE.md rule #2).
 */
@Serializable
data class PinyinAnnotationDto(
    val character: String,
    val pinyin: String,
)
