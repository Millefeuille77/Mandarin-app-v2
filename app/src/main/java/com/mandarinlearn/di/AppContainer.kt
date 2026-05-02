// AppContainer.kt — Mandarin Learn
// Manual DI container. Per ARCHITECTURE.md §6: no Hilt/Dagger.
// Phase 2: Room DB, all repositories, JsonImporter, preferences repository.
// Phase 3: ReviewVocabularyUseCase.
// Phase 4: PlayChineseAudioUseCase.
// Phase 5: GeminiService, NetworkMonitor, AndroidTtsFallback, AudioRepository (full),
//           ListeningRepository (full).
// Phase 6: AudioRecorder, SpeakingRepository (full), ScorePronunciationUseCase.
// Phase 7: StartExamUseCase, SubmitExamUseCase, ExamGrader.
// Phase 8: GetDashboardUseCase.
// Phase 9: SettingsRepository, ExportProgressUseCase, ImportProgressUseCase, ResetProgressUseCase.

package com.mandarinlearn.di

import android.content.Context
import com.mandarinlearn.BuildConfig
import com.mandarinlearn.data.audio.AndroidTtsFallback
import com.mandarinlearn.data.audio.AudioRecorder
import com.mandarinlearn.data.local.MandarinLearnDatabase
import com.mandarinlearn.data.local.import.JsonImporter
import com.mandarinlearn.data.preferences.UserPreferencesRepository
import com.mandarinlearn.data.remote.GeminiService
import com.mandarinlearn.data.repository.AudioRepository
import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.data.repository.ListeningRepository
import com.mandarinlearn.data.repository.ProgressRepository
import com.mandarinlearn.data.repository.ReadingRepository
import com.mandarinlearn.data.repository.SpeakingRepository
import com.mandarinlearn.data.repository.SettingsRepository
import com.mandarinlearn.data.repository.StreakRepository
import com.mandarinlearn.data.repository.VocabularyRepository
import com.mandarinlearn.domain.grading.ExamGrader
import com.mandarinlearn.domain.usecase.ExportProgressUseCase
import com.mandarinlearn.domain.usecase.ImportProgressUseCase
import com.mandarinlearn.domain.usecase.ResetProgressUseCase
import com.mandarinlearn.domain.usecase.GetDashboardUseCase
import com.mandarinlearn.domain.usecase.PlayChineseAudioUseCase
import com.mandarinlearn.domain.usecase.ReviewVocabularyUseCase
import com.mandarinlearn.domain.usecase.ScorePronunciationUseCase
import com.mandarinlearn.domain.usecase.StartExamUseCase
import com.mandarinlearn.domain.usecase.SubmitExamUseCase
import com.mandarinlearn.util.DefaultDispatcherProvider
import com.mandarinlearn.util.DispatcherProvider
import com.mandarinlearn.util.NetworkMonitor
import kotlinx.serialization.json.Json

/**
 * Application-scoped DI container.
 * All singletons are lazy-initialised here and injected into ViewModels via
 * their companion-object factory functions.
 *
 */
class AppContainer(val context: Context) {

    // ---- Core utilities ----

    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()

    /** Lenient Json instance shared across import and repository serialisation. */
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // ---- Database (lazy — opened on first access) ----

    val database: MandarinLearnDatabase by lazy {
        MandarinLearnDatabase.create(context)
    }

    // ---- Importer ----

    val jsonImporter: JsonImporter by lazy {
        JsonImporter(context, database, json)
    }

    // ---- Phase 5: Network & Gemini ----

