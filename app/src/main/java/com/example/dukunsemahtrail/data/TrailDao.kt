package com.example.dukunsemahtrail.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails")
    fun getAllTrails(): Flow<List<Trail>>

    @Query("SELECT * FROM trails WHERE name LIKE '%' || :searchQuery || '%' OR location LIKE '%' || :searchQuery || '%'")
    fun searchTrails(searchQuery: String): Flow<List<Trail>>

    @Query("SELECT * FROM trails WHERE id = :trailId")
    suspend fun getTrailById(trailId: Long): Trail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrails(trails: List<Trail>)

    @Query("DELETE FROM trails")
    suspend fun deleteAllTrails()
}
