// MeNavRoutes.kt — Mandarin Learn
// Phase 9 QA blocker fix: extracted Me-tab routes (Progress + Settings) from AppNavigation.kt
// to keep AppNavigation.kt under the 300-line cap.
// Adding new routes? Put feature-specific routes in their own NavGraphBuilder extension file
// in this package, following the same pattern.

package com.mandarinlearn.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mandarinlearn.BuildConfig
import com.mandarinlearn.di.AppContainer
import com.mandarinlearn.ui.progress.ProgressScreen
import com.mandarinlearn.ui.progress.ProgressViewModel
import com.mandarinlearn.ui.settings.SettingsScreen
import com.mandarinlearn.ui.settings.SettingsViewModel

/**
 * Adds the Me-tab child routes (Progress, Settings) to a NavGraphBuilder.
 * Extracted from [AppNavigation] to honour the 300-line file cap (Phase 9 QA blocker fix).
 *
 * @param navController root NavController used for back navigation and exam-result deep links.
 * @param appContainer  manual-DI container; in production never null. Preview/test paths show
 *                       no content rather than a misleading placeholder.
 */
internal fun NavGraphBuilder.meNavRoutes(
    navController: NavHostController,
    appContainer: AppContainer?,
) {
    composable(Routes.PROGRESS) {
        if (appContainer != null) {
            val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(
                appContainer.streakRepository, appContainer.vocabularyRepository,
                appContainer.progressRepository, appContainer.examRepository))
            ProgressScreen(
                viewModel             = vm,
                onNavigateBack        = { navController.popBackStack() },
                onNavigateToExamResult = { id -> navController.navigate(Routes.examResult(id)) },
            )
        }
    }

    composable(Routes.SETTINGS) {
        if (appContainer != null) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(
                    settingsRepository = appContainer.settingsRepository,
                    exportUseCase      = appContainer.exportProgressUseCase,
                    importUseCase      = appContainer.importProgressUseCase,
                    resetUseCase       = appContainer.resetProgressUseCase,
                    appVersion         = BuildConfig.VERSION_NAME,
                    geminiKeySet       = BuildConfig.GEMINI_API_KEY.isNotBlank(),
                )
            )
            SettingsScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
        } else {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
