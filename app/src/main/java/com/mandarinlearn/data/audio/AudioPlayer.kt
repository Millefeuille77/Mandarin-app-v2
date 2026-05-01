// AudioPlayer.kt — Mandarin Learn
// MediaPlayer wrapper for playing cached audio bytes.
// Used by AudioRepository to play cache-hit audio from the audio_cache table.
// Per ARCHITECTURE.md §4.6 step 3 (cache hit → play via MediaPlayer).

package com.mandarinlearn.data.audio

import android.media.MediaPlayer
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "AudioPlayer"

/**
 * Stateless helper that plays raw audio bytes via [MediaPlayer].
 *
 * The bytes are written to a temp file so [MediaPlayer.setDataSource] can reference them.
 * The temp file is cleaned up after playback or on error.
 */
object AudioPlayer {

    /**
     * Plays [audioBytes] and suspends until playback completes or an error occurs.
     *
     * @param audioBytes Raw MP3/WAV bytes to play.
     * @param cacheDir   Directory for the temporary file (e.g. context.cacheDir).
     * @throws Exception if MediaPlayer fails to prepare or play.
     */
    suspend fun play(audioBytes: ByteArray, cacheDir: File) {
        // Write bytes to a temp file — MediaPlayer requires a file path or URI
        val tempFile = File.createTempFile("audio_", ".mp3", cacheDir)
        try {
            FileOutputStream(tempFile).use { it.write(audioBytes) }
            playFile(tempFile)
        } finally {
            // Always clean up the temp file regardless of success or error
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private suspend fun playFile(file: File) = suspendCancellableCoroutine<Unit> { continuation ->
        val player = MediaPlayer()
        try {
            player.setDataSource(file.absolutePath)
            player.setOnCompletionListener {
                player.release()
                if (continuation.isActive) continuation.resume(Unit)
            }
            player.setOnErrorListener { _, what, extra ->
                player.release()
                Logger.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IllegalStateException("MediaPlayer error: what=$what extra=$extra")
                    )
                }
                true
            }
            player.prepare()
            player.start()

            continuation.invokeOnCancellation {
                player.stop()
                player.release()
            }
        } catch (e: Exception) {
            player.release()
            Logger.e(TAG, "Failed to prepare MediaPlayer", e)
            if (continuation.isActive) continuation.resumeWithException(e)
        }
    }
}
