// AudioCacheDaoTest.kt — Mandarin Learn
// Instrumented tests for AudioCacheDao. Per IMPLEMENTATION_PLAN.md Phase 5.
// Tests: insert, cache hit, cache miss, touch (LRU update), totalBytes, eviction.

package com.mandarinlearn.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mandarinlearn.data.local.dao.AudioCacheDao
import com.mandarinlearn.data.local.entity.AudioCacheEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [AudioCacheDao].
 * Uses an in-memory Room database for isolation and speed.
 */
@RunWith(AndroidJUnit4::class)
class AudioCacheDaoTest {

    private lateinit var db: MandarinLearnDatabase
    private lateinit var dao: AudioCacheDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MandarinLearnDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.audioCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeEntity(key: String, bytes: Int = 1024): AudioCacheEntity =
        AudioCacheEntity(
            cacheKey   = key,
            text       = "text_$key",
            voice      = "cmn-CN-Female-1",
            speed      = 1.0,
            audioBytes = ByteArray(bytes) { it.toByte() },
            mimeType   = "audio/mpeg",
            createdAt  = System.currentTimeMillis(),
            lastUsedAt = System.currentTimeMillis(),
            byteSize   = bytes.toLong(),
        )

    @Test
    fun insertAndGet_returnsEntity() = runTest {
        val entity = makeEntity("key1")
        dao.insert(entity)

        val result = dao.get("key1")
        assertNotNull(result)
        assertEquals("key1", result?.cacheKey)
    }

    @Test
    fun get_returnsNull_onCacheMiss() = runTest {
        val result = dao.get("nonexistent_key")
        assertNull(result)
    }

    @Test
    fun totalBytes_sumsAllEntries() = runTest {
        dao.insert(makeEntity("k1", bytes = 1000))
        dao.insert(makeEntity("k2", bytes = 2000))

        val total = dao.totalBytes()
        assertEquals(3000L, total)
    }

    @Test
    fun totalBytes_returnsZero_whenEmpty() = runTest {
        val total = dao.totalBytes()
        assertEquals(0L, total)
    }

    @Test
    fun touch_updatesLastUsedAt() = runTest {
        val entity = makeEntity("key_touch").copy(lastUsedAt = 1000L)
        dao.insert(entity)

        val newTime = 9_999_999L
        dao.touch("key_touch", newTime)

        val updated = dao.get("key_touch")
        assertEquals(newTime, updated?.lastUsedAt)
    }

    @Test
    fun evictOldest_removesNLeastRecentlyUsedEntries() = runTest {
        // Insert 3 entries with different lastUsedAt timestamps
        dao.insert(makeEntity("oldest", 1000).copy(lastUsedAt = 100L))
        dao.insert(makeEntity("middle", 1000).copy(lastUsedAt = 200L))
        dao.insert(makeEntity("newest", 1000).copy(lastUsedAt = 300L))

        assertEquals(3000L, dao.totalBytes())

        // Evict the single oldest row.
        dao.evictOldest(1)

        assertNull(dao.get("oldest"))
        assertNotNull(dao.get("middle"))
        assertNotNull(dao.get("newest"))
    }

    @Test
    fun insert_replace_overwritesExistingKey() = runTest {
        dao.insert(makeEntity("dup_key", bytes = 500))
        dao.insert(makeEntity("dup_key", bytes = 1500)) // Replace

        val result = dao.get("dup_key")
        assertEquals(1500L, result?.byteSize)
        assertEquals(1500L, dao.totalBytes()) // Only one entry
    }
}
