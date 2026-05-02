// SettingsRepository.kt — Mandarin Learn
// DataStore-backed settings repository for Phase 9 (Settings & Polish).
// Wraps UserPreferencesRepository and exposes a clean interface for SettingsViewModel.
// ARCHITECTURE.md §1 datastore = "1.0.0".

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Settings repository providing a single access point for all user preferences.
 * Delegates to [UserPreferencesRepository] for actual DataStore reads/writes.
 * Thin wrapper that allows SettingsViewModel to be injected cleanly.
 */
class SettingsRepository(
    private val preferencesRepository: UserPreferencesRepository,
) {

    // ---- Read flows ----

    /** Emits "system" | "light" | "dark". Default "system". */
    val theme: Flow<String> = preferencesRepository.theme

    /** Font scale multiplier (0.8f–1.6f). Default 1.0f. */
    val fontScale: Flow<Float> = preferencesRepository.fontScale

    /** Whether to skip card-flip animation. Default false. */
    val reduceMotion: Flow<Boolean> = preferencesRepository.reduceMotion

    /** TTS audio playback speed (0.5f–1.25f). Default 0.85f (slower for learners). */
    val audioSpeed: Flow<Float> = preferencesRepository.audioSpeed

    /** Whether pinyin is shown by default in reading passages. Default true. */
    val showPinyin: Flow<Boolean> = preferencesRepository.showPinyin

    /** Max new flashcards per session (5–50). Default 10. */
    val dailyNewCardsLimit: Flow<Int> = preferencesRepository.dailyNewCardsLimit

    // ---- Write operations ----

    suspend fun setTheme(theme: String) =
        preferencesRepository.setTheme(theme)

    suspend fun setFontScale(scale: Float) =
        preferencesRepository.setFontScale(scale)

    suspend fun setReduceMotion(reduce: Boolean) =
        preferencesRepository.setReduceMotion(reduce)

    suspend fun setAudioSpeed(speed: Float) =
        preferencesRepository.setAudioSpeed(speed)

    suspend fun setShowPinyin(show: Boolean) =
        preferencesRepository.setShowPinyin(show)

    suspend fun setDailyNewCardsLimit(limit: Int) =
        preferencesRepository.setDailyNewCardsLimit(limit)
}