    /** Wraps ConnectivityManager for network state queries (ARCHITECTURE.md §4.5). */
    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(context)
    }

    /**
     * Gemini API service singleton (ARCHITECTURE.md §4.2).
     * Key read from BuildConfig — if blank, all calls return GeminiError.NoApiKey.
     */
    val geminiService: GeminiService by lazy {
        GeminiService(
            apiKey          = BuildConfig.GEMINI_API_KEY,
            ioDispatcher    = dispatchers.io,
            networkMonitor  = networkMonitor,
        )
    }

    /** On-device TTS fallback for Chinese audio (ARCHITECTURE.md §4.6). */
    val androidTtsFallback: AndroidTtsFallback by lazy {
        AndroidTtsFallback(context)
    }

    // ---- Repositories ----

    val vocabularyRepository: VocabularyRepository by lazy {
        VocabularyRepository(database.vocabularyDao(), dispatchers)
    }

    val readingRepository: ReadingRepository by lazy {
        ReadingRepository(database.readingDao(), dispatchers)
    }

    val examRepository: ExamRepository by lazy {
        ExamRepository(
            database.examStructureDao(),
            database.sampleQuestionDao(),
            database.examResultDao(),
            dispatchers,
        )
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepository(database.userProgressDao(), dispatchers)
    }

    val streakRepository: StreakRepository by lazy {
        StreakRepository(database.streakDao(), dispatchers)
    }

    /**
     * Full Phase 5 AudioRepository — cache → Gemini → AndroidTtsFallback chain.
     * Replaces the Phase 2 stub.
     */
    val audioRepository: AudioRepository by lazy {
        AudioRepository(
            gemini         = geminiService,
            audioCacheDao  = database.audioCacheDao(),
            androidTts     = androidTtsFallback,
            networkMonitor = networkMonitor,
            ioDispatcher   = dispatchers.io,
            context        = context,
        )
    }

    /**
     * Full Phase 5 ListeningRepository — sourced from SampleQuestionDao.
     * Replaces the Phase 2 stub.
     */
    val listeningRepository: ListeningRepository by lazy {
        ListeningRepository(
            sampleQuestionDao = database.sampleQuestionDao(),
            dispatchers       = dispatchers,
            json              = json,
        )
    }

    /** Phase 6: AudioRecorder wraps MediaRecorder for voice recordings. */
    val audioRecorder: AudioRecorder by lazy {
        AudioRecorder(context)
    }

    /** Phase 6: Full SpeakingRepository — replaces the Phase 2 stub. */
    val speakingRepository: SpeakingRepository by lazy {
        SpeakingRepository(
            conversationPhraseDao = database.conversationPhraseDao(),
            geminiService         = geminiService,
            dispatchers           = dispatchers,
        )
    }

    // ---- Phase 3: Domain use cases ----

    val reviewVocabularyUseCase: ReviewVocabularyUseCase by lazy {
        ReviewVocabularyUseCase(vocabularyRepository)
    }

    // ---- Phase 4: Reading use cases ----

    val playChineseAudioUseCase: PlayChineseAudioUseCase by lazy {
        PlayChineseAudioUseCase(audioRepository)
    }

    // ---- Phase 6: Speaking use case ----

    val scorePronunciationUseCase: ScorePronunciationUseCase by lazy {
        ScorePronunciationUseCase(speakingRepository)
    }

    // ---- Phase 7: Exam grading use cases ----

    /** Stateless grader — shared instance (pure functions, no mutable state). */
    val examGrader: ExamGrader by lazy { ExamGrader() }

    /** Fetches exam structure + questions for a given HSK level. */
    val startExamUseCase: StartExamUseCase by lazy {
        StartExamUseCase(examRepository)
    }

    /** Grades answers via ExamGrader and persists the result row. */
    val submitExamUseCase: SubmitExamUseCase by lazy {
        SubmitExamUseCase(examRepository, examGrader)
    }

    // ---- Phase 8: Dashboard use case ----

    /**
     * Aggregates streak + vocabulary mastery + reading progress + due counts
     * into a single observable flow for HomeScreen and ProgressScreen.
     */
    val getDashboardUseCase: GetDashboardUseCase by lazy {
        GetDashboardUseCase(
            streakRepository     = streakRepository,
            vocabularyRepository = vocabularyRepository,
            progressRepository   = progressRepository,
            examRepository       = examRepository,
        )
    }

    // ---- Preferences (DataStore-backed) ----

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    // ---- Phase 9: Settings ----

    /** Thin wrapper over UserPreferencesRepository for SettingsViewModel injection. */
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(userPreferencesRepository)
    }

    /** Exports SRS state, exam history, streak, and progress to a SAF JSON file. */
    val exportProgressUseCase: ExportProgressUseCase by lazy {
        ExportProgressUseCase(context, database, json)
    }

    /** Reads a SAF JSON export and replaces user-state in Room (validates version == 1). */
    val importProgressUseCase: ImportProgressUseCase by lazy {
        ImportProgressUseCase(context, database, json)
    }

    /** Atomically resets all SM-2 fields, exam results, streak, and progress counts. */
    val resetProgressUseCase: ResetProgressUseCase by lazy {
        ResetProgressUseCase(database)
    }
}
