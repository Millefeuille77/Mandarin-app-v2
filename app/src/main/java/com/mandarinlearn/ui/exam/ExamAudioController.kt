// ExamAudioController.kt — Mandarin Learn
// Extracted from ExamViewModel to keep that file under the 300-line cap (Phase 7 QA M-1 preventive)
// and to wire AudioRepository.play() into the listening-section flow (Phase 7 QA M-2 fix).
// Per IMPLEMENTATION_PLAN.md Phase 7: "All ListeningScreen audio in exam-listening mode reuses
// AudioRepository.play(text) from Phase 5."

package com.mandarinlearn.ui.exam

import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "ExamAudioController"
private const val MAX_REPLAYS = 2  // matches Phase 5 ListeningViewModel cap

/**
 * Owns audio playback for the listening section of an exam.
 *
 * Lifecycle: one instance per exam session, scoped to [ExamViewModel]'s viewModelScope via [scope].
 * The controller does NOT cache state across exam sessions — a fresh instance per exam keeps replay
 * counts isolated.
 *
 * Why this lives outside ExamViewModel: ExamViewModel was at 296 lines (Phase 7 QA M-1) and adding
 * audio inline would have pushed it over the 300-line cap. Extraction also lets AudioRepository
 * stay an injected boundary that ExamViewModel doesn't have to know about.
 */
class ExamAudioController(
    private val audioRepository: AudioRepository,
    private val scope: CoroutineScope,
) {

    private var currentJob: Job? = null
    private var lastPlayedQuestionId: String? = null
    private var replayCount: Int = 0

    /**
     * Auto-play the audio for [question] iff it is a listening question with audio_text_chinese set.
     * Idempotent per question id — calling twice for the same question doesn't replay (use [replay]).
     * Resets the replay counter for the new question.
     */
    fun playFor(question: SampleQuestion) {
        if (!question.isListening()) return
        val audioText = question.audioTextChinese?.takeIf { it.isNotBlank() } ?: return
        if (lastPlayedQuestionId == question.id) return  // already auto-played this one

        lastPlayedQuestionId = question.id
        replayCount = 0
        startPlayback(audioText)
    }

    /**
     * User-triggered replay. Capped at [MAX_REPLAYS] per question.
     * Returns the number of replays remaining (so the UI can disable the button at 0).
     */
    fun replay(): Int {
        val text = currentAudioText() ?: return remainingReplays()
        if (replayCount >= MAX_REPLAYS) return 0
        replayCount++
        startPlayback(text)
        return remainingReplays()
    }

    fun remainingReplays(): Int = (MAX_REPLAYS - replayCount).coerceAtLeast(0)

    fun stop() {
        currentJob?.cancel()
        currentJob = null
    }

    private var currentAudioText: String? = null

    private fun currentAudioText(): String? = currentAudioText

    private fun startPlayback(text: String) {
        currentAudioText = text
        currentJob?.cancel()
        currentJob = scope.launch {
            try {
                audioRepository.play(text).collect { /* state changes drive UI elsewhere */ }
            } catch (t: Throwable) {
                Logger.w(TAG, "Audio playback failed for question (will not crash exam)", t)
            }
        }
    }

    /**
     * SampleQuestion doesn't carry section name; the caller wraps it. We assume the caller only
     * invokes [playFor] for listening-section questions (ExamViewModel checks the section first),
     * but defensively also check for the audio_text field which only listening questions populate.
     */
    private fun SampleQuestion.isListening(): Boolean =
        !audioTextChinese.isNullOrBlank()
}
