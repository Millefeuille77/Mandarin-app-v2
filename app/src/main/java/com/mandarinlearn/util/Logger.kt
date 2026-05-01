// Logger.kt — Mandarin Learn
// Thin wrapper around android.util.Log.
// Centralises logging so it can be silenced in tests or redirected to Crashlytics later.

package com.mandarinlearn.util

import android.util.Log

/**
 * App-wide logging facade.
 * In debug builds, all levels are emitted. In release builds, verbose/debug are stripped
 * by ProGuard (add the corresponding rules to proguard-rules.pro in Phase 10).
 */
object Logger {

    fun v(tag: String, message: String) = Log.v(tag, message)

    fun d(tag: String, message: String) = Log.d(tag, message)

    fun i(tag: String, message: String) = Log.i(tag, message)

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}
