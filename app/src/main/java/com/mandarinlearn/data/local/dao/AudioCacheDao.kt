// AudioCacheDao.kt — Mandarin Learn
// DAO for the audio_cache table. Per ARCHITECTURE.md §2.2.
// Eviction: LRU — evictLruUntil deletes rows starting from the least-recently-used.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.AudioCacheEntity

/**
 * Data Access Object for the TTS audio cache.
 * All operations are non-reactive (suspend only) because the audio pipeline is
 * synchronous at the point of lookup — the Flow wrapper is in AudioRepository.
 */
@Dao
interface AudioCacheDao {

    /** Looks up a cached audio clip by its SHA-256 cache key. Returns null on miss. */
    @Query("SELECT * FROM audio_cache WHERE cache_key = :key")
    suspend fun get(key: String): AudioCacheEntity?

    /** Inserts or replaces a cache entry (replace handles the rare re-generation case). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AudioCacheEntity)

    /** Updates last_used_at for LRU tracking after a cache hit. */
    @Query("UPDATE audio_cache SET last_used_at = :now WHERE cache_key = :key")
    suspend fun touch(key: String, now: Long)

    /** Sum of all byte_size values — used to check whether eviction is needed. */
    @Query("SELECT COALESCE(SUM(byte_size), 0) FROM audio_cache")
    suspend fun totalBytes(): Long

    /**
     * Deletes the least-recently-used rows until total byte_size <= [targetBytes].
     * Uses a subquery to identify LRU rows by last_used_at ASC.
     */
    @Query(
        """
        DELETE FROM audio_cache
        WHERE cache_key IN (
            SELECT cache_key FROM audio_cache
            ORDER BY last_used_at ASC
            LIMIT (
                SELECT COUNT(*) FROM audio_cache
            ) - (
                SELECT COUNT(*) FROM (
                    SELECT cache_key,
                           SUM(byte_size) OVER (ORDER BY last_used_at DESC) AS running_total
                    FROM audio_cache
                ) WHERE running_total <= :targetBytes
            )
        )
        """
    )
    suspend fun evictLruUntil(targetBytes: Long)
}
