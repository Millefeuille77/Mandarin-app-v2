// JsonImporter.kt — Mandarin Learn
// Reads JSON assets from res/raw/ and inserts them into Room on first launch.
// Per ARCHITECTURE.md §3.2. Public API: importIfNeeded() returns Flow<ImportProgress>.
// DTO → Entity mappers live in JsonImporterMappers.kt to keep this file ≤ 300 lines.

package com.mandarinlearn.data.local.import

import android.content.Context
import androidx.room.withTransaction
import com.mandarinlearn.data.local.MandarinLearnDatabase
import com.mandarinlearn.data.local.entity.DataVersionEntity
import com.mandarinlearn.data.local.entity.StreakEntity
import com.mandarinlearn.data.local.entity.UserProgressEntity
import com.mandarinlearn.data.local.import.dto.ConversationPhraseDto
import com.mandarinlearn.data.local.import.dto.ExamStructureDto
import com.mandarinlearn.data.local.import.dto.ReadingDto
import com.mandarinlearn.data.local.import.dto.SampleQuestionDto
import com.mandarinlearn.data.local.import.dto.ToneDrillDto
import com.mandarinlearn.data.local.import.dto.VocabularyDto
import com.mandarinlearn.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private const val TAG = "JsonImporter"
private const val CHUNK_SIZE = 200

/**
 * Reads all bundled JSON files from res/raw/ and populates Room tables.
 * Each category import is transactional — failure rolls back only that category.
 * DTO → Entity mapping helpers are in [JsonImporterMappers.kt].
 */
