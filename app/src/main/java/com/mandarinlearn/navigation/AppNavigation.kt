// AppNavigation.kt — Mandarin Learn
// Central NavHost that wires every route in the app.
// UX_SPECIFICATION.md §2 defines the full navigation tree.
// Phase 2: IMPORT is the start destination; gated on JsonImporter completion.
// Phase 4: ViewModels injected from AppContainer factory.
// Phase 6: SpeakingScreen wired with real SpeakingViewModel.
// Phase 7: ExamHubScreen, ExamScreen, ExamResultScreen wired with real ViewModels.

package com.mandarinlearn.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mandarinlearn.data.local.import.JsonImporter
import com.mandarinlearn.di.AppContainer
import com.mandarinlearn.ui.MainScaffold
import com.mandarinlearn.ui.exam.ExamResultScreen
import com.mandarinlearn.ui.exam.ExamResultViewModel
import com.mandarinlearn.ui.exam.ExamScreen
import com.mandarinlearn.ui.exam.ExamViewModel
import com.mandarinlearn.ui.importing.ImportLoadingScreen
import com.mandarinlearn.ui.importing.ImportLoadingViewModel
import com.mandarinlearn.ui.listening.ListeningScreen
import com.mandarinlearn.ui.listening.ListeningViewModel
import com.mandarinlearn.ui.progress.ProgressScreen
import com.mandarinlearn.ui.reading.PassageScreen
import com.mandarinlearn.ui.reading.PassageViewModel
import com.mandarinlearn.ui.reading.ReadingListScreen
import com.mandarinlearn.ui.reading.ReadingListViewModel
import com.mandarinlearn.ui.settings.SettingsScreen
import com.mandarinlearn.ui.speaking.SpeakingScreen
import com.mandarinlearn.ui.speaking.SpeakingViewModel
import com.mandarinlearn.ui.vocabulary.FlashcardScreen
import com.mandarinlearn.ui.vocabulary.FlashcardViewModel
import com.mandarinlearn.ui.vocabulary.VocabularyScreen
import com.mandarinlearn.ui.vocabulary.VocabularyViewModel

