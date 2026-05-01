// HskLevel.kt — Mandarin Learn
// Enum-like value class for the 5 HSK levels supported by the app.
// Used throughout the domain layer to avoid raw Int parameters.

package com.mandarinlearn.domain.model

/**
 * Represents one of the 5 HSK proficiency levels this app covers (1–5).
 * Using a value class prevents accidental substitution of arbitrary Int values.
 */
@JvmInline
value class HskLevel(val value: Int) {
    init {
        require(value in 1..5) { "HskLevel must be 1–5, was $value" }
    }

    companion object {
        val HSK1 = HskLevel(1)
        val HSK2 = HskLevel(2)
        val HSK3 = HskLevel(3)
        val HSK4 = HskLevel(4)
        val HSK5 = HskLevel(5)

        val ALL = listOf(HSK1, HSK2, HSK3, HSK4, HSK5)

        fun fromInt(value: Int): HskLevel = HskLevel(value)
    }

    override fun toString(): String = "HSK $value"
}
