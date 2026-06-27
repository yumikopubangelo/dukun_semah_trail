package com.example.dukunsemahtrail.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeDao {
    @Query("SELECT * FROM hike_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<HikeSession>>

    @Query("SELECT * FROM hike_sessions WHERE status = 'Active' LIMIT 1")
    suspend fun getActiveSession(): HikeSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HikeSession): Long

    @Update
    suspend fun updateSession(session: HikeSession)

    @Query("SELECT * FROM hike_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): HikeSession?
}
