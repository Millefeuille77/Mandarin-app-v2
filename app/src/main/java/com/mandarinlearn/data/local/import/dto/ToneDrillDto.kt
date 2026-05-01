// ToneDrillDto.kt — Mandarin Learn
// Data Transfer Object for deserialising tone_drills.json.

package com.mandarinlearn.data.local.import.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the tone drill JSON schema from the Research agent.
 * [additionalExamples] is re-serialised as JSON string for storage (ToneDrillEntity).
 */
@Serializable
data class ToneDrillDto(
    val id: String,
    @SerialName("tone_pair")
    val tonePair: String,
    val description: String,
    @SerialName("example_word")
    val exampleWord: String,
    val pinyin: String,
    val translation: String,
    @SerialName("additional_examples")
    val additionalExamples: List<ToneExampleDto>,
)

@Serializable
data class ToneExampleDto(
    val word: String,
    val pinyin: String,
    val translation: String,
)
