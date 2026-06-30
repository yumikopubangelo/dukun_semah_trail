package com.example.dukunsemahtrail.data

import kotlinx.coroutines.flow.Flow

class TrailRepository(private val trailDao: TrailDao, private val hikeDao: HikeDao) {
    val allTrails: Flow<List<Trail>> = trailDao.getAllTrails()

    fun searchTrails(query: String): Flow<List<Trail>> {
        return trailDao.searchTrails(query)
    }

    suspend fun getTrailById(id: Long): Trail? {
        return trailDao.getTrailById(id)
    }

    suspend fun insertTrails(trails: List<Trail>) {
        trailDao.insertTrails(trails)
    }

    // Initial data seeding — only runs when database is empty
    suspend fun seedInitialData() {
        if (trailDao.getTrailCount() > 0) return
        val initialTrails = listOf(
            Trail(
                id = 1,
                name = "Mount Kinabalu - Summit Trail",
                location = "Sabah, Malaysia",
                difficulty = "Hard",
                distanceKm = 8.7,
                elevationGainMeters = 2300,
                description = "The highest peak in Southeast Asia. A challenging climb with breathtaking views from Low's Peak.",
                terrainType = "Granite, Rainforest",
                safetyWarnings = "High altitude, potential for altitude sickness. Sudden weather changes.",
                pathPointsJson = "[[6.075, 116.559], [6.078, 116.562], [6.082, 116.565]]"
            ),
            Trail(
                id = 2,
                name = "Mount Rinjani - Senaru Route",
                location = "Lombok, Indonesia",
                difficulty = "Hard",
                distanceKm = 10.5,
                elevationGainMeters = 2000,
                description = "A strenuous trek to the crater rim of Mount Rinjani, offering views of the Segara Anak lake.",
                terrainType = "Volcanic ash, Forest",
                safetyWarnings = "Loose soil, steep slopes. Active volcano status.",
                pathPointsJson = "[[-8.411, 116.411], [-8.405, 116.415], [-8.395, 116.425]]"
            ),
            Trail(
                id = 3,
                name = "Mount Semeru - Mahameru Peak",
                location = "East Java, Indonesia",
                difficulty = "Expert",
                distanceKm = 13.0,
                elevationGainMeters = 3676,
                description = "The highest volcano in Java. The final ascent to Mahameru is extremely steep and covered in volcanic sand.",
                terrainType = "Volcanic sand, Pine forest",
                safetyWarnings = "Extreme wind, volcanic gas, rockfalls. Only for experienced climbers.",
                pathPointsJson = "[[-8.108, 112.922], [-8.115, 112.930], [-8.125, 112.940]]"
            ),
            Trail(
                id = 4,
                name = "Mount Merapi - Selo Route",
                location = "Central Java, Indonesia",
                difficulty = "Moderate",
                distanceKm = 4.5,
                elevationGainMeters = 1200,
                description = "A relatively short but steep climb to one of the most active volcanoes in the world.",
                terrainType = "Volcanic rock, Shrubs",
                safetyWarnings = "Volcanic activity monitoring required. Steep cliffs near the crater.",
                pathPointsJson = "[[-7.540, 110.446], [-7.545, 110.440], [-7.550, 110.435]]"
            ),
            Trail(
                id = 5,
                name = "Mount Kerinci",
                location = "Jambi, Indonesia",
                difficulty = "Hard",
                distanceKm = 9.0,
                elevationGainMeters = 3805,
                description = "The highest volcano in Indonesia and the highest peak on Sumatra.",
                terrainType = "Dense rainforest, Volcanic rock",
                safetyWarnings = "Dense jungle, slippery trails, potential for mudslides.",
                pathPointsJson = "[[-1.697, 101.264], [-1.705, 101.270], [-1.715, 101.280]]"
            )
        )
        
        trailDao.insertTrails(initialTrails)
        
        // Seed some history
        val finishedHikes = listOf(
            HikeSession(trailId = 4, startTime = System.currentTimeMillis() - 86400000 * 2, currentDistanceKm = 4.5, currentElevationGainMeters = 1200, status = "Finished"),
            HikeSession(trailId = 1, startTime = System.currentTimeMillis() - 86400000 * 10, currentDistanceKm = 8.7, currentElevationGainMeters = 2300, status = "Finished")
        )
        for (hike in finishedHikes) {
            hikeDao.insertSession(hike)
        }
    }
}
