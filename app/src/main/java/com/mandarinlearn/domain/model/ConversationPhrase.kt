// ConversationPhrase.kt — Mandarin Learn
// Domain model for a speaking-practice conversation phrase.

package com.mandarinlearn.domain.model

/**
 * Domain representation of a conversation phrase used in speaking practice.
 */
data class ConversationPhrase(
    val id: String,
    val hskLevel: Int,
    val category: String,
    val chinese: String,
    val pinyin: String,
    val english: String,
    val usageContext: String,
)
