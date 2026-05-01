// UserPreferencesRepository.kt — Mandarin Learn
// DataStore-backed settings repository. Per ARCHITECTURE.md §1 (datastore = "1.0.0").
// All settings persisted across sessions without Room (DataStore is more suitable for kv data).

package com.mandarinlearn.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Top-level extension property creates a single DataStore instance per Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Reads and writes user preferences via DataStore Preferences.
 * Defaults are the app's recommended settings for 60-year-old users.
 */
class UserPreferencesRepository(private val context: Context) {

    // ---- Theme ----

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME] ?: "system"
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME] = theme }
    }

    // ---- Font scale ----

    val fontScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.FONT_SCALE] ?: 1.0f
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SCALE] = scale.coerceIn(0.8f, 1.6f) }
    }

    // ---- Audio speed ----

    val audioSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUDIO_SPEED] ?: 0.85f // slightly slower default for learners
    }

    suspend fun setAudioSpeed(speed: Float) {
        context.dataStore.edit { it[PreferencesKeys.AUDIO_SPEED] = speed.coerceIn(0.5f, 1.25f) }
    }

    // ---- Pinyin ----

    val showPinyin: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SHOW_PINYIN] ?: true // on by default for beginners
    }

    suspend fun setShowPinyin(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_PINYIN] = show }
    }

    // ---- Daily new cards limit ----

    val dailyNewCardsLimit: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DAILY_NEW_CARDS_LIMIT] ?: 10
    }

    suspend fun setDailyNewCardsLimit(limit: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.DAILY_NEW_CARDS_LIMIT] = limit.coerceIn(5, 30)
        }
    }

    // ---- Reduce motion ----

    val reduceMotion: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.REDUCE_MOTION] ?: false
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.REDUCE_MOTION] = reduce }
    }
}
