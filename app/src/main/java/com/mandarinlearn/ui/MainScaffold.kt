// MainScaffold.kt — Mandarin Learn
// Bottom-nav scaffold that hosts the 4 tabs: Learn / Practice / Exam / Me.
// UX_SPECIFICATION.md §1.6: 4 items, icon + label always visible, height 80 dp.
// Phase 4 carried-over task: PracticeHubScreen now receives a real ViewModel from AppContainer.

package com.mandarinlearn.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mandarinlearn.di.AppContainer
import com.mandarinlearn.navigation.Routes
import com.mandarinlearn.navigation.tabFadeIn
import com.mandarinlearn.navigation.tabFadeOut
import com.mandarinlearn.ui.components.MandarinBottomNav
import com.mandarinlearn.ui.exam.ExamHubScreen
import com.mandarinlearn.ui.exam.ExamHubViewModel
import com.mandarinlearn.ui.home.HomeScreen
import com.mandarinlearn.ui.me.MeScreen
import com.mandarinlearn.ui.practice.PracticeHubScreen
import com.mandarinlearn.ui.practice.PracticeHubViewModel

/**
 * Main scaffold composable that wraps the 4 tab-root screens with the bottom navigation bar.
 * Deep-link navigation (to VocabularyScreen, ReadingListScreen, etc.) is handled by the root
 * [AppNavigation] NavHost — this inner NavHost only contains the 4 tab roots.
 *
 * Phase 4: [PracticeHubScreen] now receives a real [PracticeHubViewModel] injected from
 * [AppContainer] so it can display accurate vocab-due counts for the selected HSK level.
 *
 * @param rootNavController The root NavController (from AppNavigation) used for deeper screens.
 * @param appContainer      AppContainer for ViewModel factories. Nullable for preview safety.
 * @param modifier          Optional modifier for the Scaffold.
 */
@Composable
fun MainScaffold(
    rootNavController: NavHostController,
    appContainer: AppContainer? = null,
    modifier: Modifier = Modifier,
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

    Scaffold(
        modifier  = modifier.fillMaxSize(),
        bottomBar = {
            MandarinBottomNav(
                selectedRoute = currentRoute,
                onTabSelected = { route ->
                    tabNavController.navigate(route) {
                        popUpTo(tabNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController      = tabNavController,
            startDestination   = Routes.HOME,
            modifier           = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition    = { tabFadeIn },
            exitTransition     = { tabFadeOut },
            popEnterTransition = { tabFadeIn },
            popExitTransition  = { tabFadeOut },
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToVocabulary = { hsk -> rootNavController.navigate(Routes.vocabulary(hsk)) },
                    onNavigateToFlashcards = { hsk -> rootNavController.navigate(Routes.flashcards(hsk)) },
                    onNavigateToSettings   = { rootNavController.navigate(Routes.SETTINGS) },
                )
            }

            composable(Routes.PRACTICE) {
                // Phase 4 carried-over nav wiring: use real ViewModel when AppContainer is available
                if (appContainer != null) {
                    val practiceVm: PracticeHubViewModel = viewModel(
                        factory = PracticeHubViewModel.factory(appContainer.vocabularyRepository)
                    )
                    PracticeHubScreen(
                        viewModel              = practiceVm,
                        onNavigateToVocabulary = { hsk -> rootNavController.navigate(Routes.vocabulary(hsk)) },
                        onNavigateToReading    = { hsk -> rootNavController.navigate(Routes.readingList(hsk)) },
                        onNavigateToListening  = { hsk -> rootNavController.navigate(Routes.listening(hsk)) },
                        onNavigateToSpeaking   = { hsk -> rootNavController.navigate(Routes.speaking(hsk)) },
                    )
                } else {
                    // Fallback for previews / environments without container
                    PracticeHubScreen(
                        onNavigateToVocabulary = { hsk -> rootNavController.navigate(Routes.vocabulary(hsk)) },
                        onNavigateToReading    = { hsk -> rootNavController.navigate(Routes.readingList(hsk)) },
                        onNavigateToListening  = { hsk -> rootNavController.navigate(Routes.listening(hsk)) },
                        onNavigateToSpeaking   = { hsk -> rootNavController.navigate(Routes.speaking(hsk)) },
                    )
                }
            }

            composable(Routes.EXAM_HUB) {
                // Phase 7: use real ViewModel when AppContainer is available.
                if (appContainer != null) {
                    val examHubVm: ExamHubViewModel = viewModel(
                        factory = ExamHubViewModel.factory(appContainer.examRepository)
                    )
                    ExamHubScreen(
                        viewModel        = examHubVm,
                        onNavigateToExam = { hsk -> rootNavController.navigate(Routes.exam(hsk)) },
                    )
                } else {
                    ExamHubScreen(
                        onNavigateToExam = { hsk -> rootNavController.navigate(Routes.exam(hsk)) },
                    )
                }
            }

            composable(Routes.ME) {
                MeScreen(
                    onNavigateToProgress = { rootNavController.navigate(Routes.PROGRESS) },
                    onNavigateToSettings = { rootNavController.navigate(Routes.SETTINGS) },
                )
            }
        }
    }
}
