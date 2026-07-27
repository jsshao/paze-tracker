package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PazePunchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PunchDao {
    @Query("SELECT * FROM paze_punches WHERE cardId = :cardId ORDER BY slotIndex ASC")
    fun getPunchesForCard(cardId: Long): Flow<List<PazePunchEntity>>

    @Query("SELECT * FROM paze_punches WHERE cardId = :cardId AND slotIndex = :slotIndex LIMIT 1")
    suspend fun getPunch(cardId: Long, slotIndex: Int): PazePunchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunch(punch: PazePunchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunches(punches: List<PazePunchEntity>)

    @Update
    suspend fun updatePunch(punch: PazePunchEntity)

    @Query("DELETE FROM paze_punches WHERE cardId = :cardId")
    suspend fun deletePunchesForCard(cardId: Long)

    @Query("DELETE FROM paze_punches")
    suspend fun deleteAllPunches()
}
