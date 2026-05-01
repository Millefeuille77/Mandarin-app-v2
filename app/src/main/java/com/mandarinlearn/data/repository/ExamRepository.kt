// ExamRepository.kt — Mandarin Learn
// Full implementation. Handles exam structures, sample questions, and result persistence.
// Decodes JSON sections_json, section_scores_json, and answers_json columns.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.ExamResultDao
import com.mandarinlearn.data.local.dao.ExamStructureDao
import com.mandarinlearn.data.local.dao.SampleQuestionDao
import com.mandarinlearn.data.local.entity.ExamResultEntity
import com.mandarinlearn.data.local.entity.SampleQuestionEntity
import com.mandarinlearn.data.local.import.dto.ExamSectionDto
import com.mandarinlearn.domain.model.AnswerRecord
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.ExamSection
import com.mandarinlearn.domain.model.ExamStructure
import com.mandarinlearn.domain.model.SampleQuestion
import com.mandarinlearn.domain.model.SectionScore
import com.mandarinlearn.util.DispatcherProvider
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private const val TAG = "ExamRepository"

/**
 * Repository for exam structures, sample questions, and exam results.
 */
class ExamRepository(
    private val structureDao: ExamStructureDao,
    private val questionDao: SampleQuestionDao,
    private val resultDao: ExamResultDao,
    private val dispatchers: DispatcherProvider,
) {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    /** Returns the exam structure for a given level, decoded from JSON. */
    fun getStructure(hsk: Int): Flow<ExamStructure?> =
        structureDao.getStructure(hsk).map { entity ->
            entity?.let {
                val sections = json.decodeFromString(
                    ListSerializer(ExamSectionDto.serializer()), it.sectionsJson
                ).map { dto ->
                    ExamSection(
                        name = dto.name,
                        questionCount = dto.questionCount,
                        durationMinutes = dto.durationMinutes,
                        questionTypes = dto.questionTypes,
                        maxScore = dto.maxScore,
                        passingScore = dto.passingScore,
                        description = dto.description,
                    )
                }
                ExamStructure(
                    hskLevel = it.hskLevel,
                    totalDurationMinutes = it.totalDurationMinutes,
                    sections = sections,
                    totalMaxScore = it.totalMaxScore,
                    totalPassingScore = it.totalPassingScore,
                    vocabularyRequired = it.vocabularyRequired,
                    scoringNotes = it.scoringNotes,
                )
            }
        }

    /** Returns all exam structures — reactive. */
    fun getAllStructures(): Flow<List<ExamStructure>> =
        structureDao.getAll().map { list ->
            list.mapNotNull { entity ->
                try {
                    val sections = json.decodeFromString(
                        ListSerializer(ExamSectionDto.serializer()), entity.sectionsJson
                    ).map { dto ->
                        ExamSection(dto.name, dto.questionCount, dto.durationMinutes,
                            dto.questionTypes, dto.maxScore, dto.passingScore, dto.description)
                    }
                    ExamStructure(entity.hskLevel, entity.totalDurationMinutes, sections,
                        entity.totalMaxScore, entity.totalPassingScore,
                        entity.vocabularyRequired, entity.scoringNotes)
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to decode structure for level ${entity.hskLevel}", e)
                    null
                }
            }
        }

    /** Returns up to [limit] shuffled questions for an exam section. */
    suspend fun getQuestionsForExam(hsk: Int, section: String, limit: Int): List<SampleQuestion> =
        withContext(dispatchers.io) {
            questionDao.getQuestionsForExam(hsk, section, limit).map { it.toDomain() }
        }

    /** Returns all questions for a level and section — reactive. */
    fun getQuestions(hsk: Int, section: String): Flow<List<SampleQuestion>> =
        questionDao.getByLevelAndSection(hsk, section).map { list -> list.map { it.toDomain() } }

    /** Persists an exam result. Returns the generated row id. */
    suspend fun insertResult(result: ExamResult): Long =
        withContext(dispatchers.io) {
            try {
                resultDao.insert(result.toEntity())
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to insert exam result", e)
                throw e
            }
        }

    /** Returns results for a given level — reactive, newest first. */
    fun getResultsByLevel(hsk: Int): Flow<List<ExamResult>> =
        resultDao.getByLevel(hsk).map { list -> list.mapNotNull { it.toDomainSafe() } }

    /** Returns recent results across all levels — reactive. */
    fun getRecentResults(limit: Int): Flow<List<ExamResult>> =
        resultDao.getRecent(limit).map { list -> list.mapNotNull { it.toDomainSafe() } }

    /** Returns one result by id — reactive. */
    fun getResultById(id: Long): Flow<ExamResult?> =
        resultDao.getById(id).map { it?.toDomainSafe() }

    /** Deletes all results (Phase 9 reset). */
    suspend fun deleteAllResults() {
        withContext(dispatchers.io) { resultDao.deleteAll() }
    }

    // ---- Mappers ----

    private fun SampleQuestionEntity.toDomain(): SampleQuestion {
        val options = json.decodeFromString(ListSerializer(String.serializer()), optionsJson)
        return SampleQuestion(id, hskLevel, section, questionType, questionText,
            audioTextChinese, audioTextPinyin, options, correctAnswer, explanation)
    }

    private fun ExamResultEntity.toDomainSafe(): ExamResult? {
        return try {
            val scores = json.decodeFromString(ListSerializer(SectionScore.serializer()), sectionScoresJson)
            val answers = json.decodeFromString(ListSerializer(AnswerRecord.serializer()), answersJson)
            ExamResult(id, hskLevel, startedAt, finishedAt, durationSeconds,
                scores, totalScore, totalMaxScore, passingScore, passed == 1, answers)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to decode exam result $id", e)
            null
        }
    }

    private fun ExamResult.toEntity(): ExamResultEntity {
        val scoresJson = json.encodeToString(ListSerializer(SectionScore.serializer()), sectionScores)
        val answersJson = json.encodeToString(ListSerializer(AnswerRecord.serializer()), answers)
        return ExamResultEntity(
            id = if (id == 0L) 0L else id,
            hskLevel = hskLevel, startedAt = startedAt, finishedAt = finishedAt,
            durationSeconds = durationSeconds, sectionScoresJson = scoresJson,
            totalScore = totalScore, totalMaxScore = totalMaxScore,
            passingScore = passingScore, passed = if (passed) 1 else 0,
            answersJson = answersJson,
        )
    }
}
