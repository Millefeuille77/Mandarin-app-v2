// ToneDrill.kt — Mandarin Learn
// Domain model for a tone-pair drill.

package com.mandarinlearn.domain.model

/**
 * Domain representation of a tone-pair drill.
 * [additionalExamples] is the decoded list (not raw JSON) from the DB.
 */
data class ToneDrill(
    val id: String,
    val tonePair: String,
    val description: String,
    val exampleWord: String,
    val pinyin: String,
    val translation: String,
    val additionalExamples: List<ToneExample>,
)

data class ToneExample(
    val word: String,
    val pinyin: String,
    val translation: String,
)
