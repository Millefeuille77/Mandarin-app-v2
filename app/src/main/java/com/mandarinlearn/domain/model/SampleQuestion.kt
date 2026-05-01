// SampleQuestion.kt — Mandarin Learn
// Domain model for a sample exam question.

package com.mandarinlearn.domain.model

/**
 * Domain representation of one exam question.
 * [options] is the decoded list (not raw JSON) for display in ExamScreen.
 */
data class SampleQuestion(
    val id: String,
    val hskLevel: Int,
    val section: String,
    val questionType: String,
    val questionText: String,
    val audioTextChinese: String?,
    val audioTextPinyin: String?,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
)
