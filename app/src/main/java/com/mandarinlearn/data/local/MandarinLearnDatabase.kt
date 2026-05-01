// MandarinLearnDatabase.kt — Mandarin Learn
// Room database definition. Per ARCHITECTURE.md §2.
// Database name: mandarin_learn.db, version 1.
// All 11 entities registered here. Migrations declared in MIGRATIONS.kt.

package com.mandarinlearn.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mandarinlearn.data.local.dao.AudioCacheDao
import com.mandarinlearn.data.local.dao.ConversationPhraseDao
import com.mandarinlearn.data.local.dao.DataVersionDao
import com.mandarinlearn.data.local.dao.ExamResultDao
import com.mandarinlearn.data.local.dao.ExamStructureDao
import com.mandarinlearn.data.local.dao.ReadingDao
import com.mandarinlearn.data.local.dao.SampleQuestionDao
import com.mandarinlearn.data.local.dao.StreakDao
import com.mandarinlearn.data.local.dao.ToneDrillDao
import com.mandarinlearn.data.local.dao.UserProgressDao
import com.mandarinlearn.data.local.dao.VocabularyDao
import com.mandarinlearn.data.local.entity.AudioCacheEntity
import com.mandarinlearn.data.local.entity.ConversationPhraseEntity
import com.mandarinlearn.data.local.entity.DataVersionEntity
import com.mandarinlearn.data.local.entity.ExamResultEntity
import com.mandarinlearn.data.local.entity.ExamStructureEntity
import com.mandarinlearn.data.local.entity.ReadingEntity
import com.mandarinlearn.data.local.entity.SampleQuestionEntity
import com.mandarinlearn.data.local.entity.StreakEntity
import com.mandarinlearn.data.local.entity.ToneDrillEntity
import com.mandarinlearn.data.local.entity.UserProgressEntity
import com.mandarinlearn.data.local.entity.VocabularyEntity
import com.mandarinlearn.data.local.migrations.MIGRATIONS

/**
 * Root Room database for Mandarin Learn.
 * Version 1 has no migrations. Future versions declare them in MIGRATIONS.kt.
 * Instantiated once (singleton) via [create] and held in AppContainer.
 */
@Database(
    entities = [
        VocabularyEntity::class,
        ReadingEntity::class,
        ConversationPhraseEntity::class,
        ToneDrillEntity::class,
        ExamStructureEntity::class,
        SampleQuestionEntity::class,
        ExamResultEntity::class,
        AudioCacheEntity::class,
        UserProgressEntity::class,
        StreakEntity::class,
        DataVersionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MandarinLearnDatabase : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao
    abstract fun readingDao(): ReadingDao
    abstract fun conversationPhraseDao(): ConversationPhraseDao
    abstract fun toneDrillDao(): ToneDrillDao
    abstract fun examStructureDao(): ExamStructureDao
    abstract fun sampleQuestionDao(): SampleQuestionDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun audioCacheDao(): AudioCacheDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun streakDao(): StreakDao
    abstract fun dataVersionDao(): DataVersionDao

    companion object {
        private const val DB_NAME = "mandarin_learn.db"

        /**
         * Creates the singleton database instance.
         * Pass [inMemory]=true for instrumented tests to avoid disk I/O.
         */
        fun create(context: Context, inMemory: Boolean = false): MandarinLearnDatabase {
            val builder = if (inMemory) {
                Room.inMemoryDatabaseBuilder(context, MandarinLearnDatabase::class.java)
            } else {
                Room.databaseBuilder(context, MandarinLearnDatabase::class.java, DB_NAME)
            }
            return builder
                .addMigrations(*MIGRATIONS)
                .fallbackToDestructiveMigration() // dev safety net only — remove before 1.0
                .build()
        }
    }
}
