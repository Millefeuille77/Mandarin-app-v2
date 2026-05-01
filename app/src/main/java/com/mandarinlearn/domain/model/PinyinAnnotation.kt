// PinyinAnnotation.kt — Mandarin Learn
// Domain model for a single character + pinyin pair in a reading passage.
// Stored as JSON in reading_passages.pinyin_annotations.

package com.mandarinlearn.domain.model

import kotlinx.serialization.Serializable

/**
 * One character-pinyin pair from a reading passage.
 * [pinyin] is empty string for punctuation characters (per CLAUDE.md rule #2).
 * Serializable so it can be round-tripped through Room's JSON column.
 */
@Serializable
data class PinyinAnnotation(
    val character: String,
    val pinyin: String,
)
