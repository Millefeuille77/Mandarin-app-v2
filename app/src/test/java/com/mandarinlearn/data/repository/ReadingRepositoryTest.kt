// ReadingRepositoryTest.kt — Mandarin Learn
// Unit test for ReadingRepository.
// Phase 4: verifies pinyin_annotations parse correctly for HSK 1 (all chars annotated)
// and HSK 4 (key vocab only), and that markCompleted delegates to the DAO.

package com.mandarinlearn.data.repository

import com.mandarinlearn.data.local.dao.ReadingDao
import com.mandarinlearn.data.local.entity.ReadingEntity
import com.mandarinlearn.domain.model.PinyinAnnotation
import com.mandarinlearn.util.DispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadingRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dao: ReadingDao = mockk()
    private val dispatchers: DispatcherProvider = mockk {
        every { io } returns testDispatcher
        every { default } returns testDispatcher
        every { main } returns Dispatchers.Main
    }
    private lateinit var repository: ReadingRepository

    @Before
    fun setUp() {
        kotlinx.coroutines.test.setMain(testDispatcher)
        repository = ReadingRepository(dao, dispatchers)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.test.resetMain()
    }

    // ---- HSK 1: all characters annotated -----------------------------------

    @Test
    fun `HSK 1 passage — all characters have pinyin annotations`() = runTest {
        val annotationsJson = """
            [
              {"character":"你","pinyin":"nǐ"},
              {"character":"好","pinyin":"hǎo"},
              {"character":"，","pinyin":""},
              {"character":"我","pinyin":"wǒ"},
              {"character":"是","pinyin":"shì"},
              {"character":"学","pinyin":"xué"},
              {"character":"生","pinyin":"shēng"},
              {"character":"。","pinyin":""}
            ]
        """.trimIndent()

        val entity = buildEntity(
            id          = "hsk1_reading_001",
            hskLevel    = 1,
            annotations = annotationsJson,
        )
        every { dao.getByLevel(1) } returns flowOf(listOf(entity))

        val results = repository.getPassagesByLevel(1).toList()
        val passages = results.first()

        assertEquals(1, passages.size)
        val passage = passages[0]
        assertEquals("hsk1_reading_001", passage.id)
        assertEquals(8, passage.pinyinAnnotations.size)

        // All hanzi characters have pinyin
        val hanzi = passage.pinyinAnnotations.filter { it.pinyin.isNotEmpty() }
        assertEquals(6, hanzi.size)

        // Punctuation have empty pinyin
        val punctuation = passage.pinyinAnnotations.filter { it.pinyin.isEmpty() }
        assertEquals(2, punctuation.size)
        assertTrue(punctuation.all { it.character == "，" || it.character == "。" })
    }

    @Test
    fun `HSK 1 passage — pinyin uses tone marks not numbers`() = runTest {
        val annotationsJson = """[{"character":"你","pinyin":"nǐ"},{"character":"好","pinyin":"hǎo"}]"""
        val entity = buildEntity("hsk1_reading_002", 1, annotationsJson)
        every { dao.getByLevel(1) } returns flowOf(listOf(entity))

        val passage = repository.getPassagesByLevel(1).toList().first().first()
        val pinyinList = passage.pinyinAnnotations.map { it.pinyin }
        // Tone marks should be present (ǐ, ǎ), not digits
        assertTrue(pinyinList.any { it.contains('ǐ') || it.contains('ǎ') })
        assertFalse(pinyinList.any { it.any { c -> c.isDigit() } })
    }

    // ---- HSK 4: key vocab only annotated -----------------------------------

    @Test
    fun `HSK 4 passage — only key vocabulary has pinyin`() = runTest {
        // HSK 4 passages only annotate key vocab; other characters have empty pinyin
        val annotationsJson = """
            [
              {"character":"经","pinyin":""},
              {"character":"济","pinyin":""},
              {"character":"发","pinyin":"fā"},
              {"character":"展","pinyin":"zhǎn"}
            ]
        """.trimIndent()

        val entity = buildEntity("hsk4_reading_001", 4, annotationsJson)
        every { dao.getByLevel(4) } returns flowOf(listOf(entity))

        val passage = repository.getPassagesByLevel(4).toList().first().first()
        assertEquals(4, passage.pinyinAnnotations.size)

        val annotated = passage.pinyinAnnotations.filter { it.pinyin.isNotEmpty() }
        assertEquals(2, annotated.size) // only 发 and 展 have pinyin
    }

    // ---- Single passage by ID ----------------------------------------------

    @Test
    fun `getPassageById returns correct passage`() = runTest {
        val annotationsJson = """[{"character":"你","pinyin":"nǐ"}]"""
        val entity = buildEntity("hsk1_reading_001", 1, annotationsJson)
        every { dao.getById("hsk1_reading_001") } returns flowOf(entity)

        val passage = repository.getPassageById("hsk1_reading_001").toList().first()
        assertNotNull(passage)
        assertEquals("hsk1_reading_001", passage?.id)
    }

    @Test
    fun `getPassageById returns null for unknown id`() = runTest {
        every { dao.getById("nonexistent") } returns flowOf(null)

        val passage = repository.getPassageById("nonexistent").toList().first()
        assertNull(passage)
    }

    // ---- markCompleted -----------------------------------------------------

    @Test
    fun `markCompleted delegates to DAO`() = runTest {
        coEvery { dao.markCompleted(any(), any()) } returns Unit

        repository.markCompleted("hsk1_reading_001")

        coVerify { dao.markCompleted("hsk1_reading_001", any()) }
    }

    // ---- Malformed JSON — graceful degradation -----------------------------

    @Test
    fun `malformed annotation JSON returns empty list for that passage`() = runTest {
        val entity = buildEntity("hsk1_reading_bad", 1, "{bad json}")
        every { dao.getByLevel(1) } returns flowOf(listOf(entity))

        // toDomainSafe() catches the parse exception and returns null → mapNotNull filters it out
        val passages = repository.getPassagesByLevel(1).toList().first()
        assertTrue("Malformed passage should be filtered out", passages.isEmpty())
    }

    // ---- Helpers -----------------------------------------------------------

    private fun buildEntity(
        id: String,
        hskLevel: Int,
        annotations: String,
    ) = ReadingEntity(
        id                   = id,
        hskLevel             = hskLevel,
        title                = "Test Passage",
        chineseText          = "你好。",
        pinyinAnnotations    = annotations,
        englishTranslation   = "Hello.",
        vocabularyHighlights = """["你好"]""",
        wordCount            = 2,
        isCompleted          = 0,
        completedAt          = null,
    )
}
