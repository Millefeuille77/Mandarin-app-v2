// AudioRepository.kt — Mandarin Learn
// Full Phase 5 implementation — replaces the Phase 2 stub.
// Implements the §4.6 fallback chain: cache → Gemini → AndroidTtsFallback.
// This is the ONLY caller of GeminiService.synthesize(). UI never calls GeminiService directly.

package com.mandarinlearn.data.repository

import android.content.Context
import com.mandarinlearn.data.audio.AndroidTtsFallback
import com.mandarinlearn.data.audio.AudioPlayer
import com.mandarinlearn.data.local.dao.AudioCacheDao
import com.mandarinlearn.data.local.entity.AudioCacheEntity
import com.mandarinlearn.data.remote.GeminiService
import com.mandarinlearn.util.HashUtil
import com.mandarinlearn.util.Logger
import com.mandarinlearn.util.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

private const val TAG = "AudioRepository"
private const val DEFAULT_VOICE = "cmn-CN-Female-1"  // Single voice today; key future-proofs multi-voice (ARCH §2.1).
private const val VOICE_DEFAULT = "cmn-CN-Female-1"
private const val CACHE_MAX_BYTES = 50L * 1024L * 1024L // 50 MB

/**
 * Repository for audio playback.
 * Full §4.6 implementation: cache → Gemini TTS → AndroidTtsFallback.
 *
 * This is the single entry point for audio in the app. ViewModels and screens
 * call [play] only — they never touch GeminiService or AndroidTtsFallback directly.
 */
