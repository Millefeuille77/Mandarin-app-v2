// DataVersionDao.kt — Mandarin Learn
// DAO for the data_version table. Per ARCHITECTURE.md §2.2.
// Single-row table — id is always 1. Used by JsonImporter to detect first launch.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.DataVersionEntity

/**
 * Data Access Object for the data import version record.
 * Non-reactive (suspend only) — checked once at startup, not observed continuously.
 */
@Dao
interface DataVersionDao {

    /** Returns the current version row. Null means no import has ever been run. */
    @Query("SELECT * FROM data_version WHERE id = 1")
    suspend fun get(): DataVersionEntity?

    /** Inserts or replaces the version row after a successful import. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DataVersionEntity)
}
