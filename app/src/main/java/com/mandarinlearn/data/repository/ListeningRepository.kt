// ListeningRepository.kt — Mandarin Learn
// Full Phase 5 implementation — replaces Phase 2 stub.
// Provides listening quiz questions from the sample_questions table.
// Per IMPLEMENTATION_PLAN.md §Phase 5 item D.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.SampleQuestionDao
import com.mandarinlearn.data.local.entity.SampleQuestionEntity
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.util.DispatcherProvider
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val TAG = "ListeningRepository"
private const val SECTION_LISTENING = "listening"

/**
 * Repository for listening practice questions.
 * Questions are sourced from [SampleQuestionDao] filtered to section = "listening".
 *
 * The audio text for each question is in [SampleQuestion.audioTextChinese].
 * The [AudioRepository] is used by [ListeningViewModel] to play that text — this
 * repository is purely concerned with question data, not audio playback.
 */
class ListeningRepository(
    private val sampleQuestionDao: SampleQuestionDao,
    private val dispatchers: DispatcherProvider,
    private val json: Json,
) {

    /**
     * Returns a reactive stream of listening questions for the given HSK level.
     * Used by [ListeningViewModel] to observe question availability.
     */
    fun getListeningQuestions(hsk: Int): Flow<List<SampleQuestion>> {
        return try {
            sampleQuestionDao
                .getByLevelAndSection(hsk, SECTION_LISTENING)
                .map { entities -> entities.map { it.toDomain(json) } }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to observe listening questions for HSK $hsk", e)
            flowOf(emptyList())
        }
    }

    /**
     * Returns up to [limit] randomised questions for a listening session.
     * Runs on the IO dispatcher. Returns empty list on error (graceful degradation).
     */
    suspend fun getQuestionsForSession(hsk: Int, limit: Int = 10): List<SampleQuestion> =
        withContext(dispatchers.io) {
            try {
                sampleQuestionDao
                    .getQuestionsForExam(hsk, SECTION_LISTENING, limit)
                    .map { it.toDomain(json) }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load listening session for HSK $hsk", e)
                emptyList()
            }
        }
}

/**
 * Maps [SampleQuestionEntity] to [SampleQuestion] domain model.
 * Parses [optionsJson] from JSON List<String>.
 */
private fun SampleQuestionEntity.toDomain(json: Json): SampleQuestion {
    val options = try {
        json.decodeFromString<List<String>>(optionsJson)
    } catch (e: Exception) {
        Logger.w("ListeningRepository", "Failed to parse options JSON for question $id", e)
        emptyList()
    }
    return SampleQuestion(
        id               = id,
        hskLevel         = hskLevel,
        section          = section,
        questionType     = questionType,
        questionText     = questionText,
        audioTextChinese = audioTextChinese,
        audioTextPinyin  = audioTextPinyin,
        options          = options,
        correctAnswer    = correctAnswer,
        explanation      = explanation,
    )
}
