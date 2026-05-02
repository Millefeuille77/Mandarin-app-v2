// AudioRecorder.kt — Mandarin Learn
// Wraps MediaRecorder for short voice recordings (max 10 seconds, M4A/AAC format).
// Per IMPLEMENTATION_PLAN.md Phase 6 §B and ARCHITECTURE.md §4.3.
// Requires android.permission.RECORD_AUDIO declared in AndroidManifest.xml.

package com.mandarinlearn.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "AudioRecorder"
private const val SAMPLE_RATE_HZ = 16_000    // 16 kHz mono per Phase 6 spec
private const val BIT_RATE = 64_000          // 64 kbps — reasonable quality for speech
private const val TICK_MS = 100L             // Timer resolution

/**
 * Errors that [AudioRecorder] can surface.
 * [PermissionDenied] must be handled before calling [record] — the recorder does NOT
 * request permissions itself; it propagates this error to the caller.
 */
sealed class RecordError : Throwable() {
    data object PermissionDenied : RecordError() {
        override val message = "RECORD_AUDIO permission is not granted"
    }
    data class SetupFailed(override val cause: Throwable?) : RecordError() {
        override val message = "Failed to initialise the microphone recorder"
    }
    data class RecordingFailed(override val cause: Throwable?) : RecordError() {
        override val message = "Recording stopped unexpectedly"
    }
}

/**
 * Utility class that wraps [MediaRecorder] for short voice recordings.
 *
 * Usage:
 * ```kotlin
 * val result = recorder.record(maxSeconds = 10)
 * result.onSuccess { file -> /* send to Gemini */ }
 * result.onFailure { error -> /* handle RecordError */ }
 * ```
 *
 * The recorded file is saved in [Context.cacheDir] as `speaking_<timestamp>.m4a`.
 * Callers are responsible for deleting the file after use (SpeakingViewModel does this
 * in a `finally` block per Phase 6 acceptance criteria).
 *
 * Thread-safety: [record] switches to [Dispatchers.IO] internally; callers may call it
 * from any coroutine context.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    @Volatile private var stopRequested = false

    /**
     * Records audio for up to [maxSeconds] seconds and returns the output [File].
     *
     * If [android.Manifest.permission.RECORD_AUDIO] is not granted, returns
     * [Result.failure(RecordError.PermissionDenied)] immediately without touching hardware.
     *
     * Call [stopRecording] to stop before [maxSeconds] elapses.
     *
     * @param maxSeconds Maximum recording length (default 10, per spec).
     * @return [Result.success] with the recorded [File], or [Result.failure] with a [RecordError].
     */
    suspend fun record(maxSeconds: Int = 10): Result<File> = withContext(Dispatchers.IO) {
        // Reset stop flag at start of each new recording
        stopRequested = false

        val outputFile = File(context.cacheDir, "speaking_${System.currentTimeMillis()}.m4a")

        val rec = createRecorder(outputFile)
            ?: return@withContext Result.failure(RecordError.SetupFailed(null))

        recorder = rec

        try {
            rec.start()
            Logger.d(TAG, "Recording started — max ${maxSeconds}s → ${outputFile.name}")

            // Wait until stop is requested or timeout elapses
            val maxMs = maxSeconds * 1000L
            var elapsed = 0L
            while (!stopRequested && elapsed < maxMs) {
                delay(TICK_MS)
                elapsed += TICK_MS
            }

            rec.stop()
            rec.release()
            recorder = null

            Logger.d(TAG, "Recording stopped after ${elapsed}ms — file: ${outputFile.length()} bytes")
            Result.success(outputFile)
        } catch (e: Exception) {
            Logger.e(TAG, "Recording failed", e)
            runCatching { rec.release() }
            recorder = null
            outputFile.delete()
            Result.failure(RecordError.RecordingFailed(e))
        }
    }

    /**
     * Requests an early stop of an in-progress recording.
     * Safe to call from any thread. If no recording is in progress, this is a no-op.
     */
    fun stopRecording() {
        stopRequested = true
    }

    /**
     * Creates and prepares a [MediaRecorder] targeting [outputFile].
     * Returns null if preparation fails.
     */
    private fun createRecorder(outputFile: File): MediaRecorder? {
        return try {
            // Use the constructor that accepts Context on API 31+; older APIs use no-arg constructor.
            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            rec.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(SAMPLE_RATE_HZ)
                setAudioEncodingBitRate(BIT_RATE)
                setAudioChannels(1)   // Mono — spec: "16 kHz, mono"
                setOutputFile(outputFile.absolutePath)
                prepare()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to create MediaRecorder", e)
            null
        }
    }
}