class AudioRepository(
    private val gemini: GeminiService,
    private val audioCacheDao: AudioCacheDao,
    private val androidTts: AndroidTtsFallback,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher,
    private val context: Context,
) {

    /**
     * Plays [text] as Chinese audio and emits state changes.
     *
     * Implements §4.6 algorithm exactly:
     * 1. Emit Loading.
     * 2. Compute cacheKey = sha256("text|speed") — speed rounded to 2 dp.
     * 3. Cache hit → play via MediaPlayer, emit Playing(CACHE), Finished. Update last_used_at.
     * 4. Cache miss → check network.
     *    Online → Gemini.synthesize:
     *      Success → insert cache, play, emit Playing(GEMINI), Finished.
     *      Failure → fall through to step 5.
     *    Offline → fall through to step 5.
     * 5. AndroidTtsFallback.speak:
     *      Available → emit Playing(ANDROID_TTS), Finished. Do NOT cache.
     *      Unavailable → emit Failed(reason).
     *
     * @param text  Chinese text to speak.
     * @param speed Playback speed (0.5–1.25); passed to Gemini and AndroidTtsFallback.
     */
    fun play(text: String, speed: Float = 1.0f): Flow<AudioPlaybackState> = flow {
        // Step 1: Signal loading
        emit(AudioPlaybackState.Loading)

        if (text.isBlank()) {
            emit(AudioPlaybackState.Failed("No text to play"))
            return@flow
        }

        withContext(ioDispatcher) {
            // Step 2: Compute cache key — 3-field per ARCHITECTURE §2.1 / §4.6 (text|voice|speed).
            // Single voice today; the explicit voice field future-proofs against multi-voice.
            val cacheKey = HashUtil.audioCacheKey(text, DEFAULT_VOICE, speed)

            // Step 3: Cache lookup
            val cached = try {
                audioCacheDao.get(cacheKey)
            } catch (e: Exception) {
                Logger.w(TAG, "Cache lookup failed, treating as miss", e)
                null
            }

            if (cached != null) {
                // Cache hit — update LRU timestamp, play, done
                try {
                    audioCacheDao.touch(cacheKey, System.currentTimeMillis())
                    AudioPlayer.play(cached.audioBytes, context.cacheDir)
                    emit(AudioPlaybackState.Playing(AudioPlaybackState.Source.CACHE))
                    emit(AudioPlaybackState.Finished)
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to play cached audio", e)
                    // Cache hit but playback failed — fall through to TTS
                    playWithTtsFallback(text, speed)
                }
                return@withContext
            }

            // Step 4: Cache miss — check network
            if (networkMonitor.isOnline()) {
                // Step 4a: Try Gemini TTS
                val geminiResult = try {
                    gemini.synthesize(text, speed)
                } catch (e: Exception) {
                    Result.failure(e)
                }

                if (geminiResult.isSuccess) {
                    val blob = geminiResult.getOrNull()
                    if (blob != null) {
                        // Insert into cache before playing
                        try {
                            evictCacheIfNeeded()
                            audioCacheDao.insert(
                                AudioCacheEntity(
                                    cacheKey  = cacheKey,
                                    text      = text,
                                    voice     = VOICE_DEFAULT,
                                    speed     = speed.toDouble(),
                                    audioBytes = blob.bytes,
                                    mimeType  = blob.mimeType,
                                    createdAt = System.currentTimeMillis(),
                                    lastUsedAt = System.currentTimeMillis(),
                                    byteSize  = blob.bytes.size.toLong(),
                                )
                            )
                        } catch (e: Exception) {
                            Logger.w(TAG, "Failed to cache Gemini audio — playing without caching", e)
                        }

                        try {
                            AudioPlayer.play(blob.bytes, context.cacheDir)
                            emit(AudioPlaybackState.Playing(AudioPlaybackState.Source.GEMINI))
                            emit(AudioPlaybackState.Finished)
                        } catch (e: Exception) {
                            Logger.e(TAG, "Failed to play Gemini audio", e)
                            playWithTtsFallback(text, speed)
                        }
                        return@withContext
                    }
                }
                // Gemini failure — log and fall through
                Logger.d(TAG, "Gemini synthesize failed: ${geminiResult.exceptionOrNull()?.message}")
            }

            // Step 5: AndroidTtsFallback (reached when offline or Gemini failed)
            playWithTtsFallback(text, speed)
        }
    }

    /**
     * Runs the AndroidTtsFallback path and emits the appropriate state.
     * Extracted to avoid code duplication between the "Gemini failure" and "offline" paths.
     */
    private suspend fun Flow<AudioPlaybackState>.playWithTtsFallback(
        text: String,
        speed: Float,
    ) {
        if (!androidTts.isAvailable()) {
            emit(
                AudioPlaybackState.Failed(
                    "Audio not available — please install Chinese TTS voice in Android Settings"
                )
            )
            return
        }
        try {
            androidTts.speak(text, speed)
            emit(AudioPlaybackState.Playing(AudioPlaybackState.Source.ANDROID_TTS))
            emit(AudioPlaybackState.Finished)
        } catch (e: Exception) {
            Logger.e(TAG, "AndroidTtsFallback.speak failed", e)
            emit(
                AudioPlaybackState.Failed(
                    "Audio not available — please install Chinese TTS voice in Android Settings"
                )
            )
        }
    }

    /**
     * Evicts LRU cache entries if total byte usage exceeds 50 MB.
     * Called before inserting a new cache entry (ARCHITECTURE.md §2.1 eviction policy).
     */
    private suspend fun evictCacheIfNeeded() {
        try {
            val totalBytes = audioCacheDao.totalBytes()
            if (totalBytes > CACHE_MAX_BYTES) {
                // Target: bring down to 80% of max to avoid thrashing
                val target = (CACHE_MAX_BYTES * 0.8).toLong()
                audioCacheDao.evictLruUntil(target)
                Logger.d(TAG, "Evicted LRU cache entries: ${totalBytes / 1024 / 1024}MB → target ${target / 1024 / 1024}MB")
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Cache eviction check failed", e)
        }
    }
}

/**
 * State machine for audio playback progress.
 * Emitted as a [kotlinx.coroutines.flow.Flow] by [AudioRepository.play].
 */
sealed class AudioPlaybackState {
    data object Loading : AudioPlaybackState()
    data class Playing(val source: Source) : AudioPlaybackState()
    data object Finished : AudioPlaybackState()
    data class Failed(val reason: String) : AudioPlaybackState()

    enum class Source { CACHE, GEMINI, ANDROID_TTS }
}
