// JsonImporterMappers.kt — Mandarin Learn
// DTO → Entity mapping extension functions used by JsonImporter.
// Extracted here to keep JsonImporter.kt ≤ 300 lines (per CLAUDE.md file rule).

package com.mandarinlearn.data.local.import

import com.mandarinlearn.data.local.entity.ConversationPhraseEntity
import com.mandarinlearn.data.local.entity.ExamStructureEntity
import com.mandarinlearn.data.local.entity.ReadingEntity
import com.mandarinlearn.data.local.entity.SampleQuestionEntity
import com.mandarinlearn.data.local.entity.ToneDrillEntity
import com.mandarinlearn.data.local.entity.VocabularyEntity
import com.mandarinlearn.data.local.import.dto.ConversationPhraseDto
import com.mandarinlearn.data.local.import.dto.ExamSectionDto
import com.mandarinlearn.data.local.import.dto.ExamStructureDto
import com.mandarinlearn.data.local.import.dto.PinyinAnnotationDto
import com.mandarinlearn.data.local.import.dto.ReadingDto
import com.mandarinlearn.data.local.import.dto.SampleQuestionDto
import com.mandarinlearn.data.local.import.dto.ToneDrillDto
import com.mandarinlearn.data.local.import.dto.ToneExampleDto
import com.mandarinlearn.data.local.import.dto.VocabularyDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// Shared Json instance for DTO → Entity encoding (lenient for forward compatibility).
private val mapperJson = Json { ignoreUnknownKeys = true; explicitNulls = false }

internal fun VocabularyDto.toVocabularyEntity() = VocabularyEntity(
    id = id,
    hskLevel = hskLevel,
    character = character,
    pinyin = pinyin,
    translation = translation,
    partOfSpeech = partOfSpeech,
    exampleChinese = exampleSentence.chinese,
    examplePinyin = exampleSentence.pinyin,
    exampleEnglish = exampleSentence.english,
)

internal fun ReadingDto.toReadingEntity(): ReadingEntity {
    val annotationsJson = mapperJson.encodeToString(
        ListSerializer(PinyinAnnotationDto.serializer()),
        pinyinAnnotations,
    )
    val highlightsJson = mapperJson.encodeToString(
        ListSerializer(String.serializer()),
        vocabularyHighlights,
    )
    return ReadingEntity(
        id = id,
        hskLevel = hskLevel,
        title = title,
        chineseText = chineseText,
        pinyinAnnotations = annotationsJson,
        englishTranslation = englishTranslation,
        vocabularyHighlights = highlightsJson,
        wordCount = wordCount,
    )
}

internal fun ToneDrillDto.toToneDrillEntity(): ToneDrillEntity {
    val examplesJson = mapperJson.encodeToString(
        ListSerializer(ToneExampleDto.serializer()),
        additionalExamples,
    )
    return ToneDrillEntity(
        id = id,
        tonePair = tonePair,
        description = description,
        exampleWord = exampleWord,
        pinyin = pinyin,
        translation = translation,
        additionalExamples = examplesJson,
    )
}

internal fun ConversationPhraseDto.toConversationPhraseEntity() = ConversationPhraseEntity(
    id = id,
    hskLevel = hskLevel,
    category = category,
    chinese = chinese,
    pinyin = pinyin,
    english = english,
    usageContext = usageContext,
)

internal fun ExamStructureDto.toExamStructureEntity(): ExamStructureEntity {
    val sectionsJson = mapperJson.encodeToString(
        ListSerializer(ExamSectionDto.serializer()),
        sections,
    )
    return ExamStructureEntity(
        hskLevel = hskLevel,
        totalDurationMinutes = totalDurationMinutes,
        sectionsJson = sectionsJson,
        totalMaxScore = totalMaxScore,
        totalPassingScore = totalPassingScore,
        vocabularyRequired = vocabularyRequired,
        scoringNotes = scoringNotes,
    )
}

internal fun SampleQuestionDto.toSampleQuestionEntity(): SampleQuestionEntity {
    val optionsJson = mapperJson.encodeToString(
        ListSerializer(String.serializer()),
        options,
    )
    return SampleQuestionEntity(
        id = id,
        hskLevel = hskLevel,
        section = section,
        questionType = questionType,
        questionText = questionText,
        audioTextChinese = audioTextChinese,
        audioTextPinyin = audioTextPinyin,
        optionsJson = optionsJson,
        correctAnswer = correctAnswer,
        explanation = explanation,
    )
}