class JsonImporter(
    private val context: Context,
    private val db: MandarinLearnDatabase,
    private val json: Json,
) {

    companion object {
        const val CURRENT_VOCABULARY_VERSION = 1
        const val CURRENT_READING_VERSION = 1
        const val CURRENT_AUDIO_VERSION = 1
        const val CURRENT_EXAM_VERSION = 1

        private val HSK_LEVELS = 1..5
    }

    /**
     * Checks data_version and imports only outdated categories.
     * Final emission: ImportProgress(1f, "Done").
     * On error: ImportProgress(fraction, message, isError=true).
     */
    fun importIfNeeded(): Flow<ImportProgress> = flow {
        emit(ImportProgress(0f, "Checking database…"))

        val version = db.dataVersionDao().get()
        val needsVocab = version == null || version.vocabularyVersion < CURRENT_VOCABULARY_VERSION
        val needsReading = version == null || version.readingVersion < CURRENT_READING_VERSION
        val needsAudio = version == null || version.audioVersion < CURRENT_AUDIO_VERSION
        val needsExam = version == null || version.examVersion < CURRENT_EXAM_VERSION

        val totalSteps = listOf(needsVocab, needsReading, needsAudio, needsExam).count { it }
        if (totalSteps == 0) {
            emit(ImportProgress(1f, "Up to date"))
            return@flow
        }

        var stepsCompleted = 0
        fun progressAt(frac: Float) = (stepsCompleted + frac) / totalSteps.toFloat()

        // ---- VOCABULARY ----
        if (needsVocab) {
            emit(ImportProgress(progressAt(0f), "Importing vocabulary…"))
            try {
                importVocabulary { f -> emit(ImportProgress(progressAt(f * 0.9f), "Importing vocabulary…")) }
                stepsCompleted++
            } catch (e: Exception) {
                Logger.e(TAG, "Vocabulary import failed", e)
                emit(ImportProgress(progressAt(0f), "Failed to import vocabulary", isError = true))
                return@flow
            }
        }

        // ---- READING ----
        if (needsReading) {
            emit(ImportProgress(progressAt(0f), "Importing reading passages…"))
            try {
                importReading { f -> emit(ImportProgress(progressAt(f * 0.9f), "Importing reading…")) }
                stepsCompleted++
            } catch (e: Exception) {
                Logger.e(TAG, "Reading import failed", e)
                emit(ImportProgress(progressAt(0f), "Failed to import reading passages", isError = true))
                return@flow
            }
        }

        // ---- AUDIO ----
        if (needsAudio) {
            emit(ImportProgress(progressAt(0f), "Importing audio content…"))
            try {
                importAudio { f -> emit(ImportProgress(progressAt(f * 0.9f), "Importing audio…")) }
                stepsCompleted++
            } catch (e: Exception) {
                Logger.e(TAG, "Audio import failed", e)
                emit(ImportProgress(progressAt(0f), "Failed to import audio content", isError = true))
                return@flow
            }
        }

        // ---- EXAMS ----
        if (needsExam) {
            emit(ImportProgress(progressAt(0f), "Importing exam structures…"))
            try {
                importExams { f -> emit(ImportProgress(progressAt(f * 0.9f), "Importing exams…")) }
                stepsCompleted++
            } catch (e: Exception) {
                Logger.e(TAG, "Exam import failed", e)
                emit(ImportProgress(progressAt(0f), "Failed to import exam structures", isError = true))
                return@flow
            }
        }

        // ---- SEED & FINALISE ----
        emit(ImportProgress(0.95f, "Finalising…"))
        try {
            seedUserProgress()
            db.streakDao().upsert(StreakEntity(id = 1, currentStreak = 0, longestStreak = 0))
        } catch (e: Exception) {
            Logger.e(TAG, "Seeding failed", e)
            emit(ImportProgress(0.95f, "Failed to finalise setup", isError = true))
            return@flow
        }

        db.dataVersionDao().upsert(
            (version ?: DataVersionEntity()).copy(
                vocabularyVersion = CURRENT_VOCABULARY_VERSION,
                readingVersion = CURRENT_READING_VERSION,
                audioVersion = CURRENT_AUDIO_VERSION,
                examVersion = CURRENT_EXAM_VERSION,
                importedAt = System.currentTimeMillis(),
            )
        )
        emit(ImportProgress(1f, "Done"))
    }.flowOn(Dispatchers.Default)

    // ---- Category importers ----

    private suspend fun importVocabulary(onProgress: suspend (Float) -> Unit) {
        val levels = HSK_LEVELS.toList()
        levels.forEachIndexed { i, level ->
            val resId = context.resources.getIdentifier("hsk${level}_vocab", "raw", context.packageName)
            val dtos = context.resources.openRawResource(resId).use { s ->
                json.decodeFromString(ListSerializer(VocabularyDto.serializer()), s.bufferedReader().readText())
            }
            dtos.map { it.toVocabularyEntity() }.chunked(CHUNK_SIZE).forEach { chunk ->
                db.withTransaction { db.vocabularyDao().insertAll(chunk) }
            }
            Logger.d(TAG, "Imported ${dtos.size} vocab for HSK $level")
            onProgress((i + 1).toFloat() / levels.size)
        }
    }

    private suspend fun importReading(onProgress: suspend (Float) -> Unit) {
        val levels = HSK_LEVELS.toList()
        levels.forEachIndexed { i, level ->
            val resId = context.resources.getIdentifier("hsk${level}_readings", "raw", context.packageName)
            val dtos = context.resources.openRawResource(resId).use { s ->
                json.decodeFromString(ListSerializer(ReadingDto.serializer()), s.bufferedReader().readText())
            }
            dtos.map { it.toReadingEntity() }.chunked(CHUNK_SIZE).forEach { chunk ->
                db.withTransaction { db.readingDao().insertAll(chunk) }
            }
            Logger.d(TAG, "Imported ${dtos.size} readings for HSK $level")
            onProgress((i + 1).toFloat() / levels.size)
        }
    }

    private suspend fun importAudio(onProgress: suspend (Float) -> Unit) {
        val drillResId = context.resources.getIdentifier("tone_drills", "raw", context.packageName)
        val drills = context.resources.openRawResource(drillResId).use { s ->
            json.decodeFromString(ListSerializer(ToneDrillDto.serializer()), s.bufferedReader().readText())
        }
        db.withTransaction { db.toneDrillDao().insertAll(drills.map { it.toToneDrillEntity() }) }
        Logger.d(TAG, "Imported ${drills.size} tone drills")
        onProgress(0.5f)

        val phraseResId = context.resources.getIdentifier("conversation_phrases", "raw", context.packageName)
        val phrases = context.resources.openRawResource(phraseResId).use { s ->
            json.decodeFromString(ListSerializer(ConversationPhraseDto.serializer()), s.bufferedReader().readText())
        }
        phrases.map { it.toConversationPhraseEntity() }.chunked(CHUNK_SIZE).forEach { chunk ->
            db.withTransaction { db.conversationPhraseDao().insertAll(chunk) }
        }
        Logger.d(TAG, "Imported ${phrases.size} conversation phrases")
        onProgress(1f)
    }

    private suspend fun importExams(onProgress: suspend (Float) -> Unit) {
        val structures = HSK_LEVELS.map { level ->
            val resId = context.resources.getIdentifier("hsk${level}_exam_structure", "raw", context.packageName)
            context.resources.openRawResource(resId).use { s ->
                json.decodeFromString(ExamStructureDto.serializer(), s.bufferedReader().readText())
            }.toExamStructureEntity()
        }
        db.withTransaction { db.examStructureDao().insertAll(structures) }
        Logger.d(TAG, "Imported ${structures.size} exam structures")
        onProgress(0.5f)

        val qResId = context.resources.getIdentifier("sample_questions", "raw", context.packageName)
        val questions = context.resources.openRawResource(qResId).use { s ->
            json.decodeFromString(ListSerializer(SampleQuestionDto.serializer()), s.bufferedReader().readText())
        }
        questions.map { it.toSampleQuestionEntity() }.chunked(CHUNK_SIZE).forEach { chunk ->
            db.withTransaction { db.sampleQuestionDao().insertAll(chunk) }
        }
        Logger.d(TAG, "Imported ${questions.size} sample questions")
        onProgress(1f)
    }

    private suspend fun seedUserProgress() {
        val sections = listOf("vocabulary", "reading", "listening", "speaking", "exam")
        val rows = mutableListOf<UserProgressEntity>()
        for (level in HSK_LEVELS) {
            val vocabCount = db.vocabularyDao().countByLevel(level)
            val readingCount = db.readingDao().countByLevel(level)
            val phraseCount = db.conversationPhraseDao().countByLevel(level)
            val questionCount = db.sampleQuestionDao().countByLevel(level)
            val drillCount = db.toneDrillDao().count()
            for (section in sections) {
                val total = when (section) {
                    "vocabulary" -> vocabCount
                    "reading" -> readingCount
                    "listening" -> drillCount + questionCount
                    "speaking" -> phraseCount
                    "exam" -> questionCount
                    else -> 0
                }
                rows += UserProgressEntity(hskLevel = level, section = section, totalItems = total)
            }
        }
        db.userProgressDao().upsertAll(rows)
        Logger.d(TAG, "Seeded ${rows.size} user_progress rows")
    }
}
