// StartExamUseCase.kt — Mandarin Learn
// Use case: fetch exam structure + questions for a given HSK level.
// IMPLEMENTATION_PLAN.md Phase 7 §E (wiring).

package com.mandarinlearn.domain.usecase

import com.mandarinlearn.data.repository.ExamRepository
import com.mandarinlearn.domain.model.ExamSection
import com.mandarinlearn.domain.model.ExamStructure
import com.mandarinlearn.domain.model.SampleQuestion
import kotlinx.coroutines.flow.first

/**
 * Fetches and bundles everything needed to start an exam for [hsk].
 *
 * Returns [StartExamResult] with the exam structure and questions grouped by section.
 * Questions are fetched up to [section.questionCount]; the grader handles the shortfall
 * transparently via proportional scaling.
 */
class StartExamUseCase(
    private val examRepository: ExamRepository,
) {

    /**
     * Prepares an exam for the given HSK level.
     *
     * @param hsk     HSK level (1–5).
     * @return        [StartExamResult] on success, or failure with a descriptive message.
     */
    suspend operator fun invoke(hsk: Int): Result<StartExamResult> = runCatching {
        val structure = examRepository.getStructure(hsk).first()
            ?: error("Exam structure not found for HSK $hsk — ensure data was imported.")

        // Load questions for each section (up to section.questionCount each).
        val questionsBySection = mutableMapOf<String, List<SampleQuestion>>()
        for (section in structure.sections) {
            val questions = examRepository.getQuestionsForExam(
                hsk     = hsk,
                section = section.name,
                limit   = section.questionCount,
            )
            questionsBySection[section.name] = questions
        }

        StartExamResult(structure = structure, questionsBySection = questionsBySection)
    }
}

/**
 * Output of [StartExamUseCase]: everything needed to run an exam session.
 */
data class StartExamResult(
    val structure: ExamStructure,
    /** Questions keyed by section name. Each list has ≤ section.questionCount items. */
    val questionsBySection: Map<String, List<SampleQuestion>>,
) {
    /** Flattened ordered question list: sections in order, then questions within each. */
    val orderedSections: List<ExamSectionWithQuestions>
        get() = structure.sections.map { section ->
            ExamSectionWithQuestions(
                section   = section,
                questions = questionsBySection[section.name] ?: emptyList(),
            )
        }
}

/**
 * A section together with its loaded questions.
 */
data class ExamSectionWithQuestions(
    val section: ExamSection,
    val questions: List<SampleQuestion>,
)
