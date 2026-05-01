// DispatcherProvider.kt — Mandarin Learn
// Abstraction over Kotlin coroutine dispatchers for testability.
// Injected into repositories; tests substitute a TestDispatcher implementation.

package com.mandarinlearn.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Interface allowing tests to substitute real dispatchers with [kotlinx.coroutines.test.TestCoroutineDispatcher].
 * Production code uses [DefaultDispatcherProvider]; tests inject [TestDispatcherProvider].
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

/**
 * Production implementation that delegates to the standard [Dispatchers] singletons.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
