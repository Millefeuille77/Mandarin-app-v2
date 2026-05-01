// AudioCacheEntity.kt — Mandarin Learn
// Room entity for the `audio_cache` table. Per ARCHITECTURE.md §2.1.
// Caches Gemini TTS outputs as raw bytes to minimise API calls.
// Eviction policy: LRU when totalBytes() > 50 MB (run on app start).

package com.mandarinlearn.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One cached audio clip.
 * [cacheKey] = sha256("text|voice|speed") — see HashUtil.kt.
 * [audioBytes] stores raw MP3/WAV bytes from Gemini TTS.
 * [byteSize] mirrors audioBytes.size for fast aggregate queries (avoids loading BLOBs for eviction).
 */
@Entity(tableName = "audio_cache")
data class AudioCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val cacheKey: String,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "voice")
    val voice: String,

    @ColumnInfo(name = "speed")
    val speed: Double,

    @ColumnInfo(name = "audio_bytes", typeAffinity = ColumnInfo.BLOB)
    val audioBytes: ByteArray,

    // "audio/mpeg" or "audio/wav"
    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    // epoch millis
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    // epoch millis — updated on every cache hit to support LRU eviction
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long,

    // mirrors audioBytes.size — used by eviction queries without loading BLOBs
    @ColumnInfo(name = "byte_size")
    val byteSize: Long,
) {
    // ByteArray equals/hashCode fix — required for correct data class behaviour
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioCacheEntity) return false
        return cacheKey == other.cacheKey &&
            text == other.text &&
            voice == other.voice &&
            speed == other.speed &&
            audioBytes.contentEquals(other.audioBytes) &&
            mimeType == other.mimeType &&
            createdAt == other.createdAt &&
            lastUsedAt == other.lastUsedAt &&
            byteSize == other.byteSize
    }

    override fun hashCode(): Int {
        var result = cacheKey.hashCode()
        result = 31 * result + audioBytes.contentHashCode()
        result = 31 * result + byteSize.hashCode()
        return result
    }
}
