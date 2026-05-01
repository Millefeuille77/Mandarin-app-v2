// PreferencesKeys.kt — Mandarin Learn
// DataStore preference keys. Per ARCHITECTURE.md §1 (datastore = "1.0.0").
// UserPreferencesRepository reads/writes these keys.

package com.mandarinlearn.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Strongly-typed DataStore preference keys for all user settings.
 * Adding a key here is the only change needed when a new setting is introduced.
 */
object PreferencesKeys {

    /** "system" | "light" | "dark" */
    val THEME = stringPreferencesKey("theme")

    /** Font scale multiplier (0.8f..1.6f) — multiplied in MandarinLearnTheme. */
    val FONT_SCALE = floatPreferencesKey("font_scale")

    /** Playback speed for TTS audio (0.5f..1.25f). */
    val AUDIO_SPEED = floatPreferencesKey("audio_speed")

    /** Whether pinyin is shown by default in reading passages. */
    val SHOW_PINYIN = booleanPreferencesKey("show_pinyin")

    /** Max new flashcards shown per session (5–30). */
    val DAILY_NEW_CARDS_LIMIT = intPreferencesKey("daily_new_cards_limit")

    /** Whether to use reduced motion (skips card flip animation). */
    val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
}
