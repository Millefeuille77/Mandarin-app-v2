// ToneDrillDao.kt — Mandarin Learn
// DAO for the tone_drills table. Per ARCHITECTURE.md §2.2.

package com.mandarinlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mandarinlearn.data.local.entity.ToneDrillEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tone-pair drills (used by the Listening section).
 */
@Dao
interface ToneDrillDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(drills: List<ToneDrillEntity>)

    /** Returns all tone drills, ordered by id (tone-pair curriculum order). */
    @Query("SELECT * FROM tone_drills ORDER BY id ASC")
    fun getAll(): Flow<List<ToneDrillEntity>>

    /** Total count — used to seed user_progress for the listening section. */
    @Query("SELECT COUNT(*) FROM tone_drills")
    suspend fun count(): Int
}
