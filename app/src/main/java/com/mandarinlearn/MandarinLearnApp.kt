// MandarinLearnApp.kt — Mandarin Learn
// Application subclass that owns the AppContainer (manual DI).
// Per ARCHITECTURE.md §6: no Hilt/Dagger — manual constructor injection via AppContainer.
// Phase 2 will populate AppContainer with DB, repositories, and JsonImporter.

package com.mandarinlearn

import android.app.Application
import com.mandarinlearn.di.AppContainer

/**
 * Application subclass. Lifecycle-scope for singletons (DB, repositories, GeminiService).
 * Registered in AndroidManifest.xml via android:name=".MandarinLearnApp".
 */
class MandarinLearnApp : Application() {

    /** Manual DI container — the single source of all singletons. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Phase 2: initialise Room DB, repositories, GeminiService inside AppContainer.
        container = AppContainer(this)
    }
}
