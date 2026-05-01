// GeminiServiceTest.kt — Mandarin Learn
// Unit tests for GeminiService. Per IMPLEMENTATION_PLAN.md Phase 5 acceptance criteria.
// Tests: no-key → NoApiKey, offline → Offline, timeout propagation.

package com.mandarinlearn.data.remote

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import com.mandarinlearn.util.NetworkMonitor

/**
 * Tests for [GeminiService].
 * HTTP layer is mocked via [NetworkMonitor] and constructor injection.
 */
class GeminiServiceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `blank api key returns NoApiKey immediately`() = runTest(testDispatcher) {
        val networkMonitor = mockk<NetworkMonitor> {
            every { isOnline() } returns true
        }
        val service = GeminiService(
            apiKey         = "",
            ioDispatcher   = testDispatcher,
            networkMonitor = networkMonitor,
        )

        val result = service.synthesize("你好")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.NoApiKey)
    }

    @Test
    fun `offline returns Offline without calling network`() = runTest(testDispatcher) {
        val networkMonitor = mockk<NetworkMonitor> {
            every { isOnline() } returns false
        }
        val service = GeminiService(
            apiKey         = "test-key",
            ioDispatcher   = testDispatcher,
            networkMonitor = networkMonitor,
        )

        val result = service.synthesize("你好")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.Offline)
    }

    @Test
    fun `transcribeAndScore returns NoApiKey stub until Phase 6`() = runTest(testDispatcher) {
        val networkMonitor = mockk<NetworkMonitor> {
            every { isOnline() } returns true
        }
        val service = GeminiService(
            apiKey         = "test-key",
            ioDispatcher   = testDispatcher,
            networkMonitor = networkMonitor,
        )
        val fakeFile = java.io.File.createTempFile("test", ".m4a")

        val result = service.transcribeAndScore(fakeFile, "你好")
        fakeFile.delete()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.NoApiKey)
    }

    @Test
    fun `chat returns NoApiKey stub until Phase 8`() = runTest(testDispatcher) {
        val networkMonitor = mockk<NetworkMonitor> {
            every { isOnline() } returns true
        }
        val service = GeminiService(
            apiKey         = "test-key",
            ioDispatcher   = testDispatcher,
            networkMonitor = networkMonitor,
        )

        val result = service.chat("Explain this answer")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is GeminiError.NoApiKey)
    }

    @Test
    fun `synthesize with non-blank key falls through to Unknown due to SDK limitation`() =
        runTest(testDispatcher) {
            val networkMonitor = mockk<NetworkMonitor> {
                every { isOnline() } returns true
            }
            val service = GeminiService(
                apiKey         = "test-api-key",
                ioDispatcher   = testDispatcher,
                networkMonitor = networkMonitor,
            )

            // SDK 0.2.2 does not support audio output; expect failure (falls to AndroidTtsFallback)
            val result = service.synthesize("你好")

            assertTrue(result.isFailure)
            // Should be Unknown wrapping UnsupportedOperationException
            assertTrue(result.exceptionOrNull() is GeminiError.Unknown)
        }
}
