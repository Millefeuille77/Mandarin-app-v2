// VocabularyDto.kt — Mandarin Learn
// Data Transfer Object for deserialising hsk{1-5}_vocab.json.
// Uses kotlinx.serialization. Json { ignoreUnknownKeys = true } per ARCHITECTURE.md §3.2.

package com.mandarinlearn.data.local.import.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the JSON schema produced by the Research agent.
 * Maps to [com.mandarinlearn.data.local.entity.VocabularyEntity] via JsonImporter.
 */
@Serializable
data class VocabularyDto(
    val id: String,
    val character: String,
    val pinyin: String,
    val translation: String,
    @SerialName("hsk_level")
    val hskLevel: Int,
    @SerialName("part_of_speech")
    val partOfSpeech: String,
    @SerialName("example_sentence")
    val exampleSentence: ExampleSentenceDto,
)

@Serializable
data class ExampleSentenceDto(
    val chinese: String,
    val pinyin: String,
    val english: String,
)
