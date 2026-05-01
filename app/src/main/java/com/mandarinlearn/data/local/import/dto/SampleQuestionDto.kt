// SampleQuestionDto.kt — Mandarin Learn
// Data Transfer Object for deserialising sample_questions.json.

package com.mandarinlearn.data.local.import.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the sample question JSON schema from the Research agent.
 * [options] is re-serialised as JSON string for storage in SampleQuestionEntity.
 * [audioTextChinese] and [audioTextPinyin] are optional — present only for listening questions.
 */
@Serializable
data class SampleQuestionDto(
    val id: String,
    @SerialName("hsk_level")
    val hskLevel: Int,
    val section: String,
    @SerialName("question_type")
    val questionType: String,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("audio_text_chinese")
    val audioTextChinese: String? = null,
    @SerialName("audio_text_pinyin")
    val audioTextPinyin: String? = null,
    val options: List<String>,
    @SerialName("correct_answer")
    val correctAnswer: String,
    val explanation: String,
)
