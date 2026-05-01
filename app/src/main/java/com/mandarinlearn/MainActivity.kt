// MainActivity.kt — Mandarin Learn
// Entry point for the application. Hosts the Compose content and splash screen.
// Phase 2: passes JsonImporter from AppContainer to AppNavigation for import gating.

package com.mandarinlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mandarinlearn.navigation.AppNavigation
import com.mandarinlearn.ui.theme.MandarinLearnTheme

/**
 * Single activity for the app. All navigation happens via Compose NavHost.
 * Portrait orientation is locked in AndroidManifest.xml (ARCHITECTURE.md §1).
 *
 * Phase 2: AppContainer provides the JsonImporter to AppNavigation so the
 * import gate (ImportLoadingScreen) works on first launch.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate to avoid flash of white.
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MandarinLearnApp).container

        setContent {
            MandarinLearnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation(
                        jsonImporter  = container.jsonImporter,
                        appContainer  = container,
                    )
                }
            }
        }
    }
}
