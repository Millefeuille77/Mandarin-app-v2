// PlayChineseAudioUseCase.kt — Mandarin Learn
// Use-case that delegates audio playback to AudioRepository.
// In Phase 4 the AudioRepository is still a stub (returns Failed).
// Phase 5 wires Gemini TTS → cache → AndroidTTS fallback.
// UX_SPECIFICATION.md §4 Screen 5 (PassageScreen "Play all" button).

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.repository.AudioPlaybackState
import com.mandarinlearn.data.repository.AudioRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use-case wrapping audio playback so ViewModels never depend on AudioRepository directly.
 * Returns a [Flow] of [AudioPlaybackState] so the UI can react to Loading → Playing → Finished.
 *
 * Phase 4: the underlying [AudioRepository] stub always emits [AudioPlaybackState.Failed].
 * The PassageScreen handles this gracefully by showing a snackbar and keeping the UI functional.
 *
 * @param audioRepository Repository that owns the cache → Gemini → TTS fallback chain.
 */
class PlayChineseAudioUseCase(
    private val audioRepository: AudioRepository,
) {
    /**
     * Requests playback of [text] at the given [speed].
     *
     * @param text  Chinese text to synthesise and play.
     * @param speed Playback speed multiplier (0.5–1.25). Default 1.0.
     */
    operator fun invoke(text: String, speed: Float = 1.0f): Flow<AudioPlaybackState> =
        audioRepository.play(text, speed)
}
