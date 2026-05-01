// ConversationPhraseDto.kt — Mandarin Learn
// Data Transfer Object for deserialising conversation_phrases.json.

package com.mandarinlearn.data.local.import.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the conversation phrase JSON schema from the Research agent.
 */
@Serializable
data class ConversationPhraseDto(
    val id: String,
    @SerialName("hsk_level")
    val hskLevel: Int,
    val category: String,
    val chinese: String,
    val pinyin: String,
    val english: String,
    @SerialName("usage_context")
    val usageContext: String,
)
