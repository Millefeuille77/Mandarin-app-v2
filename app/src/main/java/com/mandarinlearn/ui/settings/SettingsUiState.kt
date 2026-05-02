// SettingsUiState.kt — Mandarin Learn
// Full Phase 9 UI state for SettingsScreen.
// UX_SPECIFICATION.md §4 Screen 11. IMPLEMENTATION_PLAN.md §Phase 9.

package com.mandarinlearn.ui.settings

/**
 * Sealed UI state for SettingsScreen.
 * Loading shown briefly while DataStore preferences are read for the first time.
 */
sealed class SettingsUiState {

    data object Loading : SettingsUiState()

    /**
     * Fully loaded state carrying all preference values and operation flags.
     *
     * @param theme              "system" | "light" | "dark". Default "system".
     * @param fontScaleIndex     0=Small(0.9), 1=Medium(1.0), 2=Large(1.15), 3=Extra-large(1.3).
     * @param reduceMotion       Skip card-flip animation when true.
     * @param audioSpeedIndex    Index into [AUDIO_SPEEDS]: 0=0.5x, 1=0.75x, 2=1.0x, 3=1.25x.
     * @param showPinyinDefault  Default pinyin-on/off for reading passages.
     * @param dailyNewCardsLimit New flashcards introduced per session (5–50).
     * @param isExporting        True while the SAF export stream is being written.
     * @param isImporting        True while import JSON is being parsed and applied.
     * @param isResetting        True while the reset Room transaction is running.
     * @param exportError        Non-null on export failure; screen shows this as a snackbar.
     * @param importError        Non-null on import failure; screen shows this as a snackbar.
     * @param appVersion         BuildConfig.VERSION_NAME displayed in the About section.
     * @param geminiKeySet       True if BuildConfig.GEMINI_API_KEY is non-blank.
     */
    data class Content(
        val theme: String = "system",
        val fontScaleIndex: Int = 1,
        val reduceMotion: Boolean = false,
        val audioSpeedIndex: Int = 2,
        val showPinyinDefault: Boolean = true,
        val dailyNewCardsLimit: Int = 10,
        val isExporting: Boolean = false,
        val isImporting: Boolean = false,
        val isResetting: Boolean = false,
        val exportError: String? = null,
        val importError: String? = null,
        val appVersion: String = "",
        val geminiKeySet: Boolean = false,
    ) : SettingsUiState()
}

// ---- Preference value tables ----

/** Font scale multipliers for the 4 discrete steps (Small / Medium / Large / Extra-large). */
val FONT_SCALE_STEPS = listOf(0.9f, 1.0f, 1.15f, 1.3f)

/** Audio speed options corresponding to chip indices 0–3. */
val AUDIO_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f)

/** Returns the multiplier for [index], defaulting to 1.0 when out of range. */
fun fontScaleForIndex(index: Int): Float = FONT_SCALE_STEPS.getOrElse(index) { 1.0f }

/** Returns the speed float for [index], defaulting to 1.0 when out of range. */
fun audioSpeedForIndex(index: Int): Float = AUDIO_SPEEDS.getOrElse(index) { 1.0f }

/** Finds the closest font-scale index for a stored [scale] float. */
fun indexForFontScale(scale: Float): Int =
    FONT_SCALE_STEPS.indexOfFirst { kotlin.math.abs(it - scale) < 0.05f }.takeIf { it >= 0 } ?: 1

/** Finds the closest audio-speed index for a stored [speed] float. */
fun indexForAudioSpeed(speed: Float): Int =
    AUDIO_SPEEDS.indexOfFirst { kotlin.math.abs(it - speed) < 0.05f }.takeIf { it >= 0 } ?: 2