/**
 * Root NavHost for Mandarin Learn.
 *
 * Start destination is [Routes.IMPORT] (Phase 2). After import completes,
 * ImportLoadingScreen navigates to [Routes.MAIN] with popUpTo so back-press can't return.
 *
 * Phase 4 carried-over task: ViewModels for Vocabulary, Flashcard, ReadingList, and Passage
 * screens are now created via factories from [AppContainer] rather than using the legacy
 * no-arg overloads that were placeholders in Phase 1.
 *
 * @param appContainer   DI container providing repositories and use-cases.
 * @param jsonImporter   Passed for ImportLoadingViewModel factory.
 * @param navController  NavController; defaults to rememberNavController.
 * @param modifier       Optional modifier for the NavHost.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    jsonImporter: JsonImporter? = null,
    appContainer: AppContainer? = null,
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.IMPORT,
        modifier         = modifier,
        enterTransition  = { slideInFromRight },
        exitTransition   = { slideOutToLeft },
        popEnterTransition  = { slideInFromLeft },
        popExitTransition   = { slideOutToRight },
    ) {
        // First-launch import screen — blocks until JsonImporter completes.
        composable(Routes.IMPORT) {
            val factory = jsonImporter?.let { ImportLoadingViewModel.factory(it) }
            val importVm: ImportLoadingViewModel = if (factory != null) {
                viewModel(factory = factory)
            } else {
                viewModel()
            }
            ImportLoadingScreen(
                onImportComplete = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.IMPORT) { inclusive = true }
                    }
                },
                viewModel = importVm,
            )
        }

        // Main scaffold hosts the 4-tab bottom navigation.
        composable(Routes.MAIN) {
            MainScaffold(
                rootNavController = navController,
                appContainer      = appContainer,
            )
        }

        // --- Vocabulary screens (Phase 3, wired here in Phase 4) ---

        composable(
            route     = Routes.VOCABULARY,
            arguments = listOf(navArgument("hsk") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val hsk = backStackEntry.arguments?.getInt("hsk") ?: 1
            if (appContainer != null) {
                val vm: VocabularyViewModel = viewModel(
                    factory = VocabularyViewModel.factory(
                        appContainer.vocabularyRepository, hsk
                    )
                )
                VocabularyScreen(
                    viewModel              = vm,
                    onNavigateToFlashcards = { navController.navigate(Routes.flashcards(hsk)) },
                    onNavigateBack         = { navController.popBackStack() },
                )
            } else {
                // Fallback for preview/test environments without a container
                VocabularyScreen(
                    hsk                    = hsk,
                    onNavigateToFlashcards = { navController.navigate(Routes.flashcards(hsk)) },
                    onNavigateBack         = { navController.popBackStack() },
                )
            }
        }

        composable(
            route     = Routes.FLASHCARDS,
            arguments = listOf(navArgument("hsk") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val hsk = backStackEntry.arguments?.getInt("hsk") ?: 1
            if (appContainer != null) {
                val vm: FlashcardViewModel = viewModel(
                    factory = FlashcardViewModel.factory(
                        vocabularyRepository = appContainer.vocabularyRepository,
                        audioRepository      = appContainer.audioRepository,
                        reviewUseCase        = appContainer.reviewVocabularyUseCase,
                        hsk                  = hsk,
                    )
                )
                FlashcardScreen(
                    viewModel      = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            } else {
                FlashcardScreen(
                    hsk            = hsk,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }

        // --- Reading screens (Phase 4) ---

        composable(
            route     = Routes.READING_LIST,
            arguments = listOf(navArgument("hsk") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val hsk = backStackEntry.arguments?.getInt("hsk") ?: 1
            if (appContainer != null) {
                val vm: ReadingListViewModel = viewModel(
                    factory = ReadingListViewModel.factory(appContainer.readingRepository, hsk)
                )
                ReadingListScreen(
                    viewModel           = vm,
                    onNavigateToPassage = { id -> navController.navigate(Routes.passage(id)) },
                    onNavigateBack      = { navController.popBackStack() },
                )
            } else {
                ReadingListScreen(
                    hsk                 = hsk,
                    onNavigateToPassage = { id -> navController.navigate(Routes.passage(id)) },
                    onNavigateBack      = { navController.popBackStack() },
                )
            }
        }

        composable(
            route     = Routes.PASSAGE,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            if (appContainer != null) {
                val vm: PassageViewModel = viewModel(
                    factory = PassageViewModel.factory(
                        passageId            = id,
                        readingRepository    = appContainer.readingRepository,
                        vocabularyRepository = appContainer.vocabularyRepository,
                        audioRepository      = appContainer.audioRepository,
                    )
                )
                PassageScreen(
                    viewModel      = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            } else {
                PassageScreen(
                    passageId      = id,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }

        // --- Listening (Phase 5 — real ViewModel) / Speaking (Phase 6) ---

        composable(
            route     = Routes.LISTENING,
            arguments = listOf(navArgument("hsk") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val hsk = backStackEntry.arguments?.getInt("hsk") ?: 1
            if (appContainer != null) {
                val vm: ListeningViewModel = viewModel(
                    factory = ListeningViewModel.factory(
                        listeningRepository = appContainer.listeningRepository,
                        audioRepository     = appContainer.audioRepository,
                        hsk                 = hsk,
                    )
                )
                ListeningScreen(
                    viewModel      = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            } else {
                ListeningScreen(
                    hsk            = hsk,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }

        composable(
            route     = Routes.SPEAKING,
            arguments = listOf(navArgument("hsk") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val hsk = backStackEntry.arguments?.getInt("hsk") ?: 1
            if (appContainer != null) {
                val vm: SpeakingViewModel = viewModel(
                    factory = SpeakingViewModel.factory(
                        speakingRepository        = appContainer.speakingRepository,
                        scorePronunciationUseCase = appContainer.scorePronunciationUseCase,
                        audioRecorder             = appContainer.audioRecorder,
                        networkMonitor            = appContainer.networkMonitor,
                        context                   = appContainer.context,
                        hsk                       = hsk,
                    )
                )
                SpeakingScreen(
                    viewModel      = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            } else {
                SpeakingScreen(
                    hsk            = hsk,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }

        // --- Exam screens (Phase 7 — real ViewModels) ---

        composable(Routes.EXAM, listOf(navArgument("hsk") { type = NavType.IntType; defaultValue = 1 })) { back ->
            val hsk = back.arguments?.getInt("hsk") ?: 1
            val toResult = { id: Long ->
                navController.navigate(Routes.examResult(id)) { popUpTo(Routes.EXAM) { inclusive = true } }
            }
            if (appContainer != null) {
                val vm: ExamViewModel = viewModel(factory = ExamViewModel.factory(
                    startExamUseCase = appContainer.startExamUseCase,
                    submitExamUseCase = appContainer.submitExamUseCase,
                    audioRepository = appContainer.audioRepository,
                    hsk = hsk))
                ExamScreen(vm, onNavigateToResult = toResult, onNavigateBack = { navController.popBackStack() })
            } else {
                ExamScreen(hsk = hsk, onNavigateToResult = toResult, onNavigateBack = { navController.popBackStack() })
            }
        }

        composable(Routes.EXAM_RESULT, listOf(navArgument("id") { type = NavType.LongType })) { back ->
            val id = back.arguments?.getLong("id") ?: 0L
            val tryAgain = { hsk: Int -> navController.navigate(Routes.exam(hsk)) }
            if (appContainer != null) {
                val vm: ExamResultViewModel = viewModel(factory = ExamResultViewModel.factory(
                    examRepository = appContainer.examRepository, resultId = id))
                ExamResultScreen(vm, onNavigateBack = { navController.popBackStack() }, onTryAgain = tryAgain)
            } else {
                ExamResultScreen(id, onNavigateBack = { navController.popBackStack() }, onTryAgain = tryAgain)
            }
        }

        // --- Me tab children ---

        composable(Routes.PROGRESS) {
            ProgressScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
