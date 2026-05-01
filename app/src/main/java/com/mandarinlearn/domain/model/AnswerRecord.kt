// AnswerRecord.kt — Mandarin Learn
// Domain model for one answer in an exam's "review mistakes" list.
// Serializable to round-trip through exam_results.answers_json.

package com.mandarinlearn.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One user answer from a completed exam, used for the "Review mistakes" feature.
 * Stored as a JSON array in exam_results.answers_json.
 */
@Serializable
data class AnswerRecord(
    @SerialName("question_id")
    val questionId: String,
    @SerialName("user_answer")
    val userAnswer: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("is_correct")
    val isCorrect: Boolean,
)
