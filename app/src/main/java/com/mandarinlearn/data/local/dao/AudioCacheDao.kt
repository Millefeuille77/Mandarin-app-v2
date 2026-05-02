// AudioCacheDao.kt — Mandarin Learn
// DAO for the audio_cache table. Per ARCHITECTURE.md §2.2.
// Eviction: LRU — evictOldest(N) deletes the N least-recently-used rows; AudioRepository loops it.

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
     * Deletes the [limit] least-recently-used rows. Used by AudioRepository's eviction loop
     * to bring total cache size below the target.
     *
     * Why a fixed limit instead of size-based eviction in SQL: Room's SQL parser does not
     * support window functions (`SUM(...) OVER (...)`), so the running-total trick that would
     * let us evict-until-fit in a single statement is not available. Instead, the repository
     * loops: check totalBytes → evictOldest(N) → re-check → repeat until under the cap.
     */
    @Query(
        """
        DELETE FROM audio_cache
        WHERE cache_key IN (
            SELECT cache_key FROM audio_cache
            ORDER BY last_used_at ASC
            LIMIT :limit
        )
        """
    )
    suspend fun evictOldest(limit: Int)
}
