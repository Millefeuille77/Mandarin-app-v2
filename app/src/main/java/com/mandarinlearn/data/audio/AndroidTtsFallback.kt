// AndroidTtsFallback.kt — Mandarin Learn
// Wraps Android's built-in TextToSpeech engine with Locale.SIMPLIFIED_CHINESE.
// Per ARCHITECTURE.md §4.6 — final fallback in the audio chain.
// Phase 5: full implementation.

package com.mandarinlearn.data.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

private const val TAG = "AndroidTtsFallback"
private const val UTTERANCE_ID = "mandarin_learn_tts"

/**
 * On-device Text-to-Speech engine wrapper for Simplified Chinese.
 *
 * Initialisation is async — [isAvailable] is false until the engine confirms
 * [TextToSpeech.LANG_AVAILABLE] for zh-CN. This class handles the init callback internally.
 *
 * [speak] suspends until the utterance completes or fails.
 *
 * Do NOT cache the output of this class — on-device synthesis varies per engine and device.
 * Per ARCHITECTURE.md §4.6 step 5: "Do NOT insert into audio_cache."
 */
class AndroidTtsFallback(context: Context) {

    private var tts: TextToSpeech? = null
    private var initStatus: Int = TextToSpeech.ERROR

    // ApplicationContext to avoid leaking Activity references
    private val appContext = context.applicationContext

    init {
        // Initialise the TTS engine asynchronously on construction.
        // The engine may not be ready until the first speak() call.
        tts = TextToSpeech(appContext) { status ->
            initStatus = status
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.w(TAG, "Simplified Chinese TTS voice not installed on this device")
                } else {
                    Logger.d(TAG, "AndroidTtsFallback initialised — zh-CN available")
                }
            } else {
                Logger.w(TAG, "TextToSpeech engine initialisation failed with status=$status")
            }
        }
    }

    /**
     * Returns true iff the TTS engine is initialised and Simplified Chinese is available.
     * A false result means speak() will not produce audio.
     */
    fun isAvailable(): Boolean {
        if (initStatus != TextToSpeech.SUCCESS) return false
        val result = tts?.setLanguage(Locale.SIMPLIFIED_CHINESE) ?: return false
        return result != TextToSpeech.LANG_MISSING_DATA &&
            result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /**
     * Speaks [text] using the on-device engine and suspends until the utterance completes.
     *
     * @param text  Chinese text to speak.
     * @param speed Playback rate — mapped directly to [TextToSpeech.setSpeechRate].
     * @throws IllegalStateException if [isAvailable] returns false.
     */
    suspend fun speak(text: String, speed: Float = 1.0f) {
        check(isAvailable()) { "AndroidTtsFallback: TTS engine or zh-CN voice not available" }

        val engine = tts ?: error("AndroidTtsFallback: TTS engine is null")
        engine.setSpeechRate(speed)

        suspendCancellableCoroutine<Unit> { continuation ->
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { /* no-op */ }

                override fun onDone(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                @Deprecated("Deprecated in API 21, still required until API 36+")
                override fun onError(utteranceId: String?) {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Logger.w(TAG, "TTS utterance error code=$errorCode for id=$utteranceId")
                    if (continuation.isActive) continuation.resume(Unit)
                }
            })

            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
            if (result == TextToSpeech.ERROR) {
                Logger.e(TAG, "TTS speak() returned ERROR for text='${text.take(20)}'")
                if (continuation.isActive) continuation.resume(Unit)
            }

            // Cancel the utterance if the coroutine is cancelled
            continuation.invokeOnCancellation {
                engine.stop()
            }
        }
    }

    /**
     * Releases the TTS engine resources.
     * Call when the owning component is destroyed (e.g. Application.onTerminate).
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
