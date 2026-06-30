package com.example.dukunsemahtrail.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var trailDao: TrailDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        trailDao = db.trailDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeTrailAndReadInList() = runBlocking {
        val trail = Trail(
            name = "Test Trail",
            location = "Test Location",
            difficulty = "Easy",
            distanceKm = 5.0,
            elevationGainMeters = 100,
            description = "Test Desc",
            terrainType = "Dirt",
            safetyWarnings = "None"
        )
        trailDao.insertTrails(listOf(trail))
        val allTrails = trailDao.getAllTrails().first()
        assertEquals(allTrails[0].name, "Test Trail")
    }

    @Test
    @Throws(Exception::class)
    fun searchTrail() = runBlocking {
        val trail1 = Trail(name = "Mountain High", location = "Alps", difficulty = "Hard", distanceKm = 10.0, elevationGainMeters = 1000, description = "", terrainType = "", safetyWarnings = "")
        val trail2 = Trail(name = "Valley Low", location = "Meadow", difficulty = "Easy", distanceKm = 5.0, elevationGainMeters = 100, description = "", terrainType = "", safetyWarnings = "")
        trailDao.insertTrails(listOf(trail1, trail2))
        
        val searchResult = trailDao.searchTrails("Mountain").first()
        assertEquals(1, searchResult.size)
        assertEquals("Mountain High", searchResult[0].name)
    }
}
