// ExamRepositoryTest.kt — Mandarin Learn
// Unit tests for ExamRepository.
// IMPLEMENTATION_PLAN.md Phase 7 §E.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.ExamResultDao
import com.mandarinlearn.data.local.dao.ExamStructureDao
import com.mandarinlearn.data.local.dao.SampleQuestionDao
import com.mandarinlearn.data.local.entity.ExamResultEntity
import com.mandarinlearn.data.local.entity.ExamStructureEntity
import com.mandarinlearn.data.local.entity.SampleQuestionEntity
import com.mandarinlearn.domain.model.AnswerRecord
import com.mandarinlearn.domain.model.ExamResult
import com.mandarinlearn.domain.model.SectionScore
import com.mandarinlearn.util.DefaultDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamRepositoryTest {

    private val structureDao: ExamStructureDao = mockk()
    private val questionDao: SampleQuestionDao = mockk()
    private val resultDao: ExamResultDao = mockk()
    private val dispatchers = DefaultDispatcherProvider()
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun makeRepo() = ExamRepository(structureDao, questionDao, resultDao, dispatchers)

    // ---- getStructure ----

    @Test
    fun `getStructure returns null when no row exists`() = runTest {
        every { structureDao.getStructure(1) } returns flowOf(null)
        val repo = makeRepo()
        var result: com.mandarinlearn.domain.model.ExamStructure? = null
        repo.getStructure(1).collect { result = it }
        assertNull(result)
    }

    @Test
    fun `getStructure decodes sections_json for HSK 1 (2 sections)`() = runTest {
        every { structureDao.getStructure(1) } returns flowOf(fakeStructureEntity(1))
        val repo = makeRepo()
        var structure: com.mandarinlearn.domain.model.ExamStructure? = null
        repo.getStructure(1).collect { structure = it }
        assertNotNull(structure)
        assertEquals(1, structure!!.hskLevel)
        assertEquals(200, structure.totalMaxScore)
        assertEquals(120, structure.totalPassingScore)
        assertEquals(2, structure.sections.size)
    }

    @Test
    fun `getStructure decodes sections_json for HSK 5 (3 sections)`() = runTest {
        every { structureDao.getStructure(5) } returns flowOf(fakeStructureEntity(5))
        val repo = makeRepo()
        var structure: com.mandarinlearn.domain.model.ExamStructure? = null
        repo.getStructure(5).collect { structure = it }
        assertNotNull(structure)
        assertEquals(3, structure!!.sections.size)
        assertEquals(300, structure.totalMaxScore)
        assertEquals(180, structure.totalPassingScore)
    }

    // ---- getQuestionsForExam ----

    @Test
    fun `getQuestionsForExam returns up to limit`() = runTest {
        coEvery { questionDao.getQuestionsForExam(1, "listening", 5) } returns
                listOf(fakeQuestionEntity("q1"), fakeQuestionEntity("q2"))
        val repo = makeRepo()
        val questions = repo.getQuestionsForExam(1, "listening", 5)
        assertEquals(2, questions.size)
    }

    // ---- insertResult ----

    @Test
    fun `insertResult calls dao and returns generated id`() = runTest {
        coEvery { resultDao.insert(any()) } returns 99L
        val repo = makeRepo()
        val id = repo.insertResult(fakeExamResult())
        assertEquals(99L, id)
        coVerify { resultDao.insert(any()) }
    }

    // ---- getResultsByLevel ----

    @Test
    fun `getResultsByLevel returns empty list when no results`() = runTest {
        every { resultDao.getByLevel(1) } returns flowOf(emptyList())
        val repo = makeRepo()
        var results = listOf<ExamResult>()
        repo.getResultsByLevel(1).collect { results = it }
        assertEquals(0, results.size)
    }

    @Test
    fun `getResultsByLevel decodes section_scores_json correctly`() = runTest {
        every { resultDao.getByLevel(1) } returns flowOf(listOf(fakeResultEntity(42L)))
        val repo = makeRepo()
        var results = listOf<ExamResult>()
        repo.getResultsByLevel(1).collect { results = it }
        assertEquals(1, results.size)
        assertEquals(42L, results[0].id)
        assertEquals(2, results[0].sectionScores.size)
    }

    // ---- Helpers ----

    private fun fakeStructureEntity(hsk: Int): ExamStructureEntity {
        val sectionCount = if (hsk <= 2) 2 else 3
        val maxScore = if (hsk <= 2) 200 else 300
        val passingScore = if (hsk <= 2) 120 else 180
        val sectionsJson = buildString {
            append("[")
            repeat(sectionCount) { i ->
                if (i > 0) append(",")
                append("""{"name":"section${i+1}","question_count":20,"duration_minutes":20,""")
                append(""""question_types":["multiple_choice"],"max_score":${maxScore/sectionCount},"passing_score":60,"description":""}""")
            }
            append("]")
        }
        return ExamStructureEntity(
            hskLevel             = hsk,
            totalDurationMinutes = 40,
            sectionsJson         = sectionsJson,
            totalMaxScore        = maxScore,
            totalPassingScore    = passingScore,
            vocabularyRequired   = 150,
            scoringNotes         = "",
        )
    }

    private fun fakeQuestionEntity(id: String) = SampleQuestionEntity(
        id               = id,
        hskLevel         = 1,
        section          = "listening",
        questionType     = "multiple_choice",
        questionText     = "Test question?",
        audioTextChinese = null,
        audioTextPinyin  = null,
        optionsJson      = """["A","B","C","D"]""",
        correctAnswer    = "A",
        explanation      = "A is correct.",
    )

    private fun fakeResultEntity(id: Long): ExamResultEntity {
        val scores = listOf(
            SectionScore("listening", 78, 100, 16, 20),
            SectionScore("reading",   82, 100, 17, 20),
        )
        val answers = listOf(AnswerRecord("q1", "A", "A", true))
        return ExamResultEntity(
            id              = id,
            hskLevel        = 1,
            startedAt       = 1000L,
            finishedAt      = 5000L,
            durationSeconds = 4,
            sectionScoresJson = json.encodeToString(scores),
            totalScore      = 160,
            totalMaxScore   = 200,
            passingScore    = 120,
            passed          = 1,
            answersJson     = json.encodeToString(answers),
        )
    }

    private fun fakeExamResult() = ExamResult(
        id              = 0L,
        hskLevel        = 1,
        startedAt       = 1000L,
        finishedAt      = 5000L,
        durationSeconds = 4,
        sectionScores   = emptyList(),
        totalScore      = 120,
        totalMaxScore   = 200,
        passingScore    = 120,
        passed          = true,
        answers         = emptyList(),
    )
}
